package org.solo.kotlin.flexdb.db.query.children

import kotlinx.coroutines.Dispatchers
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
import java.io.IOException
import java.util.*

class SelectQuery(
    tableName: String,
    engine: DbEngine,
    where: String,
    sortingType: SortingType
) : Query<List<Row>>(tableName, engine, where, null, sortingType) {
    private fun mapContext(mp: Map<*, *>): EvaluationContext {
        val context = StandardEvaluationContext(mp)
        context.addPropertyAccessor(MapAccessor())

        return context
    }

    @Throws(IOException::class, InvalidQueryException::class)
    override fun execute(): List<Row> {
        val expression = parser.parseExpression(where!!)
        val (linkedList, mutexList) = Pair(LinkedList<Row>(), Mutex())

        runBlocking {
            // If enough tables are loaded in, this table will be unloaded
            // Make sure that if a table is accesses, move it to the end of the queue
            // Anywhere coroutines are used, try to make them suspending, to make the most out of the threads
            launch(Dispatchers.Default) {
                for (row in engine.get(tableName)) {
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
