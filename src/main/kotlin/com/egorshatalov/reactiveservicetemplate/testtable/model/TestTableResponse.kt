package com.egorshatalov.reactiveservicetemplate.testtable.model

import com.egorshatalov.reactiveservicetemplate.jooq.tables.pojos.TestTable
import org.jooq.JSONB
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class TestTableResponse(
    val id: UUID,
    val name: String,
    val eventDate: LocalDate,
    val eventTimestamp: OffsetDateTime,
    val metadata: TestTableMetadata,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

// ObjectMapper instance for JSONB conversion
private val objectMapper = jacksonObjectMapper()

fun TestTable.toResponse(): TestTableResponse {
    return TestTableResponse(
        id = this.id,
        name = this.name,
        eventDate = this.eventDate,
        eventTimestamp = this.eventTimestamp,
        metadata = parseMetadata(this.metadata)!!,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

private fun parseMetadata(jsonb: JSONB): TestTableMetadata? {
    return try {
        objectMapper.readValue<TestTableMetadata>(jsonb.data())
    } catch (e: Exception) {
        null
    }
}
