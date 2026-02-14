package com.egorshatalov.reactiveservicetemplate

import com.egorshatalov.reactiveservicetemplate.config.TestDatabaseConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestDatabaseConfig::class)
abstract class AbstractContextTest {

    @Test
    fun contextLoads() {
    }

}
