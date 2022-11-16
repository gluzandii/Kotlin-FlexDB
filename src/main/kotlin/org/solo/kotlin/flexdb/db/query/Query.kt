package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.children.SelectQuery
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.io.IOException

abstract class Query<T>(
    val table: String,
    val engine: DbEngine,
    val where: String?,
    val columns: JsonColumns?,
    val sortingType: SortingType
) {
    protected val parser: ExpressionParser = SpelExpressionParser()

    @Throws(IOException::class, InvalidQueryException::class)
    abstract fun doQuery(): T

    companion object {
        @JvmStatic
        @Throws(InvalidQueryException::class)
        fun create(
            command: String,
            table: String,
            engine: DbEngine,
            where: String?,
            sortingType: SortingType
        ): Query<*> {
            return when (command.lowercase()) {
                "select" -> SelectQuery(table, engine, where ?: "true", sortingType)
                else -> throw InvalidQueryException("Invalid command: $command")
            }
        }
    }
}