package org.solo.kotlin.flexdb.json.query.classes

data class JsonReset(val table: String, val action: String, val payload: JsonResetPayload) {
    constructor() : this("", "", JsonResetPayload())
}

data class JsonResetPayload(val condition: String) {
    constructor() : this("")
}
