package org.solo.kotlin.flexdb.db.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.FileUtils
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumnFile
import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.internal.*
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.io.path.createDirectories
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes


/**
 * A Thread-Safe abstract DbEngine.
 */
@Suppress("unused")
abstract class DbEngine protected constructor(
    protected val db: DB,
    protected val rowsPerFile: Int = 1000
) {
    /**
     * Stores each table.
     *
     * Not: 'TableName': Table. No
     * 'TableName_{id range}': Table. Yes
     */
    protected val tablesMap: MutableMap<String, Table> = ConcurrentHashMap<String, Table>()

    /**
     * Stores all the tables present in this database.
     */
    protected val allTablesSet: MutableSet<String> = ConcurrentHashMap.newKeySet()

    protected val timerMap: MutableMap<String, Timer> = ConcurrentHashMap<String, Timer>()

    @Throws(IOException::class)
    protected abstract suspend fun loadTable0(tableName: String)

    @Throws(IOException::class)
    protected suspend fun loadRowInTableFolder(tableName: String, rowId: Int): DbRowFile {
        val table = db.schema.resolve(tableName).resolve("row_$rowId")
        var exp: IOException? = null
        var dbRowFile: DbRowFile? = null

        withContext(Dispatchers.IO) {
            try {
                dbRowFile = DbRowFile.deserialize(table.readBytes())
            } catch (e: IOException) {
                exp = e
            }
        }
        if (exp != null) {
            throw exp!!
        }

        return dbRowFile ?: throw IOException("Could not load row: $table")
    }

    @Throws(IOException::class)
    protected suspend fun loadColumnInTableFolder(tableName: String): DbColumnFile {
        val table = db.schema.resolve(tableName).resolve("column")
        var exp: IOException? = null
        var dbColumnFile: DbColumnFile? = null

        withContext(Dispatchers.IO) {
            try {
                dbColumnFile = DbColumnFile.deserialize(table.readBytes())
            } catch (e: IOException) {
                exp = e
            }
        }
        if (exp != null) {
            throw exp!!
        }

        return dbColumnFile ?: throw IOException("Could not load column in: $tableName")
    }

    @Throws(IOException::class)
    private suspend fun loadEntireTable(tableName: String) {
        if (!tablesMap.containsKey(tableName)) {
            loadTable0(tableName)
            checkAndAddTimer(tableName)
        } else {
            checkAndResetTimer(tableName)
        }
    }

    @Throws(IOException::class)
    protected abstract suspend fun serializeTable0(table: Table)

    @Throws(IOException::class)
    private suspend fun serializeEntireTable(table: Table) {
        if (!db.tableExists(table.name)) {
            createTable(table)
            return
        }

        serializeTable0(table)
    }

    @Throws(InvalidQueryException::class)
    protected suspend fun splitTableIntoRows(table: Table): Queue<DbRowFile> {
        val q = ConcurrentLinkedQueue<DbRowFile>()
        coroutineScope {
            var (rowLHM, rowLHMutex) = Pair(DbRowFile(), Mutex())

            for (i in table) {
                launch(Dispatchers.Default) {
                    val id = i.id

                    val contentMap = HashMap<String, DbValue<*>?>()
                    val dupli = HashMap<String, HashSet<DbValue<*>>>()

                    for ((k, v) in i) {
                        val n = k.name

                        if (v == null && k.hasConstraint(DbConstraint.NotNull)) {
                            throw InvalidQueryException("Column '${k.name}' cannot be null.")
                        }
                        if (k.hasConstraint(DbConstraint.Unique) && v != null) {
                            if (dupli.containsKey(n)) {
                                if (dupli[n]!!.contains(v)) {
                                    throw InvalidQueryException("Column '${k.name}' must be unique.")
                                }

                                dupli[n]!!.add(v)
                            } else {
                                dupli[n] = hashSetOf(v)
                            }
                        }

                        contentMap[n] = v
                    }

                    rowLHMutex.withLock {
                        rowLHM[id] = contentMap

                        if (rowLHM.size == rowsPerFile) {
                            q.add(rowLHM)
                            rowLHM = DbRowFile()
                        }
                    }
                }
            }
            if (q.peek() != rowLHM) {
                q.add(rowLHM)
            }
        }

        return q
    }

    @Throws(IOException::class)
    protected suspend fun writeRowFileInTable(tableName: String, id: Int, row: DbRowFile) {
        var exp: IOException? = null
        withContext(Dispatchers.IO) {
            try {
                db.schema.resolve(tableName).resolve("row_$id").writeBytes(row.serialize())
            } catch (e: IOException) {
                exp = e
            }
        }

        if (exp != null) {
            throw exp!!
        }
    }

    @Throws(IOException::class)
    protected suspend fun writeColumnInTable(tableName: String, row: DbColumnFile) {
        var exp: IOException? = null
        withContext(Dispatchers.IO) {
            try {
                db.schema.resolve(tableName).resolve("column").writeBytes(row.serialize())
            } catch (e: IOException) {
                exp = e
            }
        }

        if (exp != null) {
            throw exp!!
        }
    }

    private suspend fun remove(tableName: String) {
        if (!tablesMap.containsKey(tableName)) {
            return
        }
        checkAndRemoveTimer(tableName)

        val t = tablesMap.remove(tableName)!!
        serializeEntireTable(t)
    }

    @Throws(IOException::class)
    suspend fun createTable(table: Table) {
        if (allTablesSet.contains(table.name)) {
            throw IOException("Table already exists.")
        }
        tablesMap[table.name] = table
        allTablesSet.add(table.name)

        checkAndAddTimer(table.name)
        val rows = splitTableIntoRows(table)

        val path = db.tablePath(table.name)
        path.createDirectories()

        writeColumnInTable(table.name, DbColumnFile(table.schemaSet))

        var start = 0
        fun incrementAndGive(): Int {
            val c = start
            start += rowsPerFile
            return c
        }

        for (row in rows) {
            writeRowFileInTable(table.name, incrementAndGive(), row!!)
        }
    }


    @Suppress("unused")
    @Throws(IOException::class)
    suspend fun deleteTable(table: Table): Boolean {
        if (!db.tableExists(table.name)) {
            return false
        }

        var exp: IOException? = null
        val path = db.tablePath(table.name)

        withContext(Dispatchers.IO) {
            try {
                FileUtils.deleteDirectory(path.toFile())
            } catch (e: IOException) {
                exp = e
            }
        }
        if (exp != null) {
            throw exp!!
        }

        checkAndRemoveTimer(table.name)
        return true
    }

    @Throws(IOException::class)
    suspend fun get(tableName: String): Table {
        if (!allTablesSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        loadEntireTable(tableName)

        return tablesMap[tableName]!!
    }

    @Throws(IOException::class)
    suspend fun set(tableName: String, table: Table) {
        if (!allTablesSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        loadEntireTable(tableName)

        val t = tablesMap[tableName]!!
        if (!t.schemaMatches(table.schema)) {
            throw IllegalArgumentException("The table: $tableName does not match the current schema.")
        }

        tablesMap[tableName] = table
    }

    fun query(
        command: String,
        tableName: String,
        where: String?,
        columns: JsonColumns?,
        sortingType: SortingType
    ): Query<*> {
        return Query.build(command, tableName, this, where, columns, sortingType)
    }

    private fun checkAndRemoveTimer(tableName: String) {
        if (timerMap.containsKey(tableName)) {
            timerMap[tableName]!!.cancel()
            timerMap.remove(tableName)
        }
    }

    private fun checkAndAddTimer(tableName: String) {
        if (!timerMap.containsKey(tableName)) {
            val timer = Timer("${tableName}_timer", true)
            timer.schedule(TableTimerTask(this, tableName), Time.minutesToMillis(1))

            timerMap[tableName] = timer
        }
    }

    private fun checkAndResetTimer(tableName: String) {
        if (timerMap.containsKey(tableName)) {
            timerMap[tableName]!!.cancel()

            val t = Timer("${tableName}_timer", true)
            t.schedule(TableTimerTask(this, tableName), Time.minutesToMillis(1))

            timerMap[tableName] = t
        }
    }

    class TableTimerTask(private val engine: DbEngine, private val name: String) : TimerTask() {
        override fun run() = runBlocking {
            engine.remove(name)
        }
    }
}
