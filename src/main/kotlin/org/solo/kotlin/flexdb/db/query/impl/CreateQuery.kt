package org.solo.kotlin.flexdb.db.query.impl

import org.solo.kotlin.flexdb.db.engine.schemafull.SchemafullDbEngine
import org.solo.kotlin.flexdb.db.query.Query
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.schemafull.Table
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.solo.kotlin.flexdb.internal.toSchema
import java.io.IOException

/**
 * A query that is used to create a table in the current [org.solo.kotlin.flexdb.db.DB]
 */
class CreateQuery(
    tableName: String,
    engine: SchemafullDbEngine,
    columns: JsonCreatePayload,
) : Query<Unit>(tableName, engine, null, columns, SortingType.NONE to null) {

    /**
     * Executes the query, and creates a table in the database in a non-blocking way.
     *
     * @throws IOException if the table already exists, or an error occurred while creating it.
     */
    @Throws(IOException::class)
    override suspend fun execute() {
        engine.createTable(Table(tableName, columns!!.toSchema()))
    }
}
