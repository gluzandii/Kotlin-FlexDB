package org.solo.kotlin.flexdb.json.query.classes

data class JsonReset(var table: String, var action: String, var payload: JsonResetPayload) {
    constructor() : this("", "", JsonResetPayload())
}

data class JsonResetPayload(var condition: String) {
    constructor() : this("")
}
