package org.solo.kotlin.flexdb.db.query

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser

data class Query(
    val table: String,
    val engine: DbEngine,
    val where: String,
    val sortingType: SortingType
) {
    private val parser: ExpressionParser = SpelExpressionParser()

    @Throws(InvalidQueryException::class)
    fun doQuery(): List<Row> {
        val expression = parser.parseExpression(where)
        val list = mutableListOf<Row>()

        runBlocking {
            val tables = engine.getTables(table)

            for (i in tables) {
                launch {
                    for (j in i) {
                        val result = expression.getValue(j)

                        if (result == true) {
                            synchronized(list) {
                                list.add(j)
                            }
                        }
                    }
                }
            }
        }

        return list
    }
}
