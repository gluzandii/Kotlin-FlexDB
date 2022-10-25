package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.structure.SchemaMap

@Suppress("unused")
class Row(schema: Set<Column>) {
    private val content: SchemaMap

    init {
        content = SchemaMap(schema)
    }

    fun schemaMatches(s: Set<Column>) = content.schema.containsAll(s)

    @Throws(Throwable::class)
    operator fun set(colName: Column, value: DbValue<*>?) {
        content[colName] = value
    }

    operator fun get(colName: Column) = content[colName]
}