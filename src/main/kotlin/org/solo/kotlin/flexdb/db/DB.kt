package org.solo.kotlin.flexdb.db

import java.nio.file.Path

data class DB(val root: Path, val schema: Path, val logs: Path, val users: Path)
