package org.solo.kotlin.flexdb.json.query

data class JsonUpdatePayload(val columns: Map<String, String>, val condition: String?)
