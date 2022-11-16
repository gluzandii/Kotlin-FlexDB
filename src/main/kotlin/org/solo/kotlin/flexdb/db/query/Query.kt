package org.solo.kotlin.flexdb.db.query

import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.EvaluationContext
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.util.*

data class Query(
    val table: String,
    val engine: DbEngine,
    val where: String,
    val sortingType: SortingType
) {
    private val parser: ExpressionParser = SpelExpressionParser()

    private inline fun mapContext(mp: Map<*, *>): EvaluationContext {
        val context = StandardEvaluationContext(mp)
        context.addPropertyAccessor(MapAccessor())

        return context
    }

    @Throws(InvalidQueryException::class)
    fun doQuery(): List<Row> {
        val expression = parser.parseExpression(where)

        val list = mutableListOf<Row>()
        val mutexList = Mutex()

        runBlocking {
            val jobs = LinkedList<Job>()
            for (table in engine.getTables(table)) {
                jobs.add(launch {
                    try {
                        for (row in table) {
                            val result = expression.getValue(mapContext(row.map()), Boolean::class.java)

                            if (result == true) {
                                mutexList.withLock { list.add(row) }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                })
            }

            joinAll(*jobs.toTypedArray())
        }

        return list
    }
}
