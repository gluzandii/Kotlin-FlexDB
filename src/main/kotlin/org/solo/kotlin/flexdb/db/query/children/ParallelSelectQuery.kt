package org.solo.kotlin.flexdb.db.query.children

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.io.IOException
import java.util.*

class ParallelSelectQuery(
    tableName: String,
    engine: DbEngine,
    where: String,
    columns: JsonCreatePayload?,
    sortingType: SortingType
) : Query<List<Row>>(tableName, engine, where, columns, sortingType) {
    private val expression: Expression

    init {
        expression = parser.parseExpression(where)
    }

    private fun mapContext(mp: Map<*, *>): EvaluationContext {
        val context = StandardEvaluationContext(mp)
        context.addPropertyAccessor(MapAccessor())

        return context
    }

    @Throws(IOException::class, InvalidQueryException::class)
    override suspend fun execute(): List<Row> {
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
