package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import org.solo.kotlin.flexdb.json.query.classes.toSchema
import java.io.IOException

class CreateQuery(
    table: String,
    engine: DbEngine,
    columns: JsonColumns,
) : Query<Boolean>(table, engine, null, columns, SortingType.NONE) {

    @Throws(IOException::class, InvalidQueryException::class)
    override fun doQuery(): Boolean {
        return engine.createTable(Table(table, columns!!.toSchema()))
    }
}
