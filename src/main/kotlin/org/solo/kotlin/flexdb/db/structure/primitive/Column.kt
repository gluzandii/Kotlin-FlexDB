package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbEnumType
import java.util.*

/**
 * A dataclass that is used for storing a column and its metadata, not the contents of the column.
 */
data class Column(val name: String, val type: DbEnumType, private val constraints: EnumSet<DbConstraint>) {
    /**
     * Returns a [Set] containing its contents but in a [String] format.
     */
    val stringConstraints: Set<String>
        get() {
            val ms = hashSetOf<String>()
            for (i in constraints) {
                ms.add(i.name)
            }

            return ms
        }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Column) {
            return false
        }

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    /**
     * Checks if the given [DbConstraint] is present in this [Column].
     */
    fun hasConstraint(c: DbConstraint): Boolean {
        return constraints.contains(c)
    }
}