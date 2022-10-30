package org.solo.kotlin.flexdb.json

import org.solo.kotlin.flexdb.db.structure.primitive.Column

internal class InternalColumn(val name: String, val type: String, val constraints: Set<String>?) {
    override fun hashCode() = name.hashCode()

    override operator fun equals(other: Any?): Boolean {
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

        other as InternalColumn

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }
}