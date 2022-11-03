package org.solo.kotlin.flexdb.json

import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import de.undercouch.bson4jackson.BsonGenerator

object JsonUtil {
    @JvmStatic
    private val bson: BsonFactory = BsonFactory()

    init {
        bson.enable(BsonGenerator.Feature.ENABLE_STREAMING)
    }

    @JvmStatic
    fun binaryObjectMapper(): ObjectMapper {
        return ObjectMapper(bson)
    }
}