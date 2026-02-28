package com.github.template.testtable.handler

import com.github.template.testtable.service.NotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class TestTableRouter {

    @Bean
    fun testTableRoutes(handler: TestTableHandler) = coRouter {
        "/api/test-tables".nest {
            GET("", handler::findAll)
            GET("/{id}", handler::findById)
            POST("", handler::create)
            PUT("/{id}", handler::update)
            DELETE("/{id}", handler::delete)
        }

        onError<NotFoundException> { exception, _ ->
            ServerResponse.status(HttpStatus.NOT_FOUND)
                .bodyValueAndAwait(mapOf("error" to (exception.message ?: "Resource not found")))
        }
    }
}
