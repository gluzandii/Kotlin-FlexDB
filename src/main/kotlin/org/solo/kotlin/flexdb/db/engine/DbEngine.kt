package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.InvalidTableNameException
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException

abstract class DbEngine protected constructor(size: Int?) {
    protected val tables: TableMap

    init {
        tables = TableMap(size)
    }

    constructor() : this(null)

    @Throws(IOException::class)
    abstract fun loadTables()

    operator fun get(table: String): Table? {
        return tables[table]
    }

    operator fun set(name: String, table: Table) {
        tables[name] = table
    }

    @Throws(InvalidTableNameException::class)
    fun remove(key: String) {
        tables.remove(key)
    }

    fun clear() {
        tables.clear()
    }

    fun containsExact(table: String): Boolean {
        return tables.containsExact(table)
    }

    fun contains(table: String): Boolean {
        return tables.contains(table)
    }

    fun getTable(table: String): Set<Table> {
        return tables.getTable(table)
    }
}
