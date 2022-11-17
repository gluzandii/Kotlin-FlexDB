package org.solo.kotlin.flexdb.internal

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.Table
import org.solo.kotlin.flexdb.db.structure.primitive.Row
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path


fun Path.append(other: String, vararg paths: String): Path = Path(this.toString(), other, *paths)

fun Path.appendFile(other: String, vararg paths: String): File = Path(this.toString(), other, *paths).toFile()

fun Table.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}

fun Row.schemaMatches(schema: Schema): Boolean {
    return this.schema == schema
}
