package com.egorshatalov.reactiveservicetemplate.testtable.model

import com.egorshatalov.reactiveservicetemplate.jooq.tables.pojos.TestTable
import java.time.OffsetDateTime
import java.util.UUID

data class TestTableResponse(
    val id: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

fun TestTable.toResponse(): TestTableResponse {
    return TestTableResponse(
        id = this.id,
        name = this.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
