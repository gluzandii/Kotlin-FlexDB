package org.solo.kotlin.flexdb

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.DbUtil
import org.solo.kotlin.flexdb.plugins.configureRouting
import org.solo.kotlin.flexdb.plugins.configureSecurity
import org.solo.kotlin.flexdb.plugins.configureSerialization
import org.solo.kotlin.flexdb.plugins.configureSockets
import java.io.IOException
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

lateinit var db: DB

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

fun main() {
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

        val serverThread = thread(
            name = "socket server thread~1",
            priority = Thread.MAX_PRIORITY
        ) {
            configureSockets()
        }

        embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
            .start(wait = true)

        serverThread.join()
    } catch (ex: Throwable) {
        println("An error occurred.")
        ex.printStackTrace()
    }
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureRouting()
}
