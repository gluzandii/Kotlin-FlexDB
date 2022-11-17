package org.solo.kotlin.flexdb.db

import org.solo.kotlin.flexdb.internal.append
import java.nio.file.Path
import kotlin.io.path.isDirectory


@Suppress("unused")
class DB(val root: Path) {
    private val schema: Path = DbUtil.schemafullPath(root)
    private val logs: Path = DbUtil.logsPath(root)
    private val index: Path = DbUtil.indexPath(root)

    fun tableExists(name: String): Boolean {
        return tablePath(name).isDirectory()
    }

    fun tablePath(name: String): Path {
        return schema.append(name)
    }
}
