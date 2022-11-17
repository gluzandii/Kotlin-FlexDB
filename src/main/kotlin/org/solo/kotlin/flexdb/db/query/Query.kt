package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.children.CreateQuery
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
    abstract fun execute(): T

    companion object {
        @JvmStatic
        @Throws(InvalidQueryException::class)
        fun build(
            command: String,
            table: String,
            engine: DbEngine,
            where: String?,
            columns: JsonColumns?,
            sortingType: SortingType
        ): Query<*> {
            return when (command.lowercase()) {
                "select" -> SelectQuery(table, engine, where ?: "true", sortingType)
                "create" -> CreateQuery(
                    table,
                    engine,
                    columns ?: throw InvalidQueryException("Columns are required for create query")
                )

                else -> throw InvalidQueryException("Invalid command: $command")
            }
        }
    }
}