package org.solo.kotlin.flexdb.db.engine.impl

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumnFile
import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.internal.AsyncIOUtil
import java.io.IOException
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

@Suppress("unused")
class ParallelDbEngine(db: DB) : DbEngine(db) {
    @Throws(IOException::class)
    override suspend fun loadTable0(tableName: String) {
        if (!db.tableExists(tableName)) {
            throw IOException("Table $tableName does not exist")
        }
        val p = super.db.schema.resolve(tableName)
        val rgx = Regex("row_\\d+")

        val sch = DbColumnFile.deserialize(AsyncIOUtil.readBytes(p.resolve("column"))).toSchema()
        val dir = AsyncIOUtil.walk(p) { it.isRegularFile() && rgx matches it.name }

        val table = Table(tableName, sch)
        for (i in dir) {
            val r = DbRowFile.deserialize(AsyncIOUtil.readBytes(i))
            table.addAll(r.toRows(sch))
        }

        super.tablesMap[tableName] = table
        super.allTablesSet.add(tableName)
    }

    @Throws(IOException::class)
    override suspend fun serializeTable0(table: Table) {
        if (!db.tableExists(table.name)) {
            throw IOException("Table ${table.name} does not exist")
        }
    }
}