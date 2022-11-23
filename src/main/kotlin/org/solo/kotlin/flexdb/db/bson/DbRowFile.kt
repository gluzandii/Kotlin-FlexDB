package org.solo.kotlin.flexdb.db.bson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import org.solo.kotlin.flexdb.db.types.*
import org.solo.kotlin.flexdb.internal.Binary
import org.solo.kotlin.flexdb.json.JsonUtil
import java.io.IOException
import java.io.OutputStream
import java.util.*

@Suppress("unused")
@JsonDeserialize(using = DbRowDeserializer::class)
@JsonSerialize(using = DbRowSerializer::class)
data class DbRowFile(var data: TreeMap<Int, HashMap<String, DbValue<*>?>>) : Binary {
    val size: Int
        get() = data.size

    constructor() : this(TreeMap())

    operator fun get(id: Int): HashMap<String, DbValue<*>?>? {
        return data[id]
    }

    operator fun set(id: Int, mp: HashMap<String, DbValue<*>?>) {
        data[id] = mp
    }

    @Throws(IOException::class)
    override fun writeBinary(out: OutputStream) {
        val mapper = JsonUtil.newBinaryObjectMapper()
        mapper.writeValue(out, this)
    }

    companion object {
        @JvmStatic
        fun deserialize(byte: ByteArray): DbRowFile {
            val mapper = JsonUtil.newBinaryObjectMapper()
            return mapper.readValue(byte, DbRowFile::class.java)
        }
    }
}

class DbRowSerializer : JsonSerializer<DbRowFile>() {
    override fun serialize(row: DbRowFile, gen: JsonGenerator, serializers: SerializerProvider) {
        val d = TreeMap<String, HashMap<String, Any?>>()

        for ((key, value) in row.data) {
            val hm = hashMapOf<String, Any?>()
            for ((k, v) in value) {
                hm[k] = v!!.value
            }


            d[key.toString()] = hm
        }

        gen.writeStartObject()
        gen.writeObjectField("data", d)
        gen.writeEndObject()
    }
}

class DbRowDeserializer : JsonDeserializer<DbRowFile>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DbRowFile {
        val data = TreeMap<Int, HashMap<String, DbValue<*>?>>()
        val node = p.codec.readTree<ObjectNode>(p)!!

        for ((key, value) in node["data"]!!.fields()!!) {
            val columnData = HashMap<String, DbValue<*>?>()

            for ((k, v) in value.fields()!!) {
                when (v.nodeType) {
                    JsonNodeType.STRING -> {
                        columnData[k] = DbString(v.asText())
                    }

                    JsonNodeType.NUMBER -> {
                        columnData[k] = if (v.isShort || v.isInt || v.isLong) {
                            DbNumber(v.asLong())
                        } else {
                            DbDecimal(v.asDouble())
                        }
                    }

                    JsonNodeType.BOOLEAN -> {
                        columnData[k] = DbBoolean(v.asBoolean())
                    }

                    JsonNodeType.NULL -> {
                        columnData[k] = null
                    }

                    else -> {}
                }
            }

            data[key.toInt()] = columnData
        }
        return DbRowFile(data)
    }
}
