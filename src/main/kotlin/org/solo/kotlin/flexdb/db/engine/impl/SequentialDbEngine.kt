package org.solo.kotlin.flexdb.db.engine.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.internal.AsyncIOUtil
import java.io.IOException
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name


class SequentialDbEngine(db: DB) : DbEngine(db) {
    @Throws(IOException::class)
    override suspend fun loadTable0(tableName: String) {
        val rgx = super.initLoadTableCall(tableName)

        val sch: Schema
        val dir: Stream<Path>

        coroutineScope {
            val al = awaitAll(
                async {
                    return@async super.loadColumnInTableFolder(tableName).toSchema()
                },
                async {
                    return@async AsyncIOUtil.walk(super.db.schema.resolve(tableName)) { it.isRegularFile() && rgx matches it.name }
                }
            )

            sch = al[0] as Schema
            dir = al[1] as Stream<Path>
        }

        val table = Table(tableName, sch)
        for (i in dir) {
            val r = DbRowFile.deserialize(
                AsyncIOUtil.readBytes(i)
            )
            table.addAll(r.toRows(sch))
        }

        super.tablesMap[tableName] = table
        super.allTablesSet.add(tableName)
    }

    @Throws(IOException::class)
    override suspend fun serializeTable0(table: Table) {
        var (rows, start) = super.initSerializeTableCall(table)

        for (i in rows) {
            val now = start
            start += super.rowsPerFile
            super.writeRowFileInTable(table.name, now, i!!)
        }
    }
}