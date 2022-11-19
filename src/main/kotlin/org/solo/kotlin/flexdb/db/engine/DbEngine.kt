package org.solo.kotlin.flexdb.db.engine

import kotlinx.coroutines.*
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumn
import org.solo.kotlin.flexdb.db.bson.DbRow
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.internal.append
import org.solo.kotlin.flexdb.internal.deleteRecursively
import org.solo.kotlin.flexdb.internal.schemaMatches
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes


/**
 * A Thread-Safe abstract DbEngine.
 */
@Suppress("unused")
abstract class DbEngine protected constructor(
    protected val db: DB,
    private val rowsPerFile: Short
) {
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


    @Throws(IOException::class)
    protected abstract suspend fun loadTable0(tableName: String)

    @Throws(IOException::class)
    protected abstract suspend fun serializeTable0(table: Table)

    /**
     * The boolean value provided indicates if the table was loaded or not.
     */
    protected open fun preGet(b: Boolean) {}

    /**
     * The boolean value provided indicates if the table was loaded or not.
     */
    protected open fun preSet(b: Boolean) {}

    @Throws(IOException::class)
    protected suspend fun loadTable(tableName: String): Boolean {
        if (!tables.containsKey(tableName)) {
            loadTable0(tableName)
            return true
        }
        return false
    }

    private suspend fun remove(tableName: String) {
        if (!tables.containsKey(tableName)) {
            return
        }
        val t = tables.remove(tableName)!!
        serializeTable0(t)
    }

    private fun isTableLoaded(tableName: String): Boolean {
        return tables.containsKey(tableName)
    }

    @Throws(IOException::class, InvalidQueryException::class)
    fun createTable(table: Table): Boolean {
        if (db.tableExists(table.name)) {
            return false
        }
        val rowLHM = linkedMapOf<String, HashMap<String, DbValue<*>?>>()
        val dbCol = DbColumn(table.schemaSet)

        for (i in table) {
            val id = i.id.toString()

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

            rowLHM[id] = contentMap
        }

        val row = DbRow(rowLHM)

        val path = db.tablePath(table.name)

        val columnBout = ByteArrayOutputStream()
        val rowBout = ByteArrayOutputStream()

        val mapper = JsonUtil.newBinaryObjectMapper()

        mapper.writeValue(columnBout, dbCol)
        mapper.writeValue(rowBout, row)

        path.createDirectories()

        val row0 = path.append("row0")
        val column = path.append("column.bson")

        row0.writeBytes(rowBout.toByteArray())
        column.writeBytes(columnBout.toByteArray())

        return true
    }


    @Suppress("unused")
    @Throws(IOException::class)
    fun deleteTable(table: Table): Boolean {
        if (!db.tableExists(table.name)) {
            return false
        }

        val path = db.tablePath(table.name)
        return path.deleteRecursively()
    }

    @Throws(IOException::class)
    suspend fun get(tableName: String): Table {
        val b = loadTable(tableName)
        preGet(b)

        return tables[tableName]!!
    }

    @Throws(IOException::class)
    suspend fun set(tableName: String, table: Table) {
        if (!allTables.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }
        val b = loadTable(tableName)
        preSet(b)

        val t = tables[tableName]!!
        if (!t.schemaMatches(table.schema)) {
            throw IllegalArgumentException("The table: $tableName does not match the current schema.")
        }

        tables[tableName] = table
    }

    @Throws(IOException::class)
    suspend fun appendTable(tableName: String, toAppend: Table) {
        if (!allTables.contains(tableName)) {
            throw IllegalArgumentException("The table: $tableName does not exist in this database.")
        }

        val t = this.get(tableName)
        this.set(tableName, t + toAppend)
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
}
