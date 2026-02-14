package com.egorshatalov.reactiveservicetemplate.testtable.repository

import com.egorshatalov.reactiveservicetemplate.jooq.tables.pojos.TestTable
import com.egorshatalov.reactiveservicetemplate.jooq.tables.references.TEST_TABLE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.kotlin.coroutines.transactionCoroutine
import org.springframework.stereotype.Repository

@Repository
class TestTableRepository(
    private val dsl: DSLContext
) {

    fun findAll(): Flow<TestTable> {
        return dsl
            .selectFrom(TEST_TABLE)
            .asFlow()
            .map { it.into(TestTable::class.java) }
    }

    suspend fun findById(id: Long): TestTable? {
        return dsl
            .selectFrom(TEST_TABLE)
            .where(TEST_TABLE.ID.eq(id))
            .awaitFirstOrNull()
            ?.into(TestTable::class.java)
    }

    suspend fun insert(name: String): TestTable {
        return dsl.transactionCoroutine { config ->
            config.dsl()
                .insertInto(TEST_TABLE)
                .set(TEST_TABLE.NAME, name)
                .returning()
                .awaitFirst()
                .into(TestTable::class.java)
        }
    }

    suspend fun update(id: Long, name: String): TestTable? {
        return dsl.transactionCoroutine { config ->
            config.dsl()
                .update(TEST_TABLE)
                .set(TEST_TABLE.NAME, name)
                .set(TEST_TABLE.UPDATED_AT, DSL.currentOffsetDateTime())
                .where(TEST_TABLE.ID.eq(id))
                .returning()
                .awaitFirstOrNull()
                ?.into(TestTable::class.java)
        }
    }

    suspend fun deleteById(id: Long): Boolean {
        return dsl.transactionCoroutine { config ->
            config.dsl()
                .deleteFrom(TEST_TABLE)
                .where(TEST_TABLE.ID.eq(id))
                .returning()
                .awaitFirstOrNull() != null
        }
    }

    suspend fun existsById(id: Long): Boolean {
        val count = dsl
            .select(DSL.count())
            .from(TEST_TABLE)
            .where(TEST_TABLE.ID.eq(id))
            .awaitFirstOrNull()
            ?.value1()
        return (count ?: 0) > 0
    }
}
