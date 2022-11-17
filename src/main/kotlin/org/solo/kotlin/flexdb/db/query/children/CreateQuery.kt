package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.bson.DbColumn
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.internal.appendFile
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import org.solo.kotlin.flexdb.zip.ZipUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

class CreateQuery(
    table: String,
    engine: DbEngine,
    columns: JsonColumns,
) : Query<Boolean>(table, engine, null, columns, SortingType.NONE) {

    @Throws(IOException::class, InvalidQueryException::class)
    override fun doQuery(): Boolean {
        if (engine.tableExists(table)) {
            return false
        }
        if (table.isEmpty() || table.isBlank()) {
            throw InvalidQueryException("Table name cannot be empty")
        }

        val dbCol = DbColumn(columns ?: return false)
        val path = engine.tablePath(table) ?: return false
        val bout = ByteArrayOutputStream()

        val mapper = JsonUtil.newBinaryObjectMapper()
        mapper.writeValue(bout, dbCol)

        val file =
            engine.root.appendFile("temp_${Random(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)).nextInt()}")
        file.writeBytes(bout.toByteArray())

        ZipUtil.compress(path, engine.password, file)
        return true
    }
}
