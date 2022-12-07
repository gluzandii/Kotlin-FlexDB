package org.solo.kotlin.flexdb.plugins

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

val selectorManager = ActorSelectorManager(Dispatchers.IO)
const val DefaultPort = 9002

@Suppress("UNUSED_VARIABLE")
@Throws(Throwable::class)
fun configureSockets() = runBlocking {
    val serverSocket = aSocket(selectorManager)
        .tcp()
        .bind(hostname = "127.0.0.1", port = DefaultPort)

    println("Server listening at ${serverSocket.localAddress}")

    serverSocket.accept().use { socket ->
        socket.openWriteChannel(autoFlush = true).use {
            val read = socket.openReadChannel()

            try {
                while (true) {
                    val line = read.readUTF8Line() ?: ""
                }
            } catch (e: Throwable) {
                System.err.println("An error occurred in the socket thread: ${e.message}")
                System.err.println("Thread: ${Thread.currentThread()}")

                e.printStackTrace()
            }
        }
    }
}