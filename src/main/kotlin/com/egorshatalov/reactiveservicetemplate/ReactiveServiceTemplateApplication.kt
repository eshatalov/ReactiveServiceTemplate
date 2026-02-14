package com.egorshatalov.reactiveservicetemplate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveServiceTemplateApplication

fun main(args: Array<String>) {
    runApplication<ReactiveServiceTemplateApplication>(*args)
}
