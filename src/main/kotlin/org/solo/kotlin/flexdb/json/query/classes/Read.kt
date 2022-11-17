package org.solo.kotlin.flexdb.json.query.classes

data class JsonRead(var table: String, var action: String, var payload: JsonReadPayload) {
    constructor() : this("", "", JsonReadPayload())
}

data class JsonReadPayload(var columns: Set<String>, var condition: String?) {
    constructor() : this(setOf(), null)
}
