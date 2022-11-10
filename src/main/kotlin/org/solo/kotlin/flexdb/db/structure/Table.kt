package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.util.*

class Table(private val name: String, private val schema: Schema) {
    private var rows = hashMapOf<Int, Row>()

    /**
     * If the id does not exist, it is added.
     */
    @Throws(MismatchedSchemaException::class)
    fun replaceRow(id: Int, row: Row) {
        if (!rows.contains(id)) {
            addRow(row)
            return
        }
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("The row provided does not match this table's schema")
        }

        rows[row.id] = row
    }

    @Throws(IllegalStateException::class)
    fun deleteRow(id: Int) {
        if (!rows.containsKey(id)) {
            error("The id: $id is not present in the current table.")
        }

        rows.remove(id)
    }

    @Throws(MismatchedSchemaException::class)
    fun addRow(row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("The row provided does not match this table's schema")
        }
        if (rows.contains(row.id)) {
            return
        }

        rows[row.id] = row
    }

    @Throws(MismatchedSchemaException::class)
    inline fun addRows(vararg rs: Row) {
        for (i in rs) {
            addRow(i)
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Table) {
            return false
        }
        return other.name == name
    }
}