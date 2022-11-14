package org.solo.kotlin.flexdb.db.engine

import java.io.IOException

abstract class DbEngine {
    @Throws(IOException::class)
    protected abstract fun loadTables()
}