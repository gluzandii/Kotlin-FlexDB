package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.children.CreateQuery
import org.solo.kotlin.flexdb.db.query.children.ParallelSelectQuery
import org.solo.kotlin.flexdb.db.query.children.SelectQuery
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.io.IOException

abstract class Query<T>(
    val tableName: String,
    val engine: DbEngine,
    val where: String?,
    val columns: JsonCreatePayload?,
    val sortingType: SortingType
) {
    protected val parser: ExpressionParser = SpelExpressionParser()

    @Throws(IOException::class, InvalidQueryException::class)
    abstract suspend fun execute(): T

    companion object {
        @JvmStatic
        @Throws(InvalidQueryException::class)
        fun build(
            command: String,
            tableName: String,
            engine: DbEngine,
            where: String?,
            columns: JsonCreatePayload?,
            sortingType: SortingType
        ): Query<*> {
            return when (command.lowercase()) {
                "select" -> ParallelSelectQuery(
                    tableName,
                    engine,
                    where ?: "true",
                    columns,
                    sortingType
                )

                "normal select", "normal-select", "normalselect" -> SelectQuery(
                    tableName,
                    engine,
                    where ?: "true",
                    columns,
                    sortingType
                )

                "create" -> CreateQuery(
                    tableName,
                    engine,
                    columns ?: throw InvalidQueryException("Columns are required for create query")
                )

                else -> throw InvalidQueryException("Invalid command: $command")
            }
        }
    }
}