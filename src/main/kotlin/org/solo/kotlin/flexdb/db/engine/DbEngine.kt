package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.InvalidTableNameException
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException
import java.util.*

abstract class DbEngine {
    protected val tables: MutableMap<String, Table> = HashMap()

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
        if (!containsExact(key)) {
            throw InvalidTableNameException("The table: $key, is not in the map of tables.")
        }
        tables.remove(key)
    }

    fun clear() {
        tables.clear()
    }

    fun containsExact(table: String): Boolean {
        return tables.containsKey(table)
    }

    fun contains(table: String): Boolean {
        val regex = Regex("$table\\d+")
        for (i in tables.keys) {
            if (regex.matches(i)) {
                return true
            }
        }
        return false
    }

    fun getTable(table: String): Set<Table> {
        val regex = Regex("$table\\d+")
        val set = TreeSet<Table>()

        for (i in tables.keys) {
            if (regex.matches(i)) {
                set.add(tables[i]!!)
            }
        }
        return set
    }
}
