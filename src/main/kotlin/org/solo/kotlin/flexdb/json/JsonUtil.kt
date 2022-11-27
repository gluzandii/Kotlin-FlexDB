package org.solo.kotlin.flexdb.json

import com.fasterxml.jackson.core.exc.StreamWriteException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Utilities for creating [ObjectMapper] for normal and binary serialization.
 *
 * It can also serialize objects into BSON.
 */
object JsonUtil {
    /**
     * The global [BsonFactory] instance, used for creating binary [ObjectMapper] instances.
     */
    @JvmStatic
    val bson: BsonFactory = BsonFactory()

    init {
        bson.enable(BsonGenerator.Feature.ENABLE_STREAMING)
    }

    /**
     * Creates a new [ObjectMapper] instance for normal `JSON` serialization.
     */
    @JvmStatic
    fun newBinaryObjectMapper(): ObjectMapper {
        return ObjectMapper(bson)
    }

    /**
     * Creates a new [ObjectMapper] instance for normal `JSON` serialization.
     */
    @JvmStatic
    fun newObjectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    /**
     * Serializes the given object into a `BSON` byte array.
     *
     * @param obj The object to serialize into `BSON`.
     */
    @JvmStatic
    @Throws(IOException::class, StreamWriteException::class, DatabindException::class)
    fun binaryJsonSerialize(obj: Any): ByteArray {
        val mapper = newBinaryObjectMapper()
        val bytes = ByteArrayOutputStream()
        mapper.writeValue(bytes, obj)

        return bytes.toByteArray() ?: throw IOException("Could not serialize this row")
    }
}