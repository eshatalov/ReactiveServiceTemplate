package com.egorshatalov.reactiveservicetemplate.testtable.service

import com.egorshatalov.reactiveservicetemplate.jooq.tables.pojos.TestTable
import com.egorshatalov.reactiveservicetemplate.testtable.model.CreateTestTableRequest
import com.egorshatalov.reactiveservicetemplate.testtable.model.UpdateTestTableRequest
import com.egorshatalov.reactiveservicetemplate.testtable.repository.TestTableRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class TestTableServiceTest {

    private val repository = mockk<TestTableRepository>()
    private val service = TestTableService(repository)

    @Test
    fun `findAll should return all items as responses`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val id1 = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val id2 = UUID.fromString("550e8400-e29b-41d4-a716-446655440001")
        val testTable1 = TestTable(id = id1, name = "Item 1", createdAt = now, updatedAt = now)
        val testTable2 = TestTable(id = id2, name = "Item 2", createdAt = now, updatedAt = now)
        coEvery { repository.findAll() } returns flowOf(testTable1, testTable2)

        val result = service.findAll().toList()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(id1)
        assertThat(result[0].name).isEqualTo("Item 1")
        assertThat(result[1].id).isEqualTo(id2)
        assertThat(result[1].name).isEqualTo("Item 2")
    }

    @Test
    fun `findById should return response when item exists`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val testTable = TestTable(id = id, name = "Test Item", createdAt = now, updatedAt = now)
        coEvery { repository.findById(id) } returns testTable

        val result = service.findById(id)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("Test Item")
    }

    @Test
    fun `findById should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        coEvery { repository.findById(id) } returns null

        assertThatThrownBy { runBlocking { service.findById(id) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id $id not found")
    }

    @Test
    fun `create should insert and return response`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val request = CreateTestTableRequest(name = "New Item")
        val inserted = TestTable(id = id, name = "New Item", createdAt = now, updatedAt = now)
        coEvery { repository.insert("New Item") } returns inserted

        val result = service.create(request)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("New Item")
        coVerify { repository.insert("New Item") }
    }

    @Test
    fun `update should update and return response when item exists`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val request = UpdateTestTableRequest(name = "Updated Name")
        val updated = TestTable(id = id, name = "Updated Name", createdAt = now, updatedAt = now)
        coEvery { repository.update(id, "Updated Name") } returns updated

        val result = service.update(id, request)

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo("Updated Name")
        coVerify { repository.update(id, "Updated Name") }
    }

    @Test
    fun `update should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        val request = UpdateTestTableRequest(name = "Updated Name")
        coEvery { repository.update(id, "Updated Name") } returns null

        assertThatThrownBy { runBlocking { service.update(id, request) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id $id not found")
    }

    @Test
    fun `delete should delete when item exists`(): Unit = runBlocking {
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        coEvery { repository.deleteById(id) } returns true

        service.delete(id)

        coVerify { repository.deleteById(id) }
    }

    @Test
    fun `delete should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        coEvery { repository.deleteById(id) } returns false

        assertThatThrownBy { runBlocking { service.delete(id) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id $id not found")
    }
}
