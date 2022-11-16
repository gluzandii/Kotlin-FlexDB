package org.solo.kotlin.flexdb.json.query.classes

typealias JsonColumns = HashMap<String, JsonColumn>

data class JsonColumn(var type: String, var constraints: Set<String>) {
    constructor() : this("", setOf())
}

@Suppress("unused")
data class JsonCreate(var table: String, var action: String, var payload: JsonColumns) {
    constructor() : this("", "", JsonColumns())
}
