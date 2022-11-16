package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.children.SelectQuery
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.json.query.classes.JsonCreatePayload
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser

abstract class Query(
    val table: String,
    val engine: DbEngine,
    val where: String?,
    val columns: List<JsonCreatePayload>?,
    val sortingType: SortingType
) {
    protected val parser: ExpressionParser = SpelExpressionParser()

    @Throws(InvalidQueryException::class)
    abstract fun doQuery(): List<Row>

    companion object {
        @JvmStatic
        @Throws(InvalidQueryException::class)
        fun create(
            command: String,
            table: String,
            engine: DbEngine,
            where: String?,
            sortingType: SortingType
        ): Query {
            return when (command.lowercase()) {
                "select" -> SelectQuery(table, engine, where ?: "true", sortingType)
                else -> throw InvalidQueryException("Invalid command: $command")
            }
        }
    }
}