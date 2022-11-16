package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.json.query.classes.JsonCreatePayload

class CreateQuery(
    table: String,
    engine: DbEngine,
    columns: List<JsonCreatePayload>,
) : Query(table, engine, null, columns, SortingType.NONE) {
    
    override fun doQuery(): List<Row> {
        TODO("Not yet implemented")
    }
}
