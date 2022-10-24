package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.internal.append
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

inline fun schemafullPath(root: Path) = root.append("schemafull")

inline fun logsPath(root: Path) = root.append("schemafull")

inline fun usersPath(root: Path) = root.append("schemafull")

fun dbExists(name: Path): Boolean {
    if (!name.isDirectory()) {
        return false
    }

    val schema = schemafullPath(name)
    val logs = logsPath(name)
    val users = usersPath(name)

    if (!schema.isDirectory()) {
        return false
    }
    if (!logs.isDirectory()) {
        return false
    }
    if (!users.isRegularFile()) {
        return false
    }

    return true
}

fun dbExists(name: String) = dbExists(Path(name))
