package org.solo.kotlin.flexdb.json.query.classes

@Suppress("unused")
data class JsonReset(var tableName: String, var action: String, var payload: JsonResetPayload) {
    constructor() : this("", "", JsonResetPayload())
}

data class JsonResetPayload(var condition: String) {
    constructor() : this("")
}
