package org.solo.kotlin.flexdb.plugins.sockets

import java.io.IOException
import java.net.ServerSocket
import java.time.LocalDateTime
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextInt

@Throws(IOException::class)
fun configureSockets(
    port: Int = Random(
        LocalDateTime.now().nano
    ).nextInt(1000..9999),
): Thread {
    return thread(
        name = "Multi_FlexDB_Sync_Thread",
        priority = Thread.MIN_PRIORITY
    ) {
        ServerSocket(port, 1).use { server ->
            server.accept()!!.use { socket ->
                FlexDBSocketOutputStream(socket).use { out ->
                    FlexDBSocketInputStream(socket).use { `in` ->
                        while (true) {
                            val input = `in`.readUntilNull()
                            if (input.equals("exit", true)) {
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}
