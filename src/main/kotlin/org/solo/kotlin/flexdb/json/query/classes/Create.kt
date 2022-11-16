package org.solo.kotlin.flexdb.json.query.classes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

typealias JsonColumns = HashMap<String, JsonColumn>

data class JsonColumn(val type: String, val constraints: Set<String>)

@JsonDeserialize(using = JsonCreateDeserializer::class)
data class JsonCreate(val table: String, val action: String, val payload: JsonColumns)

fun deserializeJsonColumn(p: JsonNode, columns: JsonColumns) {
    p.fieldNames().forEach { pNode ->
        val v = pNode!!
        val field = p[v]!!
        val type = field["type"]!!.asText()!!
        val cons = field["constraints"]!!.asIterable().map {
            return@map it!!.asText()!!
        }.toSet()

        columns[v] = JsonColumn(type, cons)
    }
}

class JsonCreateDeserializer : JsonDeserializer<JsonCreate>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): JsonCreate {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val (table, action) = QueryUtil.getTableAndAction(node)
        val p = node["payload"]!!

        val payload = JsonColumns()
        deserializeJsonColumn(p, payload)

        return JsonCreate(table, action, payload)
    }
}
