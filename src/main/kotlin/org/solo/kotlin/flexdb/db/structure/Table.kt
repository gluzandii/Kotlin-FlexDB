package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.util.*

class Table(val name: String, private val schema: Schema) : Iterable<Row> {
    private val rows = TreeSet<Row>()

    fun containsRow(row: Row): Boolean {
        return rows.contains(row)
    }

    @Throws(MismatchedSchemaException::class)
    fun add(row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("Schema of row does not match schema of table")
        }
        rows.add(row)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun iterator(): Iterator<Row> {
        return rows.iterator()
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