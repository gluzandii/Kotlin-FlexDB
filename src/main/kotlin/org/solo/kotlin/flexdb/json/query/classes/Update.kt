package org.solo.kotlin.flexdb.json.query.classes

data class JsonUpdate(var table: String, var action: String, var payload: JsonUpdatePayload) {
    constructor() : this("", "", JsonUpdatePayload())
}

data class JsonUpdatePayload(var columns: Map<String, String>, var condition: String?) {
    constructor() : this(mapOf(), null)
}
