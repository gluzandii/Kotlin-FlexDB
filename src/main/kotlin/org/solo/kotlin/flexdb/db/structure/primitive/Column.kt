package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbEnumTypes

@Suppress("unused")
class Column(val name: String, val type: DbEnumTypes, private val constraints: Set<DbConstraint>) {
    override fun hashCode(): Int {
        return name.hashCode()
    }

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

    fun trueEquals(other: Column): Boolean {
        return (other.type == type) && (other.constraints == constraints) && equals(other)
    }

    fun hasConstraint(c: DbConstraint): Boolean {
        return constraints.contains(c)
    }

    fun constraints(): Iterator<DbConstraint> {
        return constraints.iterator()
    }
}