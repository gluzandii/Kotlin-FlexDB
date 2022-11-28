package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.InvalidPasswordProvidedException
import org.solo.kotlin.flexdb.ThisFlexDBInstance
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.*

/**
 * Static utilities for getting `paths` inside the database.
 *
 * It has methods to check if database is a proper one, or to create databases.
 */
object DbUtil {
    /**
     * Schemafull path in db.
     *
     * ```
     *  Schemafull Path: ../{db_name}/schemafull
     *  ```
     *
     *  @param root the name of the database
     */
    @JvmStatic
    fun schemafullPath(root: Path): Path {
        return root.resolve("schemafull")
    }

    /**
     * Schemafull path in db.
     *
     * ```
     *  Schemafull Path: ../{db_name}/schemaless
     *  ```
     *
     *  @param root the name of the database
     */
    @JvmStatic
    fun schemalessPath(root: Path): Path {
        return root.resolve("schemaless")
    }

    /**
     * Logs path in db.
     *
     * ```
     *  Logs Path: ../{db_name}/logs
     *  ```
     *
     *  @param root the name of the database
     */
    @JvmStatic
    fun logsPath(root: Path): Path {
        return root.resolve("logs")
    }

    /**
     * Index path in db.
     *
     * ```
     *  Index Path: ../{db_name}/index
     *  ```
     *
     *  @param root the name of the database
     */
    @JvmStatic
    fun indexPath(root: Path): Path {
        return root.resolve("index")
    }

    /**
     * Checks if the given [Path] leads to a valid database.
     *
     * @param name the path to the database
     */
    @JvmStatic
    fun dbExists(name: Path): Boolean {
        try {
            if (!name.isDirectory()) {
                return false
            }

            val schemafull = schemafullPath(name)
            val schemaless = schemalessPath(name)
            val logs = logsPath(name)
            val index = indexPath(name)

            if (!schemafull.isDirectory()) {
                return false
            }
            if (!schemaless.isDirectory()) {
                return false
            }
            if (!index.isDirectory()) {
                return false
            }
            return logs.isRegularFile()
        } catch (ex: Exception) {
            return false
        }
    }

    /**
     * Sets the [DB] object for this FlexDB instance on the JVM
     *
     * @param path path to db
     */
    @JvmStatic
    @Throws(InvalidPasswordProvidedException::class)
    fun setThisInstanceDb(path: Path): DB {
        if (!dbExists(path)) {
            error("The path: $path is not FlexDB")
        }

        ThisFlexDBInstance.thisInstanceDb = DB(
            root = path,
        )
        return ThisFlexDBInstance.thisInstanceDb!!
    }

    /**
     * Creates a new database at the given path.
     *
     * @param path the path to the database
     */
    @JvmStatic
    @Throws(IOException::class)
    fun createDB(path: Path, setGlobal: Boolean = false): DB? {
        if (dbExists(path)) {
            return null
        }

        val name = path.name
        val schemafull = schemafullPath(path)
        val schemaless = schemalessPath(path)
        val logs = logsPath(path)
        val index = indexPath(path)

        if (path.isDirectory() && !dbExists(path)) {
            throw IllegalArgumentException("Invalid path: $path given, cannot create FlexDB here.")
        }
        if (path.isRegularFile()) {
            throw IllegalArgumentException("The path: $path leads to a file.")
        }

        schemafull.createDirectories()
        schemaless.createDirectories()
        logs.createDirectories()
        index.createDirectories()

        logs.resolve("log1.log").writeText("[${LocalDateTime.now()}] - DB: \"$name\" created.")
        val d = DB(
            path,
        )

        if (setGlobal) {
            ThisFlexDBInstance.thisInstanceDb = d
        }
        return d
    }
}
