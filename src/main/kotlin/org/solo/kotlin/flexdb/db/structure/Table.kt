package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.db.types.DbValue
import java.nio.file.Path

@Suppress("unused")
class Table(val path: Path, val schema: Set<Column>) {
    private val rows = ArrayList<Row>()

    @Throws(Throwable::class)
    operator fun get(id: Int): Row {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }

        return rows[id]
    }

    fun addRow(row: Row) = rows.add(row)


    @Throws(Throwable::class)
    fun replaceRow(id: Int, row: Row) {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }
        rows[id] = row
    }

    @Throws(Throwable::class)
    fun modifyRow(id: Int, columnToModify: Column, value: DbValue<*>?) {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }
        this[id][columnToModify] = value
    }
}