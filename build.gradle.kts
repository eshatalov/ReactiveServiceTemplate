// Plugin versions (inline required by Gradle Kotlin DSL plugins block)
plugins {
    kotlin("jvm") version "2.3.10" apply false
    kotlin("plugin.spring") version "2.3.10" apply false
    id("org.springframework.boot") version "4.0.2" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.graalvm.buildtools.native") version "0.10.6" apply false
    id("org.openapi.generator") version "7.14.0" apply false
}

// Shared dependency versions available to all subprojects
val kotlinVersion by extra("2.3.10")
val springBootVersion by extra("4.0.2")
val graalvmNativeBuildToolsVersion by extra("0.10.6")
val flywayVersion by extra("11.14.0")
val postgresqlVersion by extra("42.7.9")
val jooqVersion by extra("3.19.29")
val zonkyEmbeddedPostgresVersion by extra("2.2.0")
val mockkVersion by extra("1.13.16")
val dbRiderVersion by extra("1.44.0")
val javaVersion by extra(25)
val openApiGeneratorVersion by extra("7.14.0")

subprojects {
    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(javaVersion)
                vendor = JvmVendorSpec.ORACLE
            }
        }

        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(javaVersion)

            compilerOptions {
                freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        // Suppress ByteBuddy agent warnings from MockK
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // Suppress CDS warning when a bootstrap classpath is modified
        jvmArgs("-Xshare:off")

        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}
