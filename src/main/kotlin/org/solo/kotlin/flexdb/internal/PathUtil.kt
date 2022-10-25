package org.solo.kotlin.flexdb.internal

import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("unused")
fun Path.append(other: String): Path = Path(this.toString(), other)

fun Path.append(other: Path): Path = append(other.toString())