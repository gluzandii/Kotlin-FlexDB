package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.json.query.classes.JsonCreatePayload
import java.io.IOException

class CreateQuery(
    table: String,
    engine: DbEngine,
    columns: List<JsonCreatePayload>,
) : Query<Boolean>(table, engine, null, columns, SortingType.NONE) {

    @Throws(IOException::class, InvalidQueryException::class)
    override fun doQuery(): Boolean {
        if (engine.tableExists(table)) {
            return false
        }

        return true
    }
}
