package org.solo.kotlin.flexdb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlexDbApplication

fun main(args: Array<String>) {
    runApplication<FlexDbApplication>(*args)
}
