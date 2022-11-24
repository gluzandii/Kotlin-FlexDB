package org.solo.kotlin.flexdb.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

object AsyncIOUtil {
    @Throws(IOException::class)
    suspend fun readBytes(path: Path): ByteArray {
        var exp: IOException? = null

        val bytes = withContext(Dispatchers.IO) {
            return@withContext try {
                path.readBytes()
            } catch (io: IOException) {
                exp = io
                ByteArray(0)
            }
        }
        if (exp != null) {
            throw exp!!
        }

        return bytes
    }

    @Throws(IOException::class)
    suspend fun writeBytes(path: Path, bytes: ByteArray) {
        var exp: IOException? = null
        withContext(Dispatchers.IO) {
            try {
                path.writeBytes(bytes)
            } catch (io: IOException) {
                exp = io
            }
        }

        if (exp != null) {
            throw exp!!
        }
    }

    @Throws(IOException::class)
    suspend fun deleteDirectory(path: Path) {
        var exp: IOException? = null
        withContext(Dispatchers.IO) {
            try {
                FileUtils.deleteDirectory(path.toFile())
            } catch (io: IOException) {
                exp = io
            }
        }

        if (exp != null) {
            throw exp!!
        }
    }

    @Throws(IOException::class)
    suspend fun walk(path: Path, filter: (p: Path) -> Boolean = { true }): Stream<Path> {
        var exp: IOException? = null

        val list = withContext(Dispatchers.IO) {
            return@withContext try {
                Files.walk(path)
            } catch (io: IOException) {
                exp = io
                Stream.of()
            }
        }.filter(filter)!!
        if (exp != null) {
            throw exp!!
        }

        return list
    }
}