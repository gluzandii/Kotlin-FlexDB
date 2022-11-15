package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException

abstract class DbEngine(protected val db: DB) {
    private val tables = hashMapOf<String, Table>()

    @Throws(IOException::class)
    protected abstract fun loadTables()
}