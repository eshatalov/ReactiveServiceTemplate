package com.egorshatalov.reactiveservicetemplate.testtable.handler

import com.egorshatalov.reactiveservicetemplate.testtable.model.TestTableResponse
import com.egorshatalov.reactiveservicetemplate.testtable.service.NotFoundException
import com.egorshatalov.reactiveservicetemplate.testtable.service.TestTableService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.OffsetDateTime

class TestTableHandlerTest {

    private lateinit var webTestClient: WebTestClient
    private lateinit var service: TestTableService

    @BeforeEach
    fun setUp() {
        service = mockk()
        val handler = TestTableHandler(service)
        val router = TestTableRouter()
        val routerFunction = router.testTableRoutes(handler)

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction)
            .configureClient()
            .baseUrl("/api/test-tables")
            .build()
    }

    @Test
    fun `GET should return all items`() {
        val now = OffsetDateTime.now()
        val response1 = TestTableResponse(1, "Item 1", now, now)
        val response2 = TestTableResponse(2, "Item 2", now, now)
        coEvery { service.findAll() } returns flowOf(response1, response2)

        webTestClient.get()
            .uri("")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].id").isEqualTo(1)
            .jsonPath("$[0].name").isEqualTo("Item 1")
            .jsonPath("$[1].id").isEqualTo(2)
            .jsonPath("$[1].name").isEqualTo("Item 2")
    }

    @Test
    fun `GET by id should return item`() {
        val now = OffsetDateTime.now()
        val response = TestTableResponse(1, "Test Item", now, now)
        coEvery { service.findById(1) } returns response

        webTestClient.get()
            .uri("/1")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Test Item")
    }

    @Test
    fun `GET by id should return 404 when not found`() {
        coEvery { service.findById(999) } throws NotFoundException("TestTable with id 999 not found")

        webTestClient.get()
            .uri("/999")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST should create item and return 201`() {
        val now = OffsetDateTime.now()
        val response = TestTableResponse(1, "New Item", now, now)
        coEvery { service.create(any()) } returns response

        webTestClient.post()
            .uri("")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"New Item"}""")
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("New Item")
    }

    @Test
    fun `PUT should update item and return 200`() {
        val now = OffsetDateTime.now()
        val response = TestTableResponse(1, "Updated Item", now, now)
        coEvery { service.update(1, any()) } returns response

        webTestClient.put()
            .uri("/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Updated Item"}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(1)
            .jsonPath("$.name").isEqualTo("Updated Item")
    }

    @Test
    fun `PUT should return 404 when updating non-existent item`() {
        coEvery { service.update(999, any()) } throws NotFoundException("TestTable with id 999 not found")

        webTestClient.put()
            .uri("/999")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Updated Item"}""")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `DELETE should delete item and return 204`() {
        coEvery { service.delete(1) } returns Unit

        webTestClient.delete()
            .uri("/1")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `DELETE should return 404 when deleting non-existent item`() {
        coEvery { service.delete(999) } throws NotFoundException("TestTable with id 999 not found")

        webTestClient.delete()
            .uri("/999")
            .exchange()
            .expectStatus().isNotFound
    }
}
