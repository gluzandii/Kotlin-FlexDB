package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.util.*

class Table(val name: String, private val schema: Schema) {
    var rows: TreeSet<Row>
        private set

    init {
        rows = TreeSet()
    }

    @Throws(MismatchedSchemaException::class)
    fun addRow(row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("The row provided does not match this table's schema")
        }

        rows.add(row)
    }

    @Throws(MismatchedSchemaException::class)
    fun loadRows(vararg rs: Row) {
        rs.forEach(this::addRow)
    }
}