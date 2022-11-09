package org.solo.kotlin.flexdb.json.query.classes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = JsonReadDeserializer::class)
data class JsonRead(val table: String, val action: String, val payload: JsonReadPayload)

data class JsonReadPayload(val columns: Set<String>, val condition: String?)

class JsonReadDeserializer : JsonDeserializer<JsonRead>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonRead {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = QueryUtil.getTableAndAction(node)

        val p = node["payload"]!!
        val cols = p["columns"]!!.asIterable().map {
            return@map it!!.asText()!!
        }.toSet()

        var cond: String? = null
        if (p.has("condition")) {
            cond = p["condition"]!!.asText()
        }

        return JsonRead(table, action, JsonReadPayload(cols, cond))
    }
}
