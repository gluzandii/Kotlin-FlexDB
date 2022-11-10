package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DbUtil
import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes
import org.solo.kotlin.flexdb.json.query.classes.*
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
class DbRestController {
    @GetMapping("/")
    fun root(): Map<String, String> {
        return mapOf(Pair("status", "OK"))
    }

    @PostMapping("/")
    fun query(req: HttpServletRequest) {
        try {
            val body = req.reader.readText()
            val mapper = newObjectMapper()

            when (QueryUtil.getAction(body)) {
                JsonQueryTypes.CREATE -> {
                    val query = mapper.readValue(body, JsonCreate::class.java)!!
                }

                JsonQueryTypes.READ -> {
                    val query = mapper.readValue(body, JsonRead::class.java)!!
                }

                JsonQueryTypes.UPDATE -> {
                    val query = mapper.readValue(body, JsonUpdate::class.java)!!
                }

                JsonQueryTypes.DELETE -> {
                    val query = mapper.readValue(body, JsonDelete::class.java)!!
                }

                JsonQueryTypes.RESET -> {
                    val query = mapper.readValue(body, JsonReset::class.java)!!
                }
            }
        } catch (ex: Exception) {
            System.err.println("An error occurred:")
            ex.printStackTrace()
        }
    }
}

inline fun readPassword(): String {
    return String((System.console() ?: return readln()).readPassword()!!)
}

fun main(args: Array<String>) {
    try {
        print("Enter DB to open (Absolute path): ")

        val name = Path(readln())

        print("Enter password: ")
        val pswd = readPassword()

        GlobalData.pswd = pswd

        if (DbUtil.dbExists(name)) {
            DbUtil.setGlobalDB(name)
        } else if (name.exists()) {
            println("Something exists at the path: $name, please delete it.")

            println("Exiting...")
            exitProcess(1)
        } else {
            print("Create db at: $name (y/N): ")
            val input = System.`in`.read().toChar()

            if ((input == 'Y') or (input == 'y')) {
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
