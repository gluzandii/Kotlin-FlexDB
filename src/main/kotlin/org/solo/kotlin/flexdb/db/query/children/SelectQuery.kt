package org.solo.kotlin.flexdb.db.query.children

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.util.*

class SelectQuery(
    table: String,
    engine: DbEngine,
    where: String,
    sortingType: SortingType
) : Query(table, engine, where, null, sortingType) {
    private fun mapContext(mp: Map<*, *>): EvaluationContext {
        val context = StandardEvaluationContext(mp)
        context.addPropertyAccessor(MapAccessor())

        return context
    }

    @Throws(InvalidQueryException::class)
    override fun doQuery(): List<Row> {
        val expression = parser.parseExpression(where!!)
        val (linkedList, mutexList) = Pair(LinkedList<Row>(), Mutex())

        runBlocking {
            val jobs = LinkedList<Job>()
            for (table in engine.getTables(table)) {
                jobs.add(launch {
                    try {
                        for (row in table) {
                            val result = expression.getValue(mapContext(row.map()), Boolean::class.java)

                            if (result == true) {
                                mutexList.withLock { linkedList.add(row) }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                })
            }

            joinAll(*jobs.toTypedArray())
        }

        return linkedList
    }
}
