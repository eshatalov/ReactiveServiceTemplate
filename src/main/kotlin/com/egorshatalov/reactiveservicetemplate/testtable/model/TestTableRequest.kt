package com.egorshatalov.reactiveservicetemplate.testtable.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTestTableRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String
)

data class UpdateTestTableRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String
)
