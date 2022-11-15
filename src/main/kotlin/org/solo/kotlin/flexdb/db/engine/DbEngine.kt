package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A Thread-Safe abstract DbEngine.
 */
abstract class DbEngine protected constructor(protected val db: DB, private val limit: Int) {
    private val tables: MutableMap<String, Table> = ConcurrentHashMap<String, Table>()

    private val tableNames: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    private val tableQueue: Queue<String>

    init {
        tableQueue = ConcurrentLinkedQueue()
    }

    /**
     * Note: This should not be thread safe, it will be
     * managed by calling methods.
     */
    @Throws(IOException::class)
    protected abstract fun loadTables0(tableName: String)

    @Throws(IOException::class)
    protected fun loadTables(tableName: String) {
        loadTables0(tableName)

        if (hasLimit()) {
            val name = tableQueue.poll()
            removeAll(name ?: return)
        }
    }

    @Throws(IOException::class)
    fun getTables(tableName: String): Set<Table> {
        if (!tableNames.contains(tableName)) {
            loadTables(tableName)
        }

        val regex = Regex("${tableName}_\\d+")
        val set = hashSetOf<Table>()

        for ((k, v) in tables) {
            if (regex.matches(k)) {
                set.add(v)
            }
        }

        return set
    }

    @Throws(IOException::class)
    fun getTable(tableName: String): Table {
        if (!tableNames.contains(tableName)) {
            loadTables(tableName)
        }

        return tables[tableName]!!
    }

    inline fun query(table: String, where: String, sortingType: SortingType): Query {
        return Query(table, this, where, sortingType)
    }

    private inline fun hasLimit(): Boolean {
        return limit > 0
    }

    private fun removeAll(table: String) {
        val regex = Regex("${table}_\\d+")

        for ((k, _) in tables) {
            if (regex.matches(k)) {
                tables.remove(k)
            }
        }

        tableNames.remove(table)
    }
}