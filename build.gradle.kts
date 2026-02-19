// Plugin versions (inline required by Gradle Kotlin DSL plugins block)
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

// Dependency versions
val kotlinVersion = "2.3.10"
val springBootVersion = "4.0.2"
val flywayVersion = "11.14.0"
val postgresqlVersion = "42.7.9"
val jooqVersion = "3.19.29"
val zonkyEmbeddedPostgresVersion = "2.2.0"
val mockkVersion = "1.13.16"
val dbRiderVersion = "1.44.0"
val javaVersion = 25

buildscript {
    // Dependency versions for buildscript classpath
    val flywayVersion = "11.14.0"
    val postgresqlVersion = "42.7.9"
    val jooqVersion = "3.19.29"
    val zonkyEmbeddedPostgresVersion = "2.2.0"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-core:$flywayVersion")
        classpath("org.flywaydb:flyway-database-postgresql:$flywayVersion")
        classpath("org.postgresql:postgresql:$postgresqlVersion")
        classpath("org.jooq:jooq:$jooqVersion")
        classpath("org.jooq:jooq-meta:$jooqVersion")
        classpath("org.jooq:jooq-codegen:$jooqVersion")
        classpath("io.zonky.test:embedded-postgres:$zonkyEmbeddedPostgresVersion")
    }
}

group = "com.egorshatalov"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("tools.jackson.module:jackson-module-kotlin")

    // jOOQ Kotlin Coroutines support
    implementation("org.jooq:jooq-kotlin-coroutines:$jooqVersion")

    // Flyway Spring Boot starter for app-time migrations/validation
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")

    // PostgreSQL JDBC driver (for Flyway + jOOQ)
    runtimeOnly("org.postgresql:postgresql")

    // PostgreSQL R2DBC driver (for reactive WebFlux)
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    testImplementation("org.postgresql:r2dbc-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Zonky embedded PostgreSQL for tests
    testImplementation("io.zonky.test:embedded-postgres:$zonkyEmbeddedPostgresVersion")

    // Database Rider for declarative database testing
    testImplementation("com.github.database-rider:rider-spring:$dbRiderVersion")
}

kotlin {
    jvmToolchain(javaVersion)

    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
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

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get().asFile}/generated-sources/jooq")
        }
    }
}

// Custom Flyway tasks using Flyway API directly
/**
 * Base class for Flyway tasks with shared database configuration.
 * Configuration is resolved lazily from: Environment variables > gradle.properties
 */
abstract class BaseFlywayTask : DefaultTask() {
    /**
     * Database configuration resolved lazily with fail-fast behavior.
     */
    private val dbConfig by lazy {
        val url = System.getenv("DB_URL")
            ?: project.findProperty("flyway.url") as String?
            ?: throw GradleException(
                """
                |Missing database configuration: Database JDBC URL
                |
                |Set DB_URL environment variable or add to gradle.properties:
                |  flyway.url=<your-jdbc-url>
                |
                |See gradle.properties.template for example configuration.
                |""".trimMargin()
            )
        val user = System.getenv("DB_USER")
            ?: project.findProperty("flyway.user") as String?
            ?: throw GradleException(
                """
                |Missing database configuration: Database username
                |
                |Set DB_USER environment variable or add to gradle.properties:
                |  flyway.user=<your-username>
                |
                |See gradle.properties.template for example configuration.
                |""".trimMargin()
            )
        val password = System.getenv("DB_PASSWORD")
            ?: project.findProperty("flyway.password") as String?
            ?: throw GradleException(
                """
                |Missing database configuration: Database password
                |
                |Set DB_PASSWORD environment variable or add to gradle.properties:
                |  flyway.password=<your-password>
                |
                |See gradle.properties.template for example configuration.
                |""".trimMargin()
            )
        DatabaseConfig(url, user, password)
    }

    /**
     * Creates a configured Flyway instance with common settings.
     * Subclasses can override specific settings via configure callback.
     */
    protected fun createFlyway(configure: (org.flywaydb.core.api.configuration.FluentConfiguration) -> Unit = {}): org.flywaydb.core.Flyway {
        val builder = org.flywaydb.core.Flyway.configure()
            .dataSource(dbConfig.url, dbConfig.user, dbConfig.password)
            .locations("filesystem:build/resources/main/db/migration")
        configure(builder)
        return builder.load()
    }

    /**
     * Data class to hold database configuration.
     */
    private data class DatabaseConfig(
        val url: String,
        val user: String,
        val password: String
    )
}

abstract class FlywayMigrateTask : BaseFlywayTask() {
    @TaskAction
    fun run() {
        val flyway = createFlyway {
            it.baselineOnMigrate(true)
            it.baselineVersion("0")
        }
        println("Running Flyway migrations...")
        flyway.migrate()
        println("Migrations completed successfully")
    }
}

abstract class FlywayCleanTask : BaseFlywayTask() {
    @TaskAction
    fun run() {
        val flyway = createFlyway {
            it.cleanDisabled(false)
        }
        println("Cleaning database (dropping all objects)...")
        flyway.clean()
        println("Database cleaned successfully")
    }
}

abstract class FlywayInfoTask : BaseFlywayTask() {
    @TaskAction
    fun run() {
        val flyway = createFlyway()
        println("Flyway migration info:")
        val info = flyway.info()
        info.all()?.forEach { migration ->
            println("  ${migration.version} - ${migration.description} - ${migration.state}")
        } ?: println("  No migrations found")
    }
}

abstract class FlywayValidateTask : BaseFlywayTask() {
    @TaskAction
    fun run() {
        val flyway = createFlyway()
        println("Validating Flyway migrations...")
        flyway.validate()
        println("Validation successful")
    }
}

abstract class JooqCodegenTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val migrationDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    init {
        outputs.cacheIf { true }
    }

    @TaskAction
    fun generate() {
        val embeddedPostgres = try {
            println("Starting embedded PostgreSQL for jOOQ code generation...")
            io.zonky.test.db.postgres.embedded.EmbeddedPostgres.builder().start()
        } catch (e: Exception) {
            throw GradleException("Failed to start embedded PostgreSQL", e)
        }

        try {
            val jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres")
            val username = "postgres"
            val password = "postgres"

            println("Embedded PostgreSQL JDBC URL: $jdbcUrl")

            // Run Flyway migrations
            println("Running Flyway migrations...")
            val flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("filesystem:${migrationDirectory.get().asFile.absolutePath}")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()

            val result = flyway.migrate()
            println("Migrations completed: ${result.migrationsExecuted} executed")

            // Ensure output directory exists
            outputDirectory.get().asFile.mkdirs()

            // Configure jOOQ for Kotlin generation
            val configuration = org.jooq.meta.jaxb.Configuration()
                .withJdbc(
                    org.jooq.meta.jaxb.Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(jdbcUrl)
                        .withUser(username)
                        .withPassword(password)
                )
                .withGenerator(
                    org.jooq.meta.jaxb.Generator()
                        .withName("org.jooq.codegen.KotlinGenerator")
                        .withDatabase(
                            org.jooq.meta.jaxb.Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("flyway_schema_history")
                                .withInputSchema("public")
                        )
                        .withGenerate(
                            org.jooq.meta.jaxb.Generate()
                                .withPojosAsKotlinDataClasses(true)
                                .withKotlinNotNullPojoAttributes(true)
                                .withKotlinNotNullRecordAttributes(true)
                                .withKotlinNotNullInterfaceAttributes(true)
                                .withImplicitJoinPathsAsKotlinProperties(true)
                                .withRecords(true)
                                .withPojos(true)
                                .withDaos(false)
                                .withFluentSetters(true)
                                .withDeprecated(false)
                        )
                        .withTarget(
                            org.jooq.meta.jaxb.Target()
                                .withPackageName(packageName.get())
                                .withDirectory(outputDirectory.get().asFile.absolutePath)
                        )
                )

            println("Generating jOOQ Kotlin classes...")
            org.jooq.codegen.GenerationTool.generate(configuration)

            println("jOOQ code generation completed!")
            println("Output: ${outputDirectory.get().asFile.absolutePath}")

        } finally {
            println("Shutting down embedded PostgreSQL...")
            embeddedPostgres.close()
        }
    }
}

// Register the tasks
tasks.register<FlywayMigrateTask>("dbMigrate") {
    group = "database"
    description = "Migrate the database using Flyway"
    dependsOn("processResources")
}

tasks.register<FlywayCleanTask>("dbClean") {
    group = "database"
    description = "Clean the database (drop all objects)"
    dependsOn("processResources")
}

tasks.register<FlywayInfoTask>("dbInfo") {
    group = "database"
    description = "Show migration status and info"
    dependsOn("processResources")
}

tasks.register<FlywayValidateTask>("dbValidate") {
    group = "database"
    description = "Validate Flyway migrations"
    dependsOn("processResources")
}

tasks.register<JooqCodegenTask>("jooqCodegen") {
    group = "jooq"
    description = "Generate jOOQ Kotlin classes from database schema using embedded PostgreSQL"

    migrationDirectory.set(project.layout.buildDirectory.dir("resources/main/db/migration"))
    outputDirectory.set(project.layout.buildDirectory.dir("generated-sources/jooq"))
    packageName.set("com.egorshatalov.reactiveservicetemplate.jooq")

    dependsOn("processResources")
}

// Make compilation depend on jOOQ code generation
tasks.named("compileKotlin") {
    dependsOn("jooqCodegen")
}

tasks.named<Delete>("clean") {
    delete("${layout.buildDirectory.get().asFile}/generated-sources/jooq")
}
