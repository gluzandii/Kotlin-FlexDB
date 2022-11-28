package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.DbUtil
import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes
import org.solo.kotlin.flexdb.json.query.JsonQueryUtil
import org.solo.kotlin.flexdb.json.query.classes.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

private lateinit var db: DB

@SpringBootApplication
class FlexDbApplication

@RestController
class DbRestController {
    @GetMapping("/")
    fun root(): Map<String, String> {
        return mapOf("status" to "OK")
    }

    @PostMapping("/query")
    fun query(req: HttpServletRequest) {
        // Structure of queries has been put in the "when" statement
        // It will be implemented at the end.

        try {
            val body = req.reader.readText()
            val mapper = newObjectMapper()

            when (JsonQueryUtil.getQueryType(body)) {
                JsonQueryTypes.CREATE -> {
                    val query = mapper.readValue(body, JsonCreateQuery::class.java)!!
                }

                JsonQueryTypes.SELECT -> {
                    val query = mapper.readValue(body, JsonSelectQuery::class.java)!!
                }

                JsonQueryTypes.UPDATE -> {
                    val query = mapper.readValue(body, JsonUpdateQuery::class.java)!!
                }

                JsonQueryTypes.DELETE -> {
                    val query = mapper.readValue(body, JsonDeleteQuery::class.java)!!
                }

                JsonQueryTypes.RESET -> {
                    val query = mapper.readValue(body, JsonResetQuery::class.java)!!
                }
            }
        } catch (ex: Exception) {
            System.err.println("An error occurred:")
            ex.printStackTrace()
        }
    }
}

/**
 * Reads password from stdin using [java.io.Console].
 * If [java.io.Console] from [System] is null, it will read from [System. in]
 *
 * @return password read from stdin or [java.io.Console] stdin
 */
@Suppress("unused")
fun readPassword(): String {
    return String((System.console() ?: return readln()).readPassword()!!)
}

fun main(args: Array<String>) {
    try {
        print("Enter DB to open (Absolute path): ")

        val name = Path(readln())

        if (DbUtil.dbExists(name)) {
            db = DB(name)
        } else if (name.exists()) {
            println("Something exists at the path: $name, please delete it.")

            println("Exiting...")
            exitProcess(1)
        } else {
            print("Create db at: $name (y/N): ")
            val input = System.`in`.read().toChar()

            if ((input == 'Y') || (input == 'y')) {
                db = DbUtil.createDB(name) ?: throw IOException("Could not create DB at: $name")
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
