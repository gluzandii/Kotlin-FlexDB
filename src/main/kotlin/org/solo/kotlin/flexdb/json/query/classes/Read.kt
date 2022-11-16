package org.solo.kotlin.flexdb.json.query.classes

data class JsonRead(val table: String, val action: String, val payload: JsonReadPayload) {
    constructor() : this("", "", JsonReadPayload())
}

data class JsonReadPayload(val columns: Set<String>, val condition: String?) {
    constructor() : this(setOf(), null)
}
