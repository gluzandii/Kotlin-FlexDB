package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.Constraint
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.db.types.DbValue
import java.nio.file.Path

@Suppress("unused")
class Table(val path: Path, private val schema: Set<Column>) {
    private val rows = ArrayList<Row>()

    private val uniqueColumn: MutableSet<Column> = hashSetOf()

    init {
        for (i in schema) {
            if (i.hasConstraint(Constraint.Unique)) {
                uniqueColumn.add(i)
            }
        }
    }

    @Throws(MismatchedSchemaException::class)
    private inline fun validRow(row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("The row provided does not match with this table's schema.")
        }
    }

    operator fun get(id: Int): Row {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }

        return rows[id]
    }

    @Throws(MismatchedSchemaException::class)
    fun addRow(row: Row): Boolean {
        validRow(row)
        return rows.add(row)
    }


    @Throws(MismatchedSchemaException::class)
    fun replaceRow(id: Int, row: Row) {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }

        validRow(row)
        rows[id] = row
    }

    fun modifyRow(id: Int, columnToModify: Column, value: DbValue<*>?) {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }
        this[id][columnToModify] = value
    }
}