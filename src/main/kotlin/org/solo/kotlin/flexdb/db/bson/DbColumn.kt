package org.solo.kotlin.flexdb.db.bson

import org.solo.kotlin.flexdb.json.query.classes.JsonColumns

@Suppress("unused")
data class DbColumn(var columns: JsonColumns) {
    constructor() : this(JsonColumns())
}
