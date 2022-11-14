package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.RowMap
import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.types.DbValue

class Row(val schema: Schema) : Iterable<MutableMap.MutableEntry<Column, DbValue<*>?>> {
    private val content: RowMap = RowMap(schema)

    private fun schemaMatches(s: Set<Column>): Boolean {
        return content.schema == s
    }

    fun containsColumn(c: Column): Boolean {
        return content.containsColumn(c)
    }

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(colName: Column, value: DbValue<*>?) {
        content[colName] = value
    }

    operator fun get(colName: Column): DbValue<*>? {
        return content[colName]
    }

    fun schemaMatches(sc: Schema): Boolean {
        return schema == sc
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<Column, DbValue<*>?>> {
        return content.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Row) {
            return false
        }

        if (schema != other.schema) {
            return false
        }
        return content == other.content
    }

    override fun hashCode(): Int {
        return 31 * schema.hashCode() + content.hashCode()
    }
}