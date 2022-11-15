package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.Crypto
import org.solo.kotlin.flexdb.GlobalData
import org.solo.kotlin.flexdb.InvalidPasswordProvidedException
import org.solo.kotlin.flexdb.internal.append
import org.solo.kotlin.flexdb.json.JsonUtil
import java.io.IOException
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.io.path.*

object DbUtil {
    @JvmStatic
    fun schemafullPath(root: Path): Path {
        return root.append("schemafull")
    }

    @JvmStatic
    fun indexFullPath(root: Path): Path {
        return root.append("index")
    }

    @JvmStatic
    fun logsPath(root: Path): Path {
        return root.append("logs")
    }

    @JvmStatic
    fun usersPath(root: Path): Path {
        return root.append("users.bson")
    }

    @JvmStatic
    fun pswdPath(root: Path): Path {
        return root.append("pswd.txt")
    }

    @JvmStatic
    fun dbExists(name: Path): Boolean {
        try {
            if (!name.isDirectory()) {
                return false
            }

            val schema = schemafullPath(name)
            val logs = logsPath(name)
            val users = usersPath(name)
            val pswd = pswdPath(name)

            if (!schema.isDirectory()) {
                return false
            }
            if (!logs.isDirectory()) {
                return false
            }
            if (!users.isRegularFile()) {
                return false
            }

            return pswd.isRegularFile()
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
        if (!canAccessDB(path)) {
            throw InvalidPasswordProvidedException("The password: ${GlobalData.pswd ?: "null"} is invalid.")
        }

        GlobalData.db = DB(
            root = path,
            p = GlobalData.pswd!!
        )
        return GlobalData.db!!
    }

    @JvmStatic
    fun canAccessDB(path: Path): Boolean {
        try {
            if (!dbExists(path)) {
                return false
            }

            val pswd = pswdPath(path)
            val readAll = pswd.readText()

            return Crypto.passwordMatches(GlobalData.pswd!!, readAll)
        } catch (ex: Exception) {
            return false
        }

    }

    @JvmStatic
    @Throws(IOException::class)
    fun createDB(path: Path, p: String): DB? {
        if (dbExists(path)) {
            return null
        }

        val name = path.name
        val schema = schemafullPath(path)
        val logs = logsPath(path)
        val users = usersPath(path)
        val pswd = pswdPath(path)
        val hashed = Crypto.hashPassword(p)

        if (path.isDirectory() && !dbExists(path)) {
            throw IllegalArgumentException("Invalid path: $path given, cannot create FlexDB here.")
        }
        if (path.isRegularFile()) {
            throw IllegalArgumentException("The path: $path leads to a file.")
        }

        schema.createDirectories()
        logs.createDirectories()

        logs.append("log1.log").writeText("[${LocalDateTime.now()}] - DB: \"$name\" created.")
        pswd.writeText(hashed)
        users.writeBytes(JsonUtil.binaryJsonSerialize(mapOf(Pair("root", hashed))))

        return DB(
            path,
            p
        )
    }
}
