package org.solo.kotlin.flexdb.json.query

import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper

/**
 * Class for parsing the JSON query, and utilities regarding that.
 */
object JsonQueryUtil {
    /**
     * Returns the type of query, from the JSON query.
     */
    @JvmStatic
    fun getQueryType(input: String): JsonQueryTypes {
        val map = newObjectMapper()
        val mp = map.readValue(input, HashMap::class.java)
        val action = mp["action"]!!.toString()

        return JsonQueryTypes.valueOf(action.uppercase())
    }
}
