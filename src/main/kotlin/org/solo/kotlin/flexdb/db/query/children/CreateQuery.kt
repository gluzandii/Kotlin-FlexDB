package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.IOException

class CreateQuery(
    tableName: String,
    engine: DbEngine,
    columns: JsonColumns,
) : Query<Boolean>(tableName, engine, null, columns, SortingType.NONE) {

    @Throws(IOException::class, InvalidQueryException::class)
    override fun execute(): Boolean {
        return true//engine.createTable(Table(table, columns!!.toSchema()))
    }
}
