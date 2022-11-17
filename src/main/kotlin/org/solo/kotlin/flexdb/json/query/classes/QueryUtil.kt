package org.solo.kotlin.flexdb.json.query.classes

import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes

object QueryUtil {
    fun getAction(input: String): JsonQueryTypes {
        val map = newObjectMapper()
        val mp = map.readValue(input, HashMap::class.java)
        val action = mp["action"]!!.toString()

        return JsonQueryTypes.valueOf(action.uppercase())
    }
}
