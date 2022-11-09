package org.solo.kotlin.flexdb.json.query

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

typealias JsonCreatePayload = HashMap<String, JsonColumn>

data class JsonColumn(val type: String, val constraints: Set<String>)

@JsonDeserialize(using = JsonCreateDeserializer::class)
data class JsonCreate(val table: String, val action: String, val payload: JsonCreatePayload)

class JsonCreateDeserializer : JsonDeserializer<JsonCreate>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonCreate {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = getTableAndAction(node)
        val p = node["payload"]!!

        val payload = JsonCreatePayload()

        p.fieldNames().forEach { pNode ->
            val v = pNode!!
            val field = p[v]!!
            val type = field["type"]!!.asText()!!
            val cons = field["constraints"]!!.asIterable().map {
                return@map it!!.asText()!!
            }.toSet()

            payload[v] = JsonColumn(type, cons)
        }

        return JsonCreate(table, action, payload)
    }
}
