package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.structure.RowMap
import org.solo.kotlin.flexdb.db.types.DbValue


class Row(val id: Int, schema: Set<Column>) {
    private val content: RowMap

    init {
        content = RowMap(schema)
    }

    fun schemaMatches(s: Set<Column>): Boolean {
        return content.schema == s
    }

    fun containsColumn(c: Column): Boolean {
        return content.containsColumn(c)
    }

    @Throws(Throwable::class)
    operator fun set(colName: Column, value: DbValue<*>?) {
        content[colName] = value
    }

    operator fun get(colName: Column): DbValue<*>? {
        return content[colName]
    }

    override fun hashCode(): Int {
        return id
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Row) {
            return false
        }
        if (id != other.id) {
            return false
        }

        return true
    }

    fun trueEquals(other: Row): Boolean {
        return other.schemaMatches(content.schema) && equals(other)
    }
}