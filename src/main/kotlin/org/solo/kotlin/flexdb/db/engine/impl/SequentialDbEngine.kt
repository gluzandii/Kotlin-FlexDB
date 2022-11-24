package org.solo.kotlin.flexdb.db.engine.impl

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.bson.DbColumnFile
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.internal.AsyncIOUtil
import java.io.IOException
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

class SequentialDbEngine(db: DB) : DbEngine(db) {
    @Throws(IOException::class)
    override suspend fun loadTable0(tableName: String) {
        if (!db.tableExists(tableName)) {
            throw IOException("Table $tableName does not exist")
        }
        val p = db.schema.resolve(tableName)
        val rgx = Regex("row_\\d+")

        val col = DbColumnFile.deserialize(AsyncIOUtil.readBytes(p.resolve("column")))
        val dir = AsyncIOUtil.walk(p) { it.isRegularFile() && rgx.matches(it.name) }


    }

    override suspend fun serializeTable0(table: Table) {
        TODO("Not yet implemented")
    }
}