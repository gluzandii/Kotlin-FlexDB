package org.solo.kotlin.flexdb.internal

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.io.path.Path


inline fun <T, V> T.doAsync(cont: Continuation<V>, value: V, crossinline callback: T.() -> Unit) {
    CompletableFuture.runAsync { callback() }
        .thenAccept { cont.resume(value) }
        .handle { _, e -> e?.printStackTrace() }
}


fun Path.append(other: String, vararg paths: String): Path {
    return Path(this.toString(), other, *paths)
}

@Throws(IOException::class)
fun Path.deleteRecursively(): Boolean {
    return File(this.toString()).deleteRecursively()
}

fun Table.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}

fun Row.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}
