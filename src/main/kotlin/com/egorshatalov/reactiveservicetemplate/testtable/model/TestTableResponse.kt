package com.egorshatalov.reactiveservicetemplate.testtable.model

import java.time.OffsetDateTime

data class TestTableResponse(
    val id: Long,
    val name: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
)

fun com.egorshatalov.reactiveservicetemplate.jooq.tables.pojos.TestTable.toResponse(): TestTableResponse {
    return TestTableResponse(
        id = this.id ?: throw IllegalStateException("ID should not be null for persisted entity"),
        name = this.name,
        createdAt = this.createdAt ?: throw IllegalStateException("createdAt should not be null for persisted entity"),
        updatedAt = this.updatedAt ?: throw IllegalStateException("updatedAt should not be null for persisted entity")
    )
}
