@file:Suppress("BooleanMethodIsAlwaysInverted")

package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.InvalidConfigException
import org.solo.kotlin.flexdb.db.engine.schemafull.SchemafullDbEngine
import org.solo.kotlin.flexdb.db.json.DbConfig
import org.solo.kotlin.flexdb.json.JsonUtil.newObjectMapper
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile


/**
 * Data class that stores the database configuration, and paths in the database.
 */
@Suppress("unused", "BooleanMethodIsAlwaysInverted")
data class DB(val root: Path) {
    val schemafullPath = DbUtil.schemafullPath(root)
    val schemalessPath = DbUtil.indexPath(root)

    val logsPath = DbUtil.logsPath(root)
    val indexPath = DbUtil.indexPath(root)

    @Suppress("MemberVisibilityCanBePrivate")
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
    @Throws(
        IOException::class,
        LinkageError::class,
        InvalidConfigException::class
    )

    @Suppress("unchecked_cast")
    fun schemafullEngine(): SchemafullDbEngine {
        val mapper = newObjectMapper()
        val config = mapper.readValue(configPath.inputStream(), DbConfig::class.java)

        val engineClassString =
            "org.solo.kotlin.flexdb.db.engine.schemafull.impl.${config.schemafull.engine.name}"

        return try {
            val engineClass = Class.forName(engineClassString) as Class<SchemafullDbEngine>
            val const = engineClass.getConstructor(DB::class.java, Int::class.java)

            const.newInstance(this, config.schemafull.engine.rowsPerFile)
        } catch (e: ClassNotFoundException) {
            throw InvalidConfigException("Invalid engine class for schemafull: $engineClassString")
        }
    }
}
