package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import java.io.IOException
import java.util.*

/**
 * A query that is used to select rows from a table in the current [org.solo.kotlin.flexdb.db.DB]
 */

class SelectQuery(
    tableName: String,
    engine: DbEngine,
    where: String,
    columns: JsonCreatePayload?,
    sortingType: SortingType
) : Query<List<Row>>(tableName, engine, where, columns, sortingType) {

    /**
     * Executes the query, and selects rows from the table in a non-blocking way.
     */
    @Throws(IOException::class)
    override suspend fun execute(): List<Row> {
        val expression = parser.parseExpression(where!!)
        val linkedList = LinkedList<Row>()

        for (row in engine.get(tableName)) {
            try {
                val result = expression.getValue(mapContext(row.map()), Boolean::class.java)

                if (result == true) {
                    linkedList.add(row)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return linkedList
    }
}
