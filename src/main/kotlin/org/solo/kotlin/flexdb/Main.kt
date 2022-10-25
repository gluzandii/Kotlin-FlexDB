package org.solo.kotlin.flexdb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@SpringBootApplication
class FlexDbApplication

@RestController
@Suppress("unused")
class RestControl {
    @GetMapping("/")
    fun root() = mapOf(Pair("status", "OK"))

    @PostMapping("/")
    fun query(req: HttpServletRequest) {

    }
}

fun main(args: Array<String>) {
    try {
        print("Enter DB to open (Absolute path): ")
//        val name = Path(readln())

//        createDB(name, "s")
//        setGlobalDB(name, "s")
        runApplication<FlexDbApplication>(*args)
    } catch (ex: Exception) {
        println("An error occurred.")
        ex.printStackTrace()
    }
}
