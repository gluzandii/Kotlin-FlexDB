package org.solo.kotlin.flexdb.json.query.classes

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumType
import java.util.*

typealias JsonColumns = LinkedHashMap<String, JsonColumn>

fun String.capitalise(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(Locale.getDefault())
        } else {
            lowercase().capitalise()
        }
    }
}

@Suppress("unused")
fun JsonColumns.toSchema(): Schema {
    val set = hashSetOf<Column>()

    for ((k, v) in this) {
        val type =
            DbEnumType.valueOf(v.type.capitalise())
        val consts = EnumSet.noneOf(DbConstraint::class.java)!!

        for (i in v.constraints) {
            consts.add(DbConstraint.valueOf(i.capitalise()))
        }

        set.add(Column(name = k, type, consts))
    }
    return Schema(set)
}

data class JsonColumn(var type: String, var constraints: Set<String>) {
    constructor() : this("", setOf())
}

@Suppress("unused")
data class JsonCreate(var tableName: String, var action: String, var payload: JsonColumns) {
    constructor() : this("", "", JsonColumns())
}
