package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.RowMap
import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.types.DbValue

class Row(val id: Int, val schema: Schema) : Iterable<MutableMap.MutableEntry<Column, DbValue<*>?>> {
    private val content: RowMap = RowMap(schema)

    private fun schemaMatches(s: Set<Column>): Boolean {
        return content.schema.schemaSet == s
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

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Row

        if (id != other.id) return false
        if (schema != other.schema) return false
        if (content != other.content) return false

        return true
    }
}