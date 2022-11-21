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
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes


/**
 * A Thread-Safe abstract DbEngine.
 */
@Suppress("unused")
abstract class DbEngine protected constructor(
    protected val db: DB,
    private val rowsPerFile: Int = 1000
) {
    /**
     * Stores each table.
     *
     * Not: 'TableName': Table. No
     * 'TableName_{id range}': Table. Yes
     */
    private val tablesMap: MutableMap<String, Table> = ConcurrentHashMap<String, Table>()

    /**
     * Stores all the tables present in this database.
     */
    private val allTablesSet: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private val timerMap: MutableMap<String, Timer> = ConcurrentHashMap<String, Timer>()


    /**
     * @param callback A callback that is called when the table is loaded.
     */
    @Throws(IOException::class)
    protected abstract fun loadTable0(tableName: String, callback: () -> Unit)

    /**
     * @param callback A callback that is called when the table is saved.
     */
    @Throws(IOException::class)
    protected abstract fun serializeTable0(table: Table, callback: () -> Unit)

    @Throws(IOException::class)
    protected suspend fun loadTable(tableName: String): Boolean {
        var toReturn = false
        if (!tablesMap.containsKey(tableName)) {
            suspendCoroutine<Unit> {
                loadTable0(tableName) {
                    it.resume(Unit)
                }
            }

            checkAndAddTimer(tableName)
            toReturn = true
        }

        return toReturn
    }

    @Throws(IOException::class)
    protected suspend fun serializeTable(table: Table) {
        if (!db.tableExists(table.name)) {
            createTable(table)
            return
        }

        suspendCoroutine<Unit> {
            serializeTable0(table) {
                it.resume(Unit)
            }
        }
    }

    protected suspend fun remove(tableName: String) {
        if (!tablesMap.containsKey(tableName)) {
            return
        }
        checkAndRemoveTimer(tableName)

        val t = tablesMap.remove(tableName)!!
        serializeTable(t)
    }

    @Throws(IOException::class)
    suspend fun createTable(table: Table) {
        if (allTablesSet.contains(table.name)) {
            throw IOException("Table already exists.")
        }
        tablesMap[table.name] = table
        allTablesSet.add(table.name)

        checkAndAddTimer(table.name)

        val rows = ConcurrentLinkedQueue<TreeMap<Int, HashMap<String, DbValue<*>?>>>()
        val dbCol = DbColumnFile(table.schemaSet)

        coroutineScope {
            var (rowLHM, rowLHMutex) = Pair(TreeMap<Int, HashMap<String, DbValue<*>?>>(), Mutex())

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
                            rows.add(rowLHM)
                            rowLHM = TreeMap<Int, HashMap<String, DbValue<*>?>>()
                        }
                    }
                }
            }
            if (rows.peek() != rowLHM) {
                rows.add(rowLHM)
            }
        }

        val rowsFile = LinkedList<DbRowFile>()
        var start = 0
        for (i in rows) {
            rowsFile.add(DbRowFile(i))
        }

        val path = db.tablePath(table.name)
        val columnBout = ByteArrayOutputStream()

        val mapper = JsonUtil.newBinaryObjectMapper()

        mapper.writeValue(columnBout, dbCol)
        path.createDirectories()

        val column = path.append("column.bson")
        column.writeBytes(columnBout.toByteArray())

        val rowBout = ByteArrayOutputStream()
        for (row in rowsFile) {
            rowBout.reset()
            mapper.writeValue(rowBout, row)

            path.append("row_${start}.bson").writeBytes(rowBout.toByteArray())
            start += rowsPerFile
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

        suspendCoroutine<Unit> {
            DbFuture.performAsync {
                try {
                    FileUtils.deleteDirectory(path.toFile())
                } catch (e: IOException) {
                    exp = e
                }
            }.thenAccept { _ -> it.resume(Unit) }
        }
        if (exp != null) {
            throw (exp as IOException)
        }

        checkAndRemoveTimer(table.name)
        return true
    }

    @Throws(IOException::class)
    suspend fun get(tableName: String): Table {
        if (!allTablesSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        val b = loadTable(tableName)
        if (!b) {
            checkAndResetTimer(tableName)
        }

        return tablesMap[tableName]!!
    }

    @Throws(IOException::class)
    suspend fun set(tableName: String, table: Table) {
        if (!allTablesSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        val b = loadTable(tableName)
        if (!b) {
            checkAndResetTimer(tableName)
        }

        val t = tablesMap[tableName]!!
        if (!t.schemaMatches(table.schema)) {
            throw IllegalArgumentException("The table: $tableName does not match the current schema.")
        }

        tablesMap[tableName] = table
    }

//    @Throws(IOException::class)
//    suspend fun appendTable(tableName: String, toAppend: Table) {
//        if (!allTables.contains(tableName)) {
//            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
//        }
//
//        val t = this.get(tableName)
//        this.set(tableName, t + toAppend)
//    }

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
            timer.schedule(TableTimerTask(this, tableName), 1000 * 60)

            timerMap[tableName] = timer
        }
    }

    private fun checkAndResetTimer(tableName: String) {
        if (timerMap.containsKey(tableName)) {
            timerMap[tableName]!!.cancel()

            val t = Timer("${tableName}_timer", true)
            t.schedule(TableTimerTask(this, tableName), 1000 * 60)

            timerMap[tableName] = t
        }
    }

    class TableTimerTask(private val engine: DbEngine, private val name: String) : TimerTask() {
        override fun run() = runBlocking {
            engine.remove(name)
        }
    }
}
