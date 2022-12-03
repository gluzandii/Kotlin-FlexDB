package org.solo.kotlin.flexdb.plugins.sockets

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.Socket

/**
 * A [FlexDBSocketInputStream] is an [InputStream] that reads bytes from a socket.
 *
 * It has some nice utility methods for reading from a socket,
 * for the FlexDB database.
 */
class FlexDBSocketInputStream(
    /**
     * The socket to read from.
     */
    sock: Socket,
) : InputStream() {
    /**
     * The underlying input stream.
     */
    private val readFrom: InputStream

    init {
        readFrom = sock.getInputStream()
    }

    /**
     * Reads an [Int] from the [readFrom] `InputStream` and returns it.
     */
    @Throws(IOException::class)
    override fun read(): Int {
        return readFrom.read()
    }

    /**
     * Closes this input stream and releases any system resources associated with the stream.
     */
    @Throws(IOException::class)
    override fun close() {
        readFrom.close()
    }

    /**
     * Reads from the current [InputStreamReader] until a NUL character is found.
     * ASCII value = 0
     */
    @Throws(IOException::class)
    inline fun readUntilNull(
        @Suppress("UNUSED_PARAMETER") func: () -> Unit = {},
    ): String {
        val sb = StringBuilder()
        var c = read()

        while (c != 0) {
            sb.append(c.toChar())
            c = read()
        }
        return sb.toString()
    }
}