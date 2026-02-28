package com.github.template.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.slf4j.LoggerFactory
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig {

    private val logger = LoggerFactory.getLogger(FlywayConfig::class.java)

    @Bean
    fun flywayValidationStrategy(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway: Flyway ->
            logger.info("Flyway validation enabled - migrations will NOT run automatically")
            logger.info("To migrate database, run: ./gradlew dbMigrate")

            try {
                flyway.validate()
                logger.info("Schema validation successful")
            } catch (e: FlywayException) {
                logger.error("Schema validation failed: {}", e.message)
                throw IllegalStateException(
                    "Database schema validation failed. " +
                    "Run migrations with: ./gradlew dbMigrate",
                    e
                )
            }
        }
    }
}
