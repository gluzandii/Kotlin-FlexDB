package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column

data class Schema(val schema: Set<Column>) : Iterable<Column> {
    override fun iterator(): Iterator<Column> {
        return schema.iterator()
    }
}
