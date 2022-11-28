package org.solo.kotlin.flexdb.db.query.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.db.engine.schemafull.SchemafullDbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Row
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.springframework.expression.Expression
import java.io.IOException
import java.util.*

/**
 * A query that is used to select rows from a table in the current [org.solo.kotlin.flexdb.db.DB]
 * It does it in parallel for higher efficiency.
 */
class SelectQuery(
    tableName: String,
    engine: SchemafullDbEngine,
    where: String,
    columns: JsonCreatePayload?,
    sortingType: SortingType
) : Query<List<Row>>(tableName, engine, where, columns, sortingType) {
    private val expression: Expression

    init {
        expression = parser.parseExpression(where)
    }

    /**
     * Executes the query, and selects rows from the table in a non-blocking
     * and parallel way.
     */
    @Throws(IOException::class)
    override suspend fun execute(): List<Row> {
        // Filter and only provide those columns that are needed by teh query

        val (linkedList, mutexList) = Pair(LinkedList<Row>(), Mutex())

        coroutineScope {
            for (row in engine.get(tableName)) {
                launch(Dispatchers.Default) {
                    try {
                        val result = expression.getValue(mapContext(row.map()), Boolean::class.java)

                        if (result == true) {
                            mutexList.withLock { linkedList.add(row) }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }


        return linkedList
    }
}
