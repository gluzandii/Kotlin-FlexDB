package org.solo.kotlin.flexdb.internal

import java.nio.file.Path
import kotlin.io.path.Path


fun Path.append(other: String, vararg paths: String): Path = Path(this.toString(), other, *paths)

fun ArrayList<*>.contains(id: Int): Boolean {
    return try {
        this[id]
        true
    } catch (ex: Exception) {
        false
    }
}