package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.GlobalData
import org.solo.kotlin.flexdb.InvalidPasswordProvidedException
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.*

object DbUtil {
    @JvmStatic
    fun schemafullPath(root: Path): Path {
        return root.resolve("schemafull")
    }

    @JvmStatic
    fun logsPath(root: Path): Path {
        return root.resolve("logs")
    }

    @JvmStatic
    fun indexPath(root: Path): Path {
        return root.resolve("index")
    }

    @JvmStatic
    fun dbExists(name: Path): Boolean {
        try {
            if (!name.isDirectory()) {
                return false
            }

            val schema = schemafullPath(name)
            val logs = logsPath(name)
            val index = indexPath(name)

            if (!schema.isDirectory()) {
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

    @JvmStatic
    @Throws(InvalidPasswordProvidedException::class)
    fun setGlobalDB(path: Path): DB {
        if (!dbExists(path)) {
            error("The path: $path is not FlexDB")
        }

        GlobalData.db = DB(
            root = path,
        )
        return GlobalData.db!!
    }

    @JvmStatic
    @Throws(IOException::class)
    fun createDB(path: Path): DB? {
        if (dbExists(path)) {
            return null
        }

        val name = path.name
        val schema = schemafullPath(path)
        val logs = logsPath(path)
        val index = indexPath(path)

        if (path.isDirectory() && !dbExists(path)) {
            throw IllegalArgumentException("Invalid path: $path given, cannot create FlexDB here.")
        }
        if (path.isRegularFile()) {
            throw IllegalArgumentException("The path: $path leads to a file.")
        }

        schema.createDirectories()
        logs.createDirectories()
        index.createDirectories()

        logs.resolve("log1.log").writeText("[${LocalDateTime.now()}] - DB: \"$name\" created.")
        return DB(
            path,
        )
    }
}
