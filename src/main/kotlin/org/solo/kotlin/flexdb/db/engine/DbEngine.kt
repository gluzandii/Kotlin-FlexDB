package org.solo.kotlin.flexdb.db.engine

import kotlinx.coroutines.*
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.internal.schemaMatches
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.io.path.deleteIfExists

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
     * Stores all the tables present in this database.
     */
    private val allTables: MutableSet<String> = ConcurrentHashMap.newKeySet()

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
    protected abstract fun loadTable0(tableName: String)

    @Throws(IOException::class)
    protected abstract fun serializeTable0(table: Table)

    @Throws(IOException::class)
    protected fun loadTable(tableName: String) {
        loadTable0(tableName)

        if (exceededLimit()) {
            trimTable()
        }
    }

    private fun trimTable() = runBlocking {
        while (!exceededLimit()) {
            val name = tableQueue.poll()
            launch { removeAll(name) }
        }
    }


    private fun loadTableIfNotLoaded(tableName: String) {
        if (!tables.containsKey(tableName)) {
            loadTable(tableName)
        }
    }

    private fun removeAll(table: String) {
        if (!tables.containsKey(table)) {
            return
        }
        val t = tables.remove(table)!!
        serializeTable0(t)
    }

    private fun tableExists(tableName: String): Boolean {
        return try {
            return db.tableExists(tableName)
        } catch (ex: Exception) {
            false
        }
    }

    private fun tablePath(tableName: String): Path {
        return db.tablePath(tableName)
    }

    private fun hasLimit(): Boolean {
        return limit > 0
    }

    private fun exceededLimit(): Boolean {
        return hasLimit() && tableQueue.size > limit
    }

    private fun isTableLoaded(table: String): Boolean {
        return tables.containsKey(table)
    }

//    @Throws(IOException::class, InvalidQueryException::class)
//    fun createTable(table: Table): Boolean {
//        if (tableExists(table.name)) {
//            return false
//        }
//
//        val dbCol = DbColumn(table.schemaSet)
//        val path = tablePath(table.name)
//        val bout = ByteArrayOutputStream()
//
//        val mapper = JsonUtil.newBinaryObjectMapper()
//        mapper.writeValue(bout, dbCol)
//
///        return true
//    }


    @Suppress("unused")
    @Throws(IOException::class)
    fun deleteTable(table: Table): Boolean {
        if (!tableExists(table.name)) {
            return false
        }

        val path = tablePath(table.name)
        return path.deleteIfExists()
    }

    @Throws(IOException::class)
    operator fun get(tableName: String): Table {
        loadTableIfNotLoaded(tableName)
        return tables[tableName]!!
    }

    @Throws(IOException::class)
    operator fun set(tableName: String, table: Table) {
        if (!allTables.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        if (!isTableLoaded(tableName)) {
            loadTable(tableName)
        }
        val t = tables[tableName]!!
        if (!t.schemaMatches(table.schema)) {
            throw IllegalArgumentException("The table: $tableName does not match the current schema.")
        }

        tables[tableName] = table
    }

    fun query(
        command: String,
        table: String,
        where: String?,
        columns: JsonColumns?,
        sortingType: SortingType
    ): Query<*> {
        return Query.build(command, table, this, where, columns, sortingType)
    }
}