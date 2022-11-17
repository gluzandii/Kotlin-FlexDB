package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.internal.schemaMatches
import org.solo.kotlin.flexdb.json.query.classes.JsonColumn
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.util.*

@Suppress("unused")
class Table(val name: String, val schema: Schema) : Iterable<Row> {
    private val rows = TreeSet<Row>()

    init {
        if (name.isEmpty() || name.isBlank()) {
            throw IllegalArgumentException("Table name cannot be empty")
        }
    }

    val schemaSet: JsonColumns
        get() {
            val j = JsonColumns()
            for (i in schema) {
                j[i.name] = JsonColumn(i.type.name, i.stringConstraints)
            }

            return j
        }


    operator fun plus(table: Table): Table {
        println(table)
        return this
    }

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

    override fun iterator(): Iterator<Row> {
        return rows.iterator()
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