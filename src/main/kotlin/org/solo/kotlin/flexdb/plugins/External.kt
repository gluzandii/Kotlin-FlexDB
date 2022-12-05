package org.solo.kotlin.flexdb.plugins

import org.solo.kotlin.flexdb.db.bson.DbRowFile
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Row

fun List<Row>.toSerializable(): DbRowFile {
    val rows = DbRowFile()
    for (i in this) {
        rows[i.id] = i.map()
    }

    return rows
}