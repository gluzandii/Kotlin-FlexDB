package org.solo.kotlin.flexdb.json.query.classes

data class JsonUpdate(val table: String, val action: String, val payload: JsonUpdatePayload) {
    constructor() : this("", "", JsonUpdatePayload())
}

data class JsonUpdatePayload(val columns: Map<String, String>, val condition: String?) {
    constructor() : this(mapOf(), null)
}
