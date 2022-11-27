package org.solo.kotlin.flexdb.db

import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile


/**
 * Data class that stores the database configuration, and paths in the database.
 */
@Suppress("unused")
data class DB(val root: Path) {
    val schema: Path = DbUtil.schemafullPath(root)
    val logs: Path = DbUtil.logsPath(root)
    val index: Path = DbUtil.indexPath(root)

    /**
     * Checks if the given table name exists in this database
     *
     * @param tableName the table name to check
     */
    fun tableExists(tableName: String): Boolean {
        return tablePath(tableName).isDirectory() && tablePath(tableName).resolve("column").isRegularFile()
    }

    /**
     * Returns the path to the given table name.
     *
     * @param tableName the table name to get the path for
     */
    fun tablePath(tableName: String): Path {
        return schema.resolve(tableName)
    }
}
