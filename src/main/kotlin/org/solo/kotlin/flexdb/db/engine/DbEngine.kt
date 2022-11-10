package org.solo.kotlin.flexdb.db.engine

import java.io.IOException

abstract class DbEngine protected constructor(val tables: TableMap) {
    @Throws(IOException::class)
    abstract fun loadTables()
}
