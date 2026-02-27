package com.egorshatalov.reactiveservicetemplate.config

import com.egorshatalov.reactiveservicetemplate.testtable.service.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException
import tools.jackson.core.JacksonException
import tools.jackson.databind.DatabindException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ProblemDetail {
        logger.warn("Resource not found: ${e.message}")
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message ?: "Resource not found")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ProblemDetail {
        logger.warn("Invalid request: ${e.message}")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
    }

    @ExceptionHandler(DatabindException::class)
    fun handleDatabindException(e: DatabindException): ProblemDetail {
        val errorMessage = formatJacksonErrorMessage(e as JacksonException)
        logger.warn("JSON mapping error: $errorMessage")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage)
    }

    @ExceptionHandler(JacksonException::class)
    fun handleJacksonException(e: JacksonException): ProblemDetail {
        val errorMessage = formatJacksonErrorMessage(e)
        logger.warn("Invalid JSON: $errorMessage")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage)
    }

    @ExceptionHandler(ServerWebInputException::class, DecodingException::class)
    fun handleDecodingExceptions(e: Throwable): ProblemDetail {
        val jacksonException = findJacksonExceptionInCauseChain(e)
        val errorMessage = if (jacksonException != null) {
            formatJacksonErrorMessage(jacksonException)
        } else {
            e.message ?: "Invalid request body"
        }
        logger.warn("Invalid request body: $errorMessage")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage)
    }

    private fun formatJacksonErrorMessage(e: JacksonException): String {
        val fieldPath = e.path.joinToString(".") { ref ->
            ref.propertyName ?: "[${ref.index}]"
        }
        val rawMessage = e.message ?: "unknown error"
        val cleanMessage = rawMessage.substringBefore(" at [Source:")
            .substringBefore(" (through reference chain:")
            .trim()
        return if (fieldPath.isNotEmpty()) {
            "Invalid value for field '$fieldPath': $cleanMessage"
        } else {
            "Invalid JSON: $cleanMessage"
        }
    }

    private fun findJacksonExceptionInCauseChain(e: Throwable): JacksonException? {
        var searchCause = e.cause
        while (searchCause != null) {
            if (searchCause is JacksonException) {
                return searchCause
            }
            searchCause = searchCause.cause
        }
        return null
    }
}
