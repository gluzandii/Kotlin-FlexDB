package org.solo.kotlin.flexdb.db.engines

import org.solo.kotlin.flexdb.db.DB
import org.solo.kotlin.flexdb.db.structure.Table
import java.io.IOException
import kotlin.io.path.deleteIfExists

/**
 * A thread-safe abstract db-engine.
 */
abstract class DbEngine(val db: DB) {
    protected abstract fun loadTable0(name: String): Table

    fun loadTable(name: String): Table {
        synchronized(db) {
            return loadTable0(name)
        }
    }

    @Throws(IOException::class)
    fun deleteTable(name: String): Boolean {
        synchronized(db) {
            return (db.tablePath(name) ?: return false).deleteIfExists()
        }
    }
}
