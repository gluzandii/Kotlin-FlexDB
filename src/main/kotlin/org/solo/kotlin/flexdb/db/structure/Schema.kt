package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumTypes
import java.util.*

data class Schema(val schemaSet: MutableSet<Column>) : Iterable<Column> {
    init {
        if (schemaSet.isEmpty()) {
            throw IllegalArgumentException("Schema cannot be empty")
        }

        val col =
            Column(
                "id",
                DbEnumTypes.Number,
                EnumSet.of(DbConstraint.Unique, DbConstraint.NotNull, DbConstraint.Immutable)!!
            )
        schemaSet.add(col)
    }

    override fun iterator(): Iterator<Column> {
        return schemaSet.iterator()
    }
}
