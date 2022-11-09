package org.solo.kotlin.flexdb.json.query.classes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = JsonResetDeserializer::class)
data class JsonReset(val table: String, val action: String, val payload: JsonResetPayload)

data class JsonResetPayload(val condition: String)

class JsonResetDeserializer : JsonDeserializer<JsonReset>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonReset {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = QueryUtil.getTableAndAction(node)
        val cond = node["payload"]!!["condition"]!!.asText()

        return JsonReset(table, action, JsonResetPayload(cond))
    }
}
