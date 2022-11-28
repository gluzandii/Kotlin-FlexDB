package org.solo.kotlin.flexdb.db.structure.schemafull

import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Column
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumType
import java.util.*

/**
 * A dataclass that is used for storing the schema of a table.
 */
data class Schema(val schemaSet: MutableSet<Column>) : Iterable<Column> {

    init {
        if (schemaSet.isEmpty()) {
            throw IllegalArgumentException("Schema cannot be empty")
        }

        val col =
            Column(
                "id",
                DbEnumType.NUMBER,
                EnumSet.of(DbConstraint.UNIQUE, DbConstraint.NOTNULL)!!
            )
        schemaSet.add(col)
    }

    /**
     * Returns an [Iterator] with all the [Column] in this schema.
     */
    override fun iterator(): Iterator<Column> {
        return schemaSet.iterator()
    }
}
