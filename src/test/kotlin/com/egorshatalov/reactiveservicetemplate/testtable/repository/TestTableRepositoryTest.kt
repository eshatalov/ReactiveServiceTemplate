package com.egorshatalov.reactiveservicetemplate.testtable.repository

import com.egorshatalov.reactiveservicetemplate.AbstractContextTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TestTableRepositoryTest: AbstractContextTest() {

    @Autowired
    private lateinit var repository: TestTableRepository

    @BeforeEach
    fun setUp() = runBlocking {
        // Clean up before each test
        val allItems = repository.findAll().toList()
        allItems.forEach { repository.deleteById(it.id!!) }
    }

    @Test
    fun `should insert and find by id`(): Unit = runBlocking {
        val inserted = repository.insert("Test Item")

        assertThat(inserted.id).isNotNull()
        assertThat(inserted.name).isEqualTo("Test Item")
        assertThat(inserted.createdAt).isNotNull()
        assertThat(inserted.updatedAt).isNotNull()

        val found = repository.findById(inserted.id!!)
        assertThat(found).isNotNull
        assertThat(found?.name).isEqualTo("Test Item")
        assertThat(found?.updatedAt).isNotNull()
    }

    @Test
    fun `should find all items`(): Unit = runBlocking {
        repository.insert("Item 1")
        repository.insert("Item 2")
        repository.insert("Item 3")

        val allItems = repository.findAll().toList()
        assertThat(allItems).hasSize(3)
        assertThat(allItems.map { it.name }).containsExactlyInAnyOrder("Item 1", "Item 2", "Item 3")
    }

    @Test
    fun `should update item`(): Unit = runBlocking {
        val inserted = repository.insert("Original Name")

        val updated = repository.update(inserted.id!!, "Updated Name")

        assertThat(updated).isNotNull
        assertThat(updated?.name).isEqualTo("Updated Name")
        assertThat(updated?.id).isEqualTo(inserted.id)
        assertThat(updated?.updatedAt).isNotNull()
        assertThat(updated?.updatedAt).isNotEqualTo(inserted.updatedAt)
    }

    @Test
    fun `should return null when updating non-existent item`(): Unit = runBlocking {
        val updated = repository.update(99999, "Updated Name")

        assertThat(updated).isNull()
    }

    @Test
    fun `should delete item by id`(): Unit = runBlocking {
        val inserted = repository.insert("To Be Deleted")

        val deleted = repository.deleteById(inserted.id!!)

        assertThat(deleted).isTrue()
        assertThat(repository.findById(inserted.id!!)).isNull()
    }

    @Test
    fun `should return false when deleting non-existent item`(): Unit = runBlocking {
        val deleted = repository.deleteById(99999)

        assertThat(deleted).isFalse()
    }

    @Test
    fun `should check if item exists by id`(): Unit = runBlocking {
        val inserted = repository.insert("Existing Item")

        assertThat(repository.existsById(inserted.id!!)).isTrue()
        assertThat(repository.existsById(99999)).isFalse()
    }

    @Test
    fun `should return null when finding non-existent item`(): Unit = runBlocking {
        val found = repository.findById(99999)

        assertThat(found).isNull()
    }
}
