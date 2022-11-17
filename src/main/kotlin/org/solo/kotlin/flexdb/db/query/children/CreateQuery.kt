package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.bson.DbColumn
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.ByteArrayOutputStream
import java.io.IOException

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
        val dbCol = DbColumn(columns ?: return false)
        val path = engine.tablePath(table) ?: return false
        val bout = ByteArrayOutputStream()

        val mapper = JsonUtil.newBinaryObjectMapper()
        mapper.writeValue(bout, dbCol)

        return true
    }
}
