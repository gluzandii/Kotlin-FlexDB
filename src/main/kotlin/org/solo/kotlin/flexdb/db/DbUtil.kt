package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.GlobalData
import org.solo.kotlin.flexdb.internal.append
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

inline fun schemafullPath(root: Path) = root.append("schemafull")

inline fun logsPath(root: Path) = root.append("logs")

inline fun usersPath(root: Path) = root.append("users.json")

inline fun pswdPath(root: Path) = root.append("pswd.txt")

fun dbExists(name: Path): Boolean {
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
}

@Throws(IllegalStateException::class)
fun setGlobalDB(path: Path) {
    if (!dbExists(path)) {
        error("The path: $path is not FlexDB")
    }

    GlobalData.db = DB(
        root = path,
        schema = schemafullPath(path),
        logs = logsPath(path),
        users = usersPath(path),
        pswd = pswdPath(path)
    )
}
