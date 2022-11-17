package org.solo.kotlin.flexdb.json.query.classes

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumTypes

typealias JsonColumns = HashMap<String, JsonColumn>

fun JsonColumns.toSchema(): Schema {
    val set = hashSetOf<Column>()

    for ((k, v) in this) {
        val type = DbEnumTypes.valueOf(v.type)
        val consts = hashSetOf<DbConstraint>()

        for (i in v.constraints) {
            consts.add(DbConstraint.valueOf(i))
        }

        set.add(Column(name = k, type, consts))
    }
    return Schema(set)
}

data class JsonColumn(var type: String, var constraints: Set<String>) {
    constructor() : this("", setOf())
}

@Suppress("unused")
data class JsonCreate(var table: String, var action: String, var payload: JsonColumns) {
    constructor() : this("", "", JsonColumns())
}
