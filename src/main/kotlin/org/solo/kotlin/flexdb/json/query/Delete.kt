package org.solo.kotlin.flexdb.json.query

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = JsonDeleteDeserializer::class)
data class JsonDelete(val table: String, val action: String, val payload: JsonDeletePayload)

data class JsonDeletePayload(val condition: String)

class JsonDeleteDeserializer : JsonDeserializer<JsonDelete>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonDelete {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = getTableAndAction(node)
        val cond = node["payload"]!!["condition"]!!.asText()

        return JsonDelete(table, action, JsonDeletePayload(cond))
    }
}
