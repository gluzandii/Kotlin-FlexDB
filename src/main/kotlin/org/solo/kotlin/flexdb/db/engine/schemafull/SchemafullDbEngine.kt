package org.solo.kotlin.flexdb.db.engine.schemafull

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.InvalidValueProvidedInColumnException
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumnFile
import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.schemafull.Table
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.internal.*
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.name


/**
 * A Thread-Safe abstract SchemafullDbEngine.
 */
@Suppress("unused")
abstract class SchemafullDbEngine protected constructor(
    protected val db: DB,
    protected val rowsPerFile: Int,
) {
    /**
     * Stores each table.
     *
     * It stores an entire table with a name, the table isn't fragmented.
     */
    protected val tablesMap: MutableMap<String, Table> = ConcurrentHashMap<String, Table>()

    /**
     * Stores all the tables present in this database.
     */
    protected val allTablesInThisDatabaseSet: MutableSet<String> = ConcurrentHashMap.newKeySet()

    /**
     * The map that is used to store the [Timer] that is set off after a minute,
     * to remove the table from memory and serialize it.
     */
    private val tableRemoveTimerMap: MutableMap<String, Timer> = ConcurrentHashMap<String, Timer>()

    init {
        val p = db.schemafullPath

        for (i in Files.walk(p).filter {
            return@filter it.isDirectory()
        }) {
            allTablesInThisDatabaseSet.add(i.name)
        }
    }

    /**
     * Loads an entire [Table] into memory.
     */
    @Throws(IOException::class)
    protected abstract suspend fun loadTable0(tableName: String)

    /**
     * Loads the `column` file present in the [Table] folder in the database
     * schemafull folder.
     */
    @Throws(IOException::class)
    protected suspend fun loadColumnInTableFolder(tableName: String): DbColumnFile {
        val table = db.schemafullPath.resolve(tableName)
        val c = table.resolve("column")

        return DbColumnFile.deserialize(AsyncIOUtil.readBytes(c))
    }

    /**
     * Wrapper around [loadTable0], as this also adds a timer to [tableRemoveTimerMap],
     * or refreshes the [Timer] already delegated to the table.
     */
    @Throws(IOException::class)
    private suspend fun loadEntireTable(tableName: String) {
        if (!tablesMap.containsKey(tableName)) {
            loadTable0(tableName)
            checkAndAddTimer(tableName)
        } else {
            checkAndResetTimer(tableName)
        }
    }

    /**
     * Serialized the table in the database schemafull folder.
     */
    @Throws(IOException::class)
    protected abstract suspend fun serializeTable0(table: Table)

    /**
     * Wrapper around [serializeTable0], as this also removes the timer from [tableRemoveTimerMap].
     */
    @Throws(IOException::class)
    private suspend fun serializeEntireTable(table: Table) {
        if (!db.tableExists(table.name)) {
            createTable(table)
            return
        }

        serializeTable0(table)
    }

    /**
     * Converts the given [Table] into [DbRowFile], according the [SchemafullDbEngine.rowsPerFile]
     * value of this [SchemafullDbEngine]
     */
    @Throws(InvalidValueProvidedInColumnException::class)
    protected suspend fun splitTableIntoDbRowFiles(table: Table): Queue<DbRowFile> {
        val q = ConcurrentLinkedQueue<DbRowFile>()
        coroutineScope {
            var (rowLHM, rowLHMutex) = DbRowFile() to Mutex()

            for (i in table) {
                launch(Dispatchers.Default) {
                    val id = i.id

                    val contentMap = HashMap<String, DbValue<*>?>()
                    val dupli = HashMap<String, HashSet<DbValue<*>>>()

                    for ((k, v) in i) {
                        val n = k.name

                        if (v == null && k.hasConstraint(DbConstraint.NOTNULL)) {
                            throw InvalidValueProvidedInColumnException("Column '${k.name}' cannot be null.")
                        }
                        if (k.hasConstraint(DbConstraint.UNIQUE) && v != null) {
                            if (dupli.containsKey(n)) {
                                if (dupli[n]!!.contains(v)) {
                                    throw InvalidValueProvidedInColumnException("Column '${k.name}' must be unique.")
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

    /**
     * Writes a [DbRowFile] into a table with the specified id.
     */
    @Throws(IOException::class)
    protected suspend fun writeRowFileInTable(tableName: String, id: Int, row: DbRowFile) {
        val table = db.schemafullPath.resolve(tableName)
        val rgx = table.resolve("row_$id")

        AsyncIOUtil.writeBytes(
            rgx,
            row.serialize()
        )
    }

    /**
     * Writes a [DbColumnFile] into a table.
     */
    @Throws(IOException::class)
    protected suspend fun writeColumnInTable(tableName: String, row: DbColumnFile) {
        val table = db.schemafullPath.resolve(tableName)
        val r = table.resolve("column")

        AsyncIOUtil.writeBytes(
            r,
            row.serialize()
        )
    }

    /**
     * The function that is called by children of [SchemafullDbEngine], before the
     * actual logic of serializing a table is called.
     *
     * Usually only called in the [serializeTable0] method.
     *
     * @return The split table, and start number of rows, where start is used for naming
     * the row files.
     */
    @Throws(IOException::class)
    protected suspend inline fun initSerializeTableCall(table: Table): Pair<Queue<DbRowFile>, Int> {
        if (!db.tableExists(table.name)) {
            throw IOException("Table ${table.name} does not exist")
        }
        return splitTableIntoDbRowFiles(table) to 0
    }

    /**
     * The function that is called by children of [SchemafullDbEngine], before the
     * actual logic of loading a table is called.
     *
     * Usually only called in the [loadTable0] method.
     *
     * @return A regex for matching rows in the given table.
     */
    @Throws(IOException::class)
    protected inline fun initLoadTableCall(
        tableName: String,
        @Suppress("UNUSED_PARAMETER") func: () -> Unit = { },
    ): Regex {
        if (!db.tableExists(tableName)) {
            throw IOException("Table $tableName does not exist")
        }
        return Regex("row_\\d+")
    }

    /**
     * Removes the given table from the memory map [tablesMap],
     * and also removes the timer from [tableRemoveTimerMap].
     *
     * After that it serializes the table.
     */
    private suspend fun remove(tableName: String) {
        if (!tablesMap.containsKey(tableName)) {
            return
        }
        checkAndRemoveTimer(tableName)

        val t = tablesMap.remove(tableName)!!
        serializeEntireTable(t)
    }

    /**
     * Creates a [Table], with all of its content in the database schemafull folder.
     *
     * @param table The [Table] to create
     */
    @Throws(IOException::class)
    suspend fun createTable(table: Table) {
        if (allTablesInThisDatabaseSet.contains(table.name)) {
            throw IOException("Table already exists.")
        }
        tablesMap[table.name] = table
        allTablesInThisDatabaseSet.add(table.name)

        checkAndAddTimer(table.name)

        val path = db.tablePath(table.name)
        path.createDirectories()

        writeColumnInTable(table.name, table.dbColumnFile)
        serializeTable0(table)
    }


    /**
     * Deletes a [Table] from the database schemafull folder,
     * it also removes the table from the memory map [tablesMap].
     */
    @Throws(IOException::class)
    suspend fun deleteTable(table: Table): Boolean {
        if (!db.tableExists(table.name)) {
            return false
        }
        val (name) = table
        val path = db.tablePath(name)

        if (tablesMap.containsKey(name)) {
            tablesMap.remove(name)
        }

        AsyncIOUtil.deleteDirectory(path)
        checkAndRemoveTimer(name)
        return true
    }

    /**
     * Retrieves an **entire** [Table] from this database.
     *
     * If the [Table] is already loaded, it retrieves it from the memory map [tablesMap].
     * If the [Table] is not loaded, it loads it from the database schemafull folder.
     */
    @Throws(IOException::class)
    suspend fun get(tableName: String): Table {
        if (!allTablesInThisDatabaseSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        loadEntireTable(tableName)

        return tablesMap[tableName]!!
    }

    /**
     * Sets a [Table] in this database.
     *
     * If the [Table] is not loaded, it firsts loads the table from the database schemafull folder,
     * then sets it.
     *
     * It first loads the table, to check if the
     * [org.solo.kotlin.flexdb.db.structure.schemafull.Schema] of the table provided, is the same
     * as on the one stored on disk.
     */
    @Throws(IOException::class)
    suspend fun set(tableName: String, table: Table) {
        if (!allTablesInThisDatabaseSet.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        loadEntireTable(tableName)

        val t = tablesMap[tableName]!!
        if (!t.tableSchemaMatches(table)) {
            throw IllegalArgumentException("The table: $tableName does not match the current schema.")
        }

        tablesMap[tableName] = table
    }

    /**
     * Runs a [Query] on this database, and returns the result.
     *
     * @param command The type of query, like *select*, or *create*
     * @param tableName The table to operate the query one
     * @param where An optional where clause for queries like *select* or *reset*
     * @param columns The columns to return after query.
     */
    suspend fun query(
        command: String,
        tableName: String,
        where: String?,
        columns: JsonCreatePayload?,
        sortingType: Pair<SortingType, String?>,
    ): Any? {
        return Query.build(command, tableName, this, where, columns, sortingType).execute()
    }

    /**
     * Removes the timer of the given tableName if it exists.
     */
    private fun checkAndRemoveTimer(tableName: String) {
        if (tableRemoveTimerMap.containsKey(tableName)) {
            tableRemoveTimerMap[tableName]!!.cancel()
            tableRemoveTimerMap.remove(tableName)
        }
    }

    /**
     * Adds the timer of the given tableName if it doesn't exist.
     */
    private fun checkAndAddTimer(tableName: String) {
        if (!tableRemoveTimerMap.containsKey(tableName)) {
            val timer = Timer("${tableName}_timer", true)
            timer.schedule(TableTimerTask(this, tableName), Time.minutesToMillis(1))

            tableRemoveTimerMap[tableName] = timer
        }
    }

    /**
     * If the timer of the given table is loaded, it resets it.
     */
    private fun checkAndResetTimer(tableName: String) {
        if (tableRemoveTimerMap.containsKey(tableName)) {
            tableRemoveTimerMap[tableName]!!.cancel()

            val t = Timer("${tableName}_timer", true)
            t.schedule(TableTimerTask(this, tableName), Time.minutesToMillis(1))

            tableRemoveTimerMap[tableName] = t
        }
    }

    /**
     * The [TimerTask] that is used to delete the table from memory.
     */
    class TableTimerTask(private val engine: SchemafullDbEngine, private val name: String) : TimerTask() {
        override fun run() = runBlocking {
            engine.remove(name)
        }
    }
}
