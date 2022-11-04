package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.RowMap
import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.types.DbValue


data class Row(val id: Int, val schema: Schema) {
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
}