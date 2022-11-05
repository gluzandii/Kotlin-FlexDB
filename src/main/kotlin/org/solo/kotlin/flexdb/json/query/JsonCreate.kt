package org.solo.kotlin.flexdb.json.query

class JsonCreate {
    lateinit var table: String
    lateinit var payload: Map<String, JsonCreateColumn>
}

class JsonCreateColumn {
    lateinit var type: String
    lateinit var constraints: Set<String>
}
