package org.solo.kotlin.flexdb.db.engine.schemafull.impl

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.engine.schemafull.SchemafullDbEngine
import org.solo.kotlin.flexdb.db.structure.schemafull.Schema
import org.solo.kotlin.flexdb.db.structure.schemafull.Table
import org.solo.kotlin.flexdb.internal.AsyncIOUtil
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name


/**
 * A parallel non-blocking implementation of [SchemafullDbEngine].
 *
 * Where the tables are loaded and serialized parallely.
 */
@Suppress("unused", "unchecked_cast")
class ParallelSchemafullDbEngine(
    db: DB,
    rowsPerFile: Int,
) : SchemafullDbEngine(db, rowsPerFile) {
    @Throws(IOException::class)
    override suspend fun loadTable0(tableName: String) {
        val rgx = initLoadTableCall(tableName)
        coroutineScope {
            val al = awaitAll(
                async {
                    return@async super.loadColumnInTableFolder(tableName).toSchema()
                },
                async {
                    return@async AsyncIOUtil.walk(super.db.schemafullPath.resolve(tableName)) {
                        return@walk it.isRegularFile() && rgx matches it.name
                    }
                }
            )


            val sch = al[0] as Schema
            val dir = al[1] as Stream<Path>

            val (table, tableMutex) = Pair(Table(tableName, sch), Mutex())
            val list = LinkedList<Job>()

            for (i in dir) {
                list.addLast(
                    launch(Dispatchers.Default) {
                        val r = DbRowFile.deserialize(
                            AsyncIOUtil.readBytes(i)
                        )
                        tableMutex.withLock { table.addAll(r.toRows(sch)) }
                    }
                )
            }
            list.joinAll()

            super.tablesMap[tableName] = table
            super.allTablesInThisDatabaseSet.add(tableName)
        }
    }

    @Throws(IOException::class)
    override suspend fun serializeTable0(table: Table) {
        var (rows, start) = super.initSerializeTableCall(table)

        coroutineScope {
            val list = LinkedList<Job>()
            for (i in rows) {
                val now = start
                start += super.rowsPerFile

                list.addLast(
                    launch(Dispatchers.Default) {
                        super.writeRowFileInTable(table.name, now, i!!)
                    }
                )
            }

            list.joinAll()
        }
    }
}