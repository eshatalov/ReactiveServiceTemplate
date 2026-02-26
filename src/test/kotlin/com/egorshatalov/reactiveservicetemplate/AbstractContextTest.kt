package com.egorshatalov.reactiveservicetemplate

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.spring.api.DBRider
import com.egorshatalov.reactiveservicetemplate.config.JsonbDataTypeFactory
import com.egorshatalov.reactiveservicetemplate.config.TestDatabaseConfig
import com.egorshatalov.reactiveservicetemplate.config.TestFlywayConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestDatabaseConfig::class, TestFlywayConfig::class)
@DBRider(dataSourceBeanName = "dataSource")
@DBUnit(dataTypeFactoryClass = JsonbDataTypeFactory::class)
abstract class AbstractContextTest
