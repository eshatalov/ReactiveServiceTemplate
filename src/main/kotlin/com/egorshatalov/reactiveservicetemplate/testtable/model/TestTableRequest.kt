package com.egorshatalov.reactiveservicetemplate.testtable.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.OffsetDateTime

data class CreateTestTableRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Event date is required")
    val eventDate: LocalDate,
    @field:NotNull(message = "Event timestamp is required")
    val eventTimestamp: OffsetDateTime,
    @field:NotNull(message = "Metadata is required")
    val metadata: TestTableMetadata
)

data class UpdateTestTableRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Event date is required")
    val eventDate: LocalDate,
    @field:NotNull(message = "Event timestamp is required")
    val eventTimestamp: OffsetDateTime,
    @field:NotNull(message = "Metadata is required")
    val metadata: TestTableMetadata
)
