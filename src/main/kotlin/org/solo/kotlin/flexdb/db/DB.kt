package org.solo.kotlin.flexdb.db

import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile


@Suppress("unused")
class DB(val root: Path) {
    val schema: Path = DbUtil.schemafullPath(root)
    private val logs: Path = DbUtil.logsPath(root)
    private val index: Path = DbUtil.indexPath(root)

    fun tableExists(name: String): Boolean {
        return tablePath(name).isDirectory() && tablePath(name).resolve("column").isRegularFile()
    }

    fun tablePath(name: String): Path {
        return schema.resolve(name)
    }
}
