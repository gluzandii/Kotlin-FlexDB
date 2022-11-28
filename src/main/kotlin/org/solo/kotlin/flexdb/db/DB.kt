package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.db.engine.schemafull.SchemafullDbEngine
import org.solo.kotlin.flexdb.db.json.DbConfig
import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile


/**
 * Data class that stores the database configuration, and paths in the database.
 */
@Suppress("unused")
data class DB(val root: Path) {
    val schemafullPath = DbUtil.schemafullPath(root)
    val schemalessPath = DbUtil.indexPath(root)

    val logsPath = DbUtil.logsPath(root)
    val indexPath = DbUtil.indexPath(root)
    val configPath = DbUtil.configPath(root)

    init {
        if (!DbUtil.dbExists(root)) {
            throw IOException("Database does not exist at path: $root")
        }
    }

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
        return schemafullPath.resolve(tableName)
    }

    /**
     * Returns the [SchemafullDbEngine] for this database.
     */
    @Throws(IOException::class)
    fun schemafullEngine(): SchemafullDbEngine? {
        val mapper = newObjectMapper()
        val config = mapper.readValue(configPath.toFile(), DbConfig::class.java)
        return null
    }
}
