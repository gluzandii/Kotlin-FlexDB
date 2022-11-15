package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.primitive.Row

data class Query(
    val table: String,
    val engine: DbEngine,
    val where: String,
    val sortingType: SortingType
) {
    fun doQuery(): List<Row> {
        return listOf()
    }
}
