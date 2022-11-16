package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.query.ReadOnlyQuery
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
    /**
     * Stores each table.
     *
     * Not: 'TableName': Table. No
     * 'TableName_{id range}': Table. Yes
     */
    private val tables: MutableMap<String, Table> = ConcurrentHashMap<String, Table>()

    /**
     * Stores currently loaded tables.
     */
    private val tableNames: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    /**
     * The queue of tables, to properly manage the table limit.
     */
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

            // tableNames.remove(name)
            // Do not call the above code, since it is called in the below function 'removeAll'
            removeAll(name ?: return)
        }
    }

    @Throws(IOException::class)
    fun getTables(tableName: String): Set<Table> {
        loadTableIfNotLoaded(tableName)

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
        loadTableIfNotLoaded(tableName)
        return tables[tableName]!!
    }

    fun query(table: String, where: String, sortingType: SortingType): ReadOnlyQuery {
        return ReadOnlyQuery(table, this, where, sortingType)
    }

    protected fun hasLimit(): Boolean {
        return limit > 0
    }

    private fun loadTableIfNotLoaded(tableName: String) {
        if (!tableNames.contains(tableName)) {
            loadTables(tableName)
        }
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

    companion object {
        @JvmStatic
        fun tableName(table: String, index: Int): String {
            var start = 0
            var end = 200

            while (true) {
                val range = (start until end)

                if (range.contains(index)) {
                    return "${table}_${start}"
                }

                start = end
                end += 200
            }
        }
    }
}