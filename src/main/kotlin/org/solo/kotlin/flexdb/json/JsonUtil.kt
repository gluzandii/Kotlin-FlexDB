package org.solo.kotlin.flexdb.json

import com.fasterxml.jackson.core.exc.StreamWriteException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator
import java.io.ByteArrayOutputStream
import java.io.IOException

object JsonUtil {
    @JvmStatic
    val bson: BsonFactory = BsonFactory()

    init {
        bson.enable(BsonGenerator.Feature.ENABLE_STREAMING)
    }

    @JvmStatic
    inline fun binaryObjectMapper(): ObjectMapper {
        return ObjectMapper(bson)
    }

    @JvmStatic
    @Throws(IOException::class, StreamWriteException::class, DatabindException::class)
    fun <T, V> binaryJsonSerialize(map: Map<T, V>): ByteArray {
        val b = ByteArrayOutputStream()

        val mp = binaryObjectMapper()
        mp.writeValue(b, map)

        return b.toByteArray()
    }
}