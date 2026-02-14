package com.egorshatalov.reactiveservicetemplate.config

import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider
import javax.sql.DataSource

@TestConfiguration
class TestDatabaseConfig {

    private val embeddedPostgres: EmbeddedPostgres = EmbeddedPostgres.builder().start()

    @Bean(destroyMethod = "close")
    @Primary
    fun embeddedPostgresInstance(): EmbeddedPostgres = embeddedPostgres

    @Bean
    @Primary
    fun connectionFactory(): ConnectionFactory {
        val jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres")

        // Parse JDBC URL to extract host, port, and database name
        // JDBC URL format: jdbc:postgresql://host:port/database?params
        val regex = Regex("jdbc:postgresql://([^:]+):(\\d+)/([^?]+)")
        val match = regex.find(jdbcUrl) ?: throw IllegalStateException("Failed to parse JDBC URL: $jdbcUrl")

        val host = match.groupValues[1]
        val port = match.groupValues[2].toInt()
        val database = match.groupValues[3]

        val options = ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "postgresql")
            .option(ConnectionFactoryOptions.HOST, host)
            .option(ConnectionFactoryOptions.PORT, port)
            .option(ConnectionFactoryOptions.DATABASE, database)
            .option(ConnectionFactoryOptions.USER, "postgres")
            .option(ConnectionFactoryOptions.PASSWORD, "postgres")
            .build()

        return PostgresqlConnectionFactoryProvider().create(options)
    }

    @Bean
    @Primary
    fun dataSource(): DataSource {
        return embeddedPostgres.postgresDatabase
    }
}
