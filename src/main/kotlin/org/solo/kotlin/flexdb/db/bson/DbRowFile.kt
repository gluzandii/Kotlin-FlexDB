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
import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.schemafull.Schema
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Row
import org.solo.kotlin.flexdb.db.types.*
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.JsonUtil.newBinaryObjectMapper
import java.io.IOException
import java.util.*


/**
 * Implementation of a row file (row0, row1000) in the database.
 * ```
 * {
 *      "data": {
 *          "0": {
 *              "name": "Sushant Nariangadu",
 *              "email": "nsushant2007@gmail.com"
 *          },
 *          "1": {
 *              "name": "Radhakrishnan Nariangadu",
 *              "email": "reachnrk@gmail.com"
 *          }
 *      }
 * }
 * ```
 */
@JsonDeserialize(using = DbRowDeserializer::class)
@JsonSerialize(using = DbRowSerializer::class)
data class DbRowFile(var data: TreeMap<Int, Map<String, DbValue<*>?>>) {
    val size: Int
        get() = data.size

    constructor() : this(TreeMap())

    operator fun get(id: Int): Map<String, DbValue<*>?>? {
        return data[id]
    }

    operator fun set(id: Int, mp: Map<String, DbValue<*>?>) {
        data[id] = mp
    }

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    fun toRows(schema: Schema): List<Row> {
        val l = LinkedList<Row>()
        for ((k, v) in data) {
            val r = Row(k, schema)
            for ((k1, v1) in v) {
                r[k1] = v1
            }
        }
        return l
    }

    @Throws(IOException::class)
    fun serialize(): ByteArray {
        return JsonUtil.binaryJsonSerialize(this)
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun deserialize(byte: ByteArray): DbRowFile {
            return newBinaryObjectMapper().readValue(byte, DbRowFile::class.java)
                ?: throw IOException("Could not parse row file.")
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
        val data = TreeMap<Int, Map<String, DbValue<*>?>>()
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
