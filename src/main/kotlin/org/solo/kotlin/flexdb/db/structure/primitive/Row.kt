package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.structure.SchemaMap

@Suppress("unused")
class Row(val rowNum: Long, schema: Set<Column>) {
    private val content: SchemaMap

    init {
        content = SchemaMap(schema)
    }

    @Throws(Throwable::class)
    operator fun set(colName: Column, value: DbValue<*>?) {
        content[colName] = value
    }
    
    operator fun get(colName: Column) = content[colName]
}