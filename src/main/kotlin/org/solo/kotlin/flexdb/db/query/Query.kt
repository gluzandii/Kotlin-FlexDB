package org.solo.kotlin.flexdb.db.query

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.EvaluationContext
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

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
        var exp: Exception? = null

        runBlocking {
            for (i in engine.getTables(table)) {
                launch {
                    try {
                        for (j in i) {
                            val result = expression.getValue(mapContext(j.map()), Boolean::class.java)

                            if (result == true) {
                                synchronized(list) {
                                    list.add(j)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        exp = InvalidQueryException(ex.message ?: "Unknown error")
                    }
                }
            }
        }

        throw (exp ?: return list)
    }
}
