package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.nio.file.Path
import java.util.*

class Table(val path: Path, val schema: Set<Column>) {
    private val rows = LinkedList<Row>()

    operator fun get(id: Int): Row {
        if (id >= rows.size) {
            throw IndexOutOfBoundsException("The id: $id is greater than the amount of rows present in the current table.")
        }

        return rows[id]
    }

    fun addRow(row: Row) = rows.add(row)
}