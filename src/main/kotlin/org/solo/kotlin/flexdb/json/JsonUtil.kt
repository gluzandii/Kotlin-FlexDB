package org.solo.kotlin.flexdb.json

import com.fasterxml.jackson.core.exc.StreamWriteException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

object JsonUtil {
    @JvmStatic
    val bson: BsonFactory = BsonFactory()

    init {
        bson.enable(BsonGenerator.Feature.ENABLE_STREAMING)
    }

    @JvmStatic
    fun newBinaryObjectMapper(): ObjectMapper {
        return ObjectMapper(bson)
    }

    @JvmStatic
    fun newObjectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @JvmStatic
    @Throws(IOException::class, StreamWriteException::class, DatabindException::class)
    suspend fun binaryJsonSerialize(obj: Any): ByteArray {
        var exp: IOException? = null
        val b = withContext(Dispatchers.Default) {
            return@withContext try {
                val mapper = newBinaryObjectMapper()
                val bytes = ByteArrayOutputStream()
                mapper.writeValue(bytes, obj)

                bytes.toByteArray()
            } catch (io: IOException) {
                exp = io
                null
            }
        }
        if (exp != null) {
            throw exp!!
        }

        return b ?: throw IOException("Could not serialize this rowc")
    }
}