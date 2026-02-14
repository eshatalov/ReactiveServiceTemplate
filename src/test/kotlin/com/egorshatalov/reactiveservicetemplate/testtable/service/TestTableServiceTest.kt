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

class TestTableServiceTest {

    private val repository = mockk<TestTableRepository>()
    private val service = TestTableService(repository)

    @Test
    fun `findAll should return all items as responses`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val testTable1 = TestTable(id = 1, name = "Item 1", createdAt = now, updatedAt = now)
        val testTable2 = TestTable(id = 2, name = "Item 2", createdAt = now, updatedAt = now)
        coEvery { repository.findAll() } returns flowOf(testTable1, testTable2)

        val result = service.findAll().toList()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[0].name).isEqualTo("Item 1")
        assertThat(result[1].id).isEqualTo(2)
        assertThat(result[1].name).isEqualTo("Item 2")
    }

    @Test
    fun `findById should return response when item exists`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val testTable = TestTable(id = 1, name = "Test Item", createdAt = now, updatedAt = now)
        coEvery { repository.findById(1) } returns testTable

        val result = service.findById(1)

        assertThat(result.id).isEqualTo(1)
        assertThat(result.name).isEqualTo("Test Item")
    }

    @Test
    fun `findById should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        coEvery { repository.findById(999) } returns null

        assertThatThrownBy { runBlocking { service.findById(999) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id 999 not found")
    }

    @Test
    fun `create should insert and return response`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val request = CreateTestTableRequest(name = "New Item")
        val inserted = TestTable(id = 1, name = "New Item", createdAt = now, updatedAt = now)
        coEvery { repository.insert("New Item") } returns inserted

        val result = service.create(request)

        assertThat(result.id).isEqualTo(1)
        assertThat(result.name).isEqualTo("New Item")
        coVerify { repository.insert("New Item") }
    }

    @Test
    fun `update should update and return response when item exists`(): Unit = runBlocking {
        val now = OffsetDateTime.now()
        val request = UpdateTestTableRequest(name = "Updated Name")
        val updated = TestTable(id = 1, name = "Updated Name", createdAt = now, updatedAt = now)
        coEvery { repository.update(1, "Updated Name") } returns updated

        val result = service.update(1, request)

        assertThat(result.id).isEqualTo(1)
        assertThat(result.name).isEqualTo("Updated Name")
        coVerify { repository.update(1, "Updated Name") }
    }

    @Test
    fun `update should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        val request = UpdateTestTableRequest(name = "Updated Name")
        coEvery { repository.update(999, "Updated Name") } returns null

        assertThatThrownBy { runBlocking { service.update(999, request) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id 999 not found")
    }

    @Test
    fun `delete should delete when item exists`(): Unit = runBlocking {
        coEvery { repository.deleteById(1) } returns true

        service.delete(1)

        coVerify { repository.deleteById(1) }
    }

    @Test
    fun `delete should throw NotFoundException when item does not exist`(): Unit = runBlocking {
        coEvery { repository.deleteById(999) } returns false

        assertThatThrownBy { runBlocking { service.delete(999) } }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("TestTable with id 999 not found")
    }
}
