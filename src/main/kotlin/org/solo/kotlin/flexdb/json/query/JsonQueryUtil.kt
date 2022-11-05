package org.solo.kotlin.flexdb.json.query

import com.fasterxml.jackson.databind.ObjectMapper

object JsonQueryUtil {
    @JvmStatic
    private val mapper = ObjectMapper()

    @JvmStatic
    fun serializeJsonCreate(body: String): JsonCreate {
        return mapper.readValue(body, JsonCreate::class.java)
    }

    @JvmStatic
    fun deserializeJsonCreate(body: JsonCreate): String {
        return mapper.writeValueAsString(body)
    }
}
