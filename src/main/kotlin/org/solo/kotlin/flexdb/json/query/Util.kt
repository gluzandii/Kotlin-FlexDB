package org.solo.kotlin.flexdb.json.query

import com.fasterxml.jackson.databind.JsonNode

internal inline fun getTableAndAction(p: JsonNode): Pair<String, String> {
    val table = p["table"]!!.asText()
    val action = p["action"]!!.asText()

    return Pair(table, action)
}
