package org.solo.kotlin.flexdb.db.structure.primitive

import org.solo.kotlin.flexdb.db.types.DbEnumTypes

class Column(val name: String, val type: DbEnumTypes, private val constraints: Set<Constraint>) {
    override fun hashCode() = name.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column
        if (name != other.name) return false

        return true
    }

    @Suppress("unused")
    fun trueEquals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    fun hasConstraint(c: Constraint) = constraints.contains(c)
}