package org.solo.kotlin.flexdb.db.query.children

import org.solo.kotlin.flexdb.InvalidQueryException
import org.solo.kotlin.flexdb.db.engine.DbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.springframework.context.expression.MapAccessor
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.io.IOException
import java.util.*

@Suppress("unused")
class SelectQuery(
    tableName: String,
    engine: DbEngine,
    where: String,
    columns: JsonCreatePayload?,
    sortingType: SortingType
) : Query<List<Row>>(tableName, engine, where, columns, sortingType) {
    private fun mapContext(mp: Map<*, *>): EvaluationContext {
        val context = StandardEvaluationContext(mp)
        context.addPropertyAccessor(MapAccessor())

        return context
    }

    @Throws(IOException::class, InvalidQueryException::class)
    override suspend fun execute(): List<Row> {
        val expression = parser.parseExpression(where!!)
        val linkedList = LinkedList<Row>()

        for (row in engine.get(tableName)) {
            try {
                val result = expression.getValue(mapContext(row.map()), Boolean::class.java)

                if (result == true) {
                    linkedList.add(row)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return linkedList
    }
}
