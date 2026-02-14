package com.egorshatalov.reactiveservicetemplate.config

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class TestFlywayConfig() {

    private val logger = LoggerFactory.getLogger(TestFlywayConfig::class.java)

    @Bean
    @Primary
    fun testFlywayMigrationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway: Flyway ->
            logger.info("Running Flyway migrations for tests")
            flyway.clean()
            flyway.migrate()
            logger.info("Flyway migrations completed successfully")
        }
    }
}
