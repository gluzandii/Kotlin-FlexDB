package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.util.concurrent.ConcurrentHashMap

class Table(val name: String, private val schema: Schema) {
    private val rows = ConcurrentHashMap<Int, Row>()

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

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Table

        if (name != other.name) return false
        if (schema != other.schema) return false
        if (rows != other.rows) return false

        return true
    }
}