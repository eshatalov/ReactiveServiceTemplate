package com.egorshatalov.reactiveservicetemplate.testtable.service

import com.egorshatalov.reactiveservicetemplate.testtable.model.CreateTestTableRequest
import com.egorshatalov.reactiveservicetemplate.testtable.model.TestTableResponse
import com.egorshatalov.reactiveservicetemplate.testtable.model.UpdateTestTableRequest
import com.egorshatalov.reactiveservicetemplate.testtable.model.toResponse
import com.egorshatalov.reactiveservicetemplate.testtable.repository.TestTableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TestTableService(
    private val repository: TestTableRepository
) {

    @Transactional(readOnly = true)
    fun findAll(): Flow<TestTableResponse> {
        return repository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    suspend fun findById(id: Long): TestTableResponse {
        return repository.findById(id)
            ?.toResponse()
            ?: throw NotFoundException("TestTable with id $id not found")
    }

    @Transactional
    suspend fun create(request: CreateTestTableRequest): TestTableResponse {
        return repository.insert(request.name).toResponse()
    }

    @Transactional
    suspend fun update(id: Long, request: UpdateTestTableRequest): TestTableResponse {
        return repository.update(id, request.name)
            ?.toResponse()
            ?: throw NotFoundException("TestTable with id $id not found")
    }

    @Transactional
    suspend fun delete(id: Long) {
        if (!repository.deleteById(id)) {
            throw NotFoundException("TestTable with id $id not found")
        }
    }
}
