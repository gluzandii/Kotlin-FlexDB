package org.solo.kotlin.flexdb.json.query.classes

data class JsonDelete(val table: String, val action: String, val payload: JsonDeletePayload) {
    constructor() : this("", "", JsonDeletePayload())
}

data class JsonDeletePayload(val condition: String) {
    constructor() : this("")
}
