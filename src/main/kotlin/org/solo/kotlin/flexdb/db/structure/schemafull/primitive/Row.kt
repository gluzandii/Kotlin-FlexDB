package org.solo.kotlin.flexdb.db.structure.schemafull.primitive

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.schemafull.RowMap
import org.solo.kotlin.flexdb.db.structure.schemafull.Schema
import org.solo.kotlin.flexdb.db.types.DbValue

/**
 *
 */
data class Row(
    val id: Int,
    val schema: Schema
) : Iterable<Map.Entry<Column, DbValue<*>?>>, Comparator<Row>, Comparable<Row> {
    private val content: RowMap = RowMap(schema)

    fun schemaMatches(schema: Schema): Boolean {
        return this.schema == schema
    }

    fun map(): Map<String, DbValue<*>?> {
        val m = content.map()
        val mp = hashMapOf<String, DbValue<*>?>()

        for ((k, v) in m) {
            mp[k.name] = v
        }
        return mp
    }

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(colName: String, value: DbValue<*>?) {
        content[colName] = value
    }

    operator fun get(colName: String): DbValue<*>? {
        return content[colName]
    }

    override fun iterator(): Iterator<Map.Entry<Column, DbValue<*>?>> {
        return content.iterator()
    }

    override fun compare(o1: Row, o2: Row): Int {
        return if (o1.hashCode() == o2.hashCode()) {
            0
        } else if (o1.hashCode() > o2.hashCode()) {
            1
        } else {
            -1
        }
    }

    override fun compareTo(other: Row): Int {
        return compare(this, other)
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Row

        if (id != other.id) return false
        if (schema != other.schema) return false
        if (content != other.content) return false

        return true
    }
}