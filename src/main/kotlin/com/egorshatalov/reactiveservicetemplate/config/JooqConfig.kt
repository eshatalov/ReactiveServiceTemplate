package com.egorshatalov.reactiveservicetemplate.config

import io.r2dbc.spi.ConnectionFactory
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy

@Configuration
class JooqConfig {

    @Bean
    fun dslContext(connectionFactory: ConnectionFactory): DSLContext {
        val transactionAwareFactory = TransactionAwareConnectionFactoryProxy(connectionFactory)
        return DSL.using(transactionAwareFactory, SQLDialect.POSTGRES)
    }
}
