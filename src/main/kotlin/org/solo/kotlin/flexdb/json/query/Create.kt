package org.solo.kotlin.flexdb.json.query

typealias JsonCreatePayload = Map<String, JsonColumn>

data class JsonColumn(val type: String, val constraints: Set<String>)
