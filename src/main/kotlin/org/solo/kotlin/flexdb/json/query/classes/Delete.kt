package org.solo.kotlin.flexdb.json.query.classes

data class JsonDelete(var table: String, var action: String, var payload: JsonDeletePayload) {
    constructor() : this("", "", JsonDeletePayload())
}

data class JsonDeletePayload(var condition: String) {
    constructor() : this("")
}
