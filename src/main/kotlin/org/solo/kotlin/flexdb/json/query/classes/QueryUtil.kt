package org.solo.kotlin.flexdb.json.query.classes

import com.fasterxml.jackson.databind.JsonNode
import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes

object QueryUtil {
    internal inline fun getTableAndAction(p: JsonNode): Pair<String, String> {
        val table = p["table"]!!.asText()
        val action = p["action"]!!.asText()

        return Pair(table, action)
    }

    fun getAction(input: String): JsonQueryTypes {
        val map = newObjectMapper()
        val mp = map.readValue(input, HashMap::class.java) as HashMap<String, Any>
        val action = mp["action"]!!.toString()

        return JsonQueryTypes.valueOf(action.uppercase())
    }
}
