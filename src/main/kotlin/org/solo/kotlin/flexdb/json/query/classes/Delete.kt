package org.solo.kotlin.flexdb.json.query.classes

@Suppress("unused")
data class JsonDelete(var tableName: String, var action: String, var payload: JsonDeletePayload) {
    constructor() : this("", "", JsonDeletePayload())
}

data class JsonDeletePayload(var condition: String) {
    constructor() : this("")
}
