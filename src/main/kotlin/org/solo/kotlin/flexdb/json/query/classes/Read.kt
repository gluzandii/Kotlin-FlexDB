package org.solo.kotlin.flexdb.json.query.classes

@Suppress("unused")
data class JsonRead(var tableName: String, var action: String, var payload: JsonReadPayload) {
    constructor() : this("", "", JsonReadPayload())
}

data class JsonReadPayload(var columns: Set<String>, var condition: String?) {
    constructor() : this(setOf(), null)
}
