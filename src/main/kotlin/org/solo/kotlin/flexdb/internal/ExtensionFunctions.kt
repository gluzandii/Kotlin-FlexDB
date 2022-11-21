package org.solo.kotlin.flexdb.internal

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.nio.file.Path
import kotlin.io.path.Path


fun Path.append(other: String, vararg paths: String): Path {
    return Path(this.toString(), other, *paths)
}

fun Table.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}

fun Row.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}
