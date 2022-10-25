package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbValue

class Row(val rowNum: Long, schema: Array<Column>) {
    private val content: MutableMap<Column, DbValue<*>?> = hashMapOf()

    init {
        schema.forEach { content[it] = null }
    }

    @Throws(Throwable::class)
    operator fun set(colName: String, value: DbValue<*>?) = set(Column.nameOnly(colName), value)

    @Throws(Throwable::class)
    operator fun set(colName: Column, value: DbValue<*>?) {
        if (!content.containsKey(colName)) {
            throw IllegalArgumentException("The key: ${colName.name} does not exist in the row.")
        }
        content[colName] = value
    }

    operator fun get(colName: String) = get(Column.nameOnly(colName))
    operator fun get(colName: Column) = content.getOrDefault(colName, null)
}