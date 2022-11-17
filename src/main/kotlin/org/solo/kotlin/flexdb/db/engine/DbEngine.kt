package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumn
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.internal.appendFile
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import org.solo.kotlin.flexdb.zip.ZipUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

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

        if (exceededLimit()) {
            trimTable()
        }
    }

    private fun tableExists(tableName: String): Boolean {
        return try {
            return db.tableExists(tableName)
        } catch (ex: Exception) {
            false
        }
    }

    private fun tableExists(table: Table): Boolean {
        return tableExists(table.name)
    }

    private fun tablePath(table: Table): Path? {
        return db.tablePath(table.name)
    }

    private fun hasLimit(): Boolean {
        return limit > 0
    }

    private fun exceededLimit(): Boolean {
        return hasLimit() && tableQueue.size > limit
    }

    private fun trimTable() {
        while (!exceededLimit()) {
            val name = tableQueue.poll()

            // tableNames.remove(name)
            // Do not call the above code, since it is called in the below function 'removeAll'

            // Focus on making this method parallel this later
            removeAll(name ?: return)
        }
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

    @Throws(IOException::class, InvalidQueryException::class)
    fun createTable(table: Table): Boolean {
        if (tableExists(table)) {
            return false
        }

        val dbCol = DbColumn(table.schemaSet)
        val path = tablePath(table) ?: throw InvalidQueryException("Table path is null")
        val bout = ByteArrayOutputStream()

        val mapper = JsonUtil.newBinaryObjectMapper()
        mapper.writeValue(bout, dbCol)

        val file =
            db.root.appendFile("temp_${Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).nextInt()}")

        try {
            file.writeBytes(bout.toByteArray())
            ZipUtil.compress(path, db.password, file)
        } finally {
            file.delete()
        }
        return true
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
    operator fun get(tableName: String): Table {
        loadTableIfNotLoaded(tableName)
        return tables[tableName]!!
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