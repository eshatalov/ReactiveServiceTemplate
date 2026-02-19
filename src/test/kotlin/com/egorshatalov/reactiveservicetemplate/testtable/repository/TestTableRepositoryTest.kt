package com.egorshatalov.reactiveservicetemplate.testtable.repository

import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.egorshatalov.reactiveservicetemplate.AbstractContextTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class TestTableRepositoryTest : AbstractContextTest() {

    @Autowired
    private lateinit var repository: TestTableRepository

    @Test
    @DataSet
    fun `should insert and find by id`(): Unit = runBlocking {
        val inserted = repository.insert("Test Item")

        assertThat(inserted.id).isNotNull()
        assertThat(inserted.name).isEqualTo("Test Item")
        assertThat(inserted.createdAt).isNotNull()
        assertThat(inserted.updatedAt).isNotNull()

        val found = repository.findById(inserted.id)
        assertThat(found).isNotNull
        assertThat(found?.name).isEqualTo("Test Item")
        assertThat(found?.updatedAt).isNotNull()
    }

    @Test
    @DataSet("datasets/test_table/test_table.yml", useSequenceFiltering = false)
    fun `should find all items`(): Unit = runBlocking {
        val allItems = repository.findAll().toList()
        assertThat(allItems).hasSize(2)
        assertThat(allItems.map { it.name }).containsExactlyInAnyOrder("Test Item 1", "Test Item 2")
    }

    @Test
    @DataSet
    fun `should update item`(): Unit = runBlocking {
        val inserted = repository.insert("Original Name")

        val updated = repository.update(inserted.id, "Updated Name")

        assertThat(updated).isNotNull
        assertThat(updated?.name).isEqualTo("Updated Name")
        assertThat(updated?.id).isEqualTo(inserted.id)
        assertThat(updated?.updatedAt).isNotNull()
        assertThat(updated?.updatedAt).isNotEqualTo(inserted.updatedAt)
    }

    @Test
    @DataSet
    fun `should return null when updating non-existent item`(): Unit = runBlocking {
        val nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        val updated = repository.update(nonExistentId, "Updated Name")

        assertThat(updated).isNull()
    }

    @Test
    @DataSet("datasets/test_table/test_table.yml", useSequenceFiltering = false)
    @ExpectedDataSet("datasets/test_table/expected/test_table_after_delete.yml")
    fun `should delete item by id`(): Unit = runBlocking {
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val deleted = repository.deleteById(id)

        assertThat(deleted).isTrue()
    }

    @Test
    @DataSet
    fun `should return false when deleting non-existent item`(): Unit = runBlocking {
        val nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        val deleted = repository.deleteById(nonExistentId)

        assertThat(deleted).isFalse()
    }

    @Test
    @DataSet
    fun `should check if item exists by id`(): Unit = runBlocking {
        val inserted = repository.insert("Existing Item")
        val nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")

        assertThat(repository.existsById(inserted.id)).isTrue()
        assertThat(repository.existsById(nonExistentId)).isFalse()
    }

    @Test
    @DataSet
    fun `should return null when finding non-existent item`(): Unit = runBlocking {
        val nonExistentId = UUID.fromString("550e8400-e29b-41d4-a716-446655449999")
        val found = repository.findById(nonExistentId)

        assertThat(found).isNull()
    }
}
