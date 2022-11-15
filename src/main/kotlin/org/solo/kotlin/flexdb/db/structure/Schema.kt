package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column

data class Schema(val schemaSet: Set<Column>) : Iterable<Column> {
    override fun iterator(): Iterator<Column> {
        return schemaSet.iterator()
    }
}
