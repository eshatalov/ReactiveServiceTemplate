package com.github.template

import com.github.database.rider.core.api.configuration.DBUnit
import com.github.database.rider.spring.api.DBRider
import com.github.template.config.JsonbDataTypeFactory
import com.github.template.config.TestDatabaseConfig
import com.github.template.config.TestFlywayConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestDatabaseConfig::class, TestFlywayConfig::class)
@DBRider(dataSourceBeanName = "dataSource")
@DBUnit(dataTypeFactoryClass = JsonbDataTypeFactory::class, schema = "template")
abstract class AbstractContextTest
