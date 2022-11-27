package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.MismatchedSchemaException
import org.solo.kotlin.flexdb.db.bson.DbColumnFile
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.solo.kotlin.flexdb.json.query.classes.JsonQueryColumn
import java.util.*


class Table(val name: String, val schema: Schema) : Iterable<Row> {
    /**
     * Rows in table
     */
    private val rows = TreeSet<Row>()

    init {
        if (name.isEmpty() || name.isBlank()) {
            throw IllegalArgumentException("Table name cannot be empty")
        }
    }

    /**
     * Returns the [DbColumnFile] of this table, that can be serialized.
     */
    val dbColumnFile: DbColumnFile
        get() {
            val j = JsonCreatePayload()
            for (i in schema) {
                j[i.name] = JsonQueryColumn(i.type.name, i.stringConstraints)
            }

            return DbColumnFile(j)
        }

    /**
     * Checks if the schema of given [Table] is the same as this table.
     *
     * @param t The [Table] to check
     */
    fun tableSchemaMatches(t: Table): Boolean {
        return this.schema == t.schema
    }

    /**
     * Adds 2 tables together.
     */
    @Throws(MismatchedSchemaException::class)
    operator fun plus(table: Table): Table {
        if (!table.tableSchemaMatches(this)) {
            throw MismatchedSchemaException("Cannot add table with schema ${table.schema} to table with schema $schema")
        }
        val t = Table(name, schema)

        t.addAll(table.rows)
        t.addAll(this.rows)

        return t
    }


    /**
     * Adds a [Row] to the current table, unless it already exists.
     *
     * @param row [Row] to add
     */
    @Throws(MismatchedSchemaException::class)
    fun add(row: Row) {
        if (!row.schemaMatches(schema)) {
            throw MismatchedSchemaException("Schema of row does not match schema of table")
        }
        if (rows.contains(row)) {
            throw IllegalArgumentException("Row already exists in table")
        }
        rows.add(row)
    }

    /**
     * Adds the [Collection] of rows to this [Table]
     *
     * @param rows [Collection] of [Row] to add
     */
    @Throws(MismatchedSchemaException::class)
    fun addAll(rows: Collection<Row>) {
        for (i in rows) {
            add(i)
        }
    }

    /**
     * [Iterator] that iterates over the rows in this table.
     */
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