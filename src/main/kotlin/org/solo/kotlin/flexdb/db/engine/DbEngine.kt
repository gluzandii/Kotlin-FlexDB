package org.solo.kotlin.flexdb.db.engine

import kotlinx.coroutines.*
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
import kotlin.io.path.deleteIfExists
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
    protected abstract fun serializeTable0(table: Table)

    @Throws(IOException::class)
    protected fun loadTables(tableName: String) {
        loadTables0(tableName)

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
            loadTables(tableName)
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

    @Throws(IOException::class, InvalidQueryException::class)
    fun createTable(table: Table): Boolean {
        if (tableExists(table.name)) {
            return false
        }

        val dbCol = DbColumn(table.schemaSet)
        val path = tablePath(table.name)
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
    fun deleteTable(table: Table): Boolean {
        if (!tableExists(table.name)) {
            return false
        }

        val path = tablePath(table.name)
        return path.deleteIfExists()
    }

    @Throws(IOException::class)
    fun getTable(tableName: String): Table {
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
}