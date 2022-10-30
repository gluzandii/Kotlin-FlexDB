package org.solo.kotlin.flexdb.json

import com.fasterxml.jackson.core.exc.StreamWriteException
import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import java.io.ByteArrayOutputStream
import java.io.IOException

object DbJsonUtil {
//    @JvmStatic
//    val bson = BsonFactory()

    @JvmStatic
    val mapper: ObjectMapper

    init {
//        bson.enable(BsonGenerator.Feature.ENABLE_STREAMING)
        mapper = ObjectMapper(/*bson*/)
    }

    @JvmStatic
    @Throws(IOException::class, StreamWriteException::class, DatabindException::class)
    fun serializeColumn(c: Set<Column>): ByteArray {
        val bytes = ByteArrayOutputStream()
        val set = hashSetOf<InternalColumn>()

        for (i in c) {
            set.add(InternalUtil.toInternalColumn(i))
        }

        mapper.writeValue(bytes, mapOf(Pair("columns", set)))
        return bytes.toByteArray()
    }

    @JvmStatic
    @Throws(IOException::class, StreamWriteException::class, DatabindException::class)
    fun deserializeColumn(c: ByteArray): Set<Column> {
        val set = hashSetOf<Column>()
        val mp = mapper.readValue(c, HashMap::class.java) as HashMap<String, Set<InternalColumn>>

        for (i in mp["columns"] ?: return set) {
            set.add(InternalUtil.toColumn(i))
        }

        return set
    }
}

