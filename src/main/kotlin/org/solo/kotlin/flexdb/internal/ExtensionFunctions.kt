package org.solo.kotlin.flexdb.internal

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path


fun Path.append(other: String, vararg paths: String): Path = Path(this.toString(), other, *paths)

fun Path.appendFile(other: String, vararg paths: String): File = Path(this.toString(), other, *paths).toFile()
