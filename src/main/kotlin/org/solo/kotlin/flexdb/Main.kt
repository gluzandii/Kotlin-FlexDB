package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DbUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

@SpringBootApplication
class FlexDbApplication

@RestController

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

        val name = Path(readln())
        val pswd = System.console()?.readPassword()?.let { String(it) } ?: readln()

        GlobalData.pswd = pswd

        if (DbUtil.dbExists(name)) {
            DbUtil.setGlobalDB(name, "s")
        } else if (name.exists()) {
            println("Something exists at the path: $name, please delete it.")

            println("Exiting...")
            exitProcess(1)
        } else {
            print("Create db at: $name (y/N): ")
            val input = System.`in`.read().toChar()
            
            if (input == 'Y' || input == 'y') {
                DbUtil.createDB(name, "s")
            } else {
                println("Exiting...")
                exitProcess(1)
            }
        }
        runApplication<FlexDbApplication>(*args)
    } catch (ex: Throwable) {
        println("An error occurred.")
        ex.printStackTrace()
    }
}
