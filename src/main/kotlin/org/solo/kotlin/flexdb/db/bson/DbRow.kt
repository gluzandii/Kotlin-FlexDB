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

@Suppress("unused")
@JsonDeserialize(using = DbRowDeserializer::class)
@JsonSerialize(using = DbRowSerializer::class)
data class DbRow(var data: LinkedHashMap<String, HashMap<String, DbValue<*>?>>)

class DbRowSerializer : JsonSerializer<DbRow>() {
    override fun serialize(row: DbRow, gen: JsonGenerator, serializers: SerializerProvider) {
        val d = linkedMapOf<String, HashMap<String, Any?>>()

        for ((key, value) in row.data) {
            val hm = hashMapOf<String, Any?>()
            for ((k, v) in value) {
                hm[k] = v!!.value
            }

            d[key] = hm
        }

        gen.writeStartObject()
        gen.writeObjectField("data", d)
        gen.writeEndObject()
    }
}

class DbRowDeserializer : JsonDeserializer<DbRow>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DbRow {
        val data = LinkedHashMap<String, HashMap<String, DbValue<*>?>>()
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

            data[key] = columnData
        }
        return DbRow(data)
    }
}
