package org.solo.kotlin.flexdb.json.query.classes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = JsonUpdateDeserializer::class)
data class JsonUpdate(val table: String, val action: String, val payload: JsonUpdatePayload)

data class JsonUpdatePayload(val columns: Map<String, String>, val condition: String?)

class JsonUpdateDeserializer : JsonDeserializer<JsonUpdate>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonUpdate {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = QueryUtil.getTableAndAction(node)

        val p = node["payload"]!!
        val cs = p["columns"]!!

        val cols = hashMapOf<String, String>()
        val cond = p["condition"]!!.asText()!!

        cs.fieldNames().forEach { v ->
            val name = v!!
            val value = cs[name]!!.asText()!!

            cols[name] = value
        }

        return JsonUpdate(table, action, JsonUpdatePayload(cols, cond))
    }
}
