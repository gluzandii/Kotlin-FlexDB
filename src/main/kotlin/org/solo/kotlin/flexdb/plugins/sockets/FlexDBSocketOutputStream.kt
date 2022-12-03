package org.solo.kotlin.flexdb.plugins.sockets

import java.io.IOException
import java.io.OutputStream
import java.net.Socket

inline fun CharSequence.append(
    int: Int,
    @Suppress("UNUSED_PARAMETER") func: () -> Unit = { },
): ByteArray {
    return (this.toString() + int.toChar()).toByteArray()
}

/**
 * A [FlexDBSocketOutputStream] is an [OutputStream] that writes bytes to a socket.
 *
 * It has some nice utility methods for writing to a socket,
 * for the FlexDB database.
 */
class FlexDBSocketOutputStream(
    /**
     * The socket to write to.
     */
    sock: Socket,
) : OutputStream() {
    /**
     * The underlying output stream.
     */
    private val writeTo: OutputStream

    init {
        writeTo = sock.getOutputStream()
    }

    /**
     * Writes the specified [Int] to this output stream.
     */
    @Throws(IOException::class)
    override fun write(b: Int) {
        writeTo.write(b)
    }

    /**
     * Closes this output stream and releases any system resources associated with this stream.
     */
    @Throws(IOException::class)
    override fun close() {
        writeTo.flush()
        writeTo.close()
    }

    @Throws(IOException::class)
    fun writeAndEndWithNull(str: CharSequence) {
        try {
            write(str.append(0))
        } finally {
            writeTo.flush()
        }
    }
}