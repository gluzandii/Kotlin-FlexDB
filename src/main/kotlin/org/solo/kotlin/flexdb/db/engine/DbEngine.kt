package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException

abstract class DbEngine {
    private val tables = hashMapOf<String, Table>()

    @Throws(IOException::class)
    protected abstract fun loadTables()
}