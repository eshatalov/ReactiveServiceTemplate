val springBootVersion: String by rootProject.extra

plugins {
    kotlin("jvm")
    id("org.openapi.generator")
}

group = "com.github.template"
version = "0.0.1-SNAPSHOT"

val generatedSourceDir = "${layout.buildDirectory.get().asFile}/generated/src/main/kotlin"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

sourceSets {
    main {
        kotlin {
            srcDir(generatedSourceDir)
        }
    }
}

openApiGenerate {
    generatorName.set("kotlin")
    library.set("jvm-spring-webclient")
    inputSpec.set("$projectDir/src/main/resources/template.yaml")
    outputDir.set("${layout.buildDirectory.get().asFile}/generated")
    modelPackage.set("com.github.template.client.model")
    apiPackage.set("com.github.template.client")
    packageName.set("com.github.template.client")
    configOptions.set(
        mapOf(
            "useSpringBoot3" to "true",
            "serializationLibrary" to "jackson",
            "dateLibrary" to "java8",
            "enumPropertyNaming" to "UPPERCASE",
            "omitGradleWrapper" to "true",
        )
    )
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)
}

// Post-processing: migrate generated code from Jackson 2 to Jackson 3 and fix Spring 7 incompatibilities.
// Jackson 3 moved most packages from com.fasterxml.jackson to tools.jackson, but jackson-annotations
// stayed at com.fasterxml.jackson.annotation (artifact com.fasterxml.jackson.core:jackson-annotations:2.20).
// Spring 7 renamed Jackson2Json* codecs to JacksonJson* and requires JsonMapper instead of ObjectMapper.
val migrateToJackson3 by tasks.registering {
    group = "openapi"
    description = "Migrate generated OpenAPI code from Jackson 2 to Jackson 3 and fix Spring 7 incompatibilities"

    doLast {
        val generatedDir = file(generatedSourceDir)
        if (!generatedDir.exists()) {
            logger.warn("Generated source directory does not exist: $generatedSourceDir")
            return@doLast
        }

        generatedDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                var content = file.readText()
                val originalContent = content

                // Jackson 3: replace package prefixes for databind/module/datatype,
                // but NOT for annotations (they stayed at com.fasterxml.jackson.annotation)
                content = content.replace("com.fasterxml.jackson.databind", "tools.jackson.databind")
                content = content.replace("com.fasterxml.jackson.module", "tools.jackson.module")
                content = content.replace("com.fasterxml.jackson.datatype", "tools.jackson.datatype")

                // Spring 7: renamed Jackson2* codec classes (dropped the "2")
                content = content.replace("Jackson2JsonEncoder", "JacksonJsonEncoder")
                content = content.replace("Jackson2JsonDecoder", "JacksonJsonDecoder")
                content = content.replace("jackson2JsonEncoder", "jacksonJsonEncoder")
                content = content.replace("jackson2JsonDecoder", "jacksonJsonDecoder")

                // Spring 7: JacksonJsonEncoder/Decoder constructors require JsonMapper, not ObjectMapper.
                // ApiClient.request() has T: Any? but Spring's toEntity() requires T: Any.
                if (file.name == "ApiClient.kt") {
                    content = content.replace("reified T: Any?>", "reified T: Any>")
                }

                // ResponseEntity.body is nullable; generated API methods expect non-null return.
                content = content.replace(".map { it.body }", ".map { it.body!! }")

                // Serializer.kt: complete rewrite for Jackson 3 (ObjectMapper is abstract,
                // jacksonObjectMapper() removed, modules registered via JsonMapper.builder())
                if (file.name == "Serializer.kt") {
                    val packageLine = content.lines().first { it.startsWith("package ") }
                    content = buildSerializerJackson3(packageLine)
                }

                if (content != originalContent) {
                    file.writeText(content)
                    logger.lifecycle("Migrated to Jackson 3: ${file.relativeTo(generatedDir)}")
                }
            }
    }
}

fun buildSerializerJackson3(packageLine: String): String = """
$packageLine

import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.KotlinModule

// Jackson 3: Java Time support is built into core (no separate JavaTimeModule).
// WRITE_DATES_AS_TIMESTAMPS moved from SerializationFeature to DateTimeFeature.
// KotlinModule constructor is internal; use Builder instead.
object Serializer {
    @JvmStatic
    val jacksonObjectMapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()
}
""".trimIndent() + "\n"

// Task wiring: openApiGenerate -> migrateToJackson3 -> compileKotlin
migrateToJackson3 {
    dependsOn(tasks.named("openApiGenerate"))
}

tasks.named("compileKotlin") {
    dependsOn(migrateToJackson3)
}
