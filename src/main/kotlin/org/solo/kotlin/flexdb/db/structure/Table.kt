package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row

class Table(val name: String, private val schema: Schema) {
    private val rows = hashMapOf<Int, Row>()

    fun contains(rowId: Int): Boolean {
        return rows.containsKey(rowId)
    }

    fun containsRow(row: Row): Boolean {
        return rows.containsValue(row)
    }

    operator fun get(rowId: Int): Row? {
        return rows[rowId]
    }

    @Throws(MismatchedSchemaException::class)
    operator fun set(rowId: Int, row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("Schema of row does not match schema of table")
        }
        rows[rowId] = row
    }
}