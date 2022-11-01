package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbEnumTypes

class Column(val name: String, val type: DbEnumTypes, val constraints: Set<DbConstraint>) {
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Column) {
            return false
        }
        if (name != other.name) {
            return false
        }

        return true
    }

    fun trueEquals(other: Column): Boolean {
        return (other.type == type) && (other.constraints == constraints) && equals(other)
    }

    fun hasConstraint(c: DbConstraint) = constraints.contains(c)
}