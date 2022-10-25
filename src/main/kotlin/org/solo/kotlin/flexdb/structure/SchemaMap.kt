package org.solo.kotlin.flexdb.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.types.DbValue

class SchemaMap(schema: Set<Column>) {
    private val hm = hashMapOf<Column, DbValue<*>?>()

    val schema: Set<Column>
        get() = hm.keys

    init {
        schema.forEach {
            if (it.type == null) {
                throw IllegalArgumentException("Improper column: ${it.name} is used.")
            }

            hm[it] = null
        }
    }

    operator fun get(col: Column) = hm[col]

    @Throws(Throwable::class)
    operator fun set(col: Column, value: DbValue<*>?) {
        if (col.type == null) {
            throw IllegalArgumentException("Improper column used.")
        }
        if (!hm.containsKey(col)) {
            throw IllegalArgumentException("This column is not part of the schema");
        }

        if (value == null) {
            hm[col] = null
            return
        }
        if (col.type != value.type) {
            throw IllegalArgumentException("Cannot put type of: ${value.type} in ${col.type}")
        }

        hm[col] = value
    }
}