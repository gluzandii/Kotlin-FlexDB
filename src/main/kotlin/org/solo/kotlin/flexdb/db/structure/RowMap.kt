package org.solo.kotlin.flexdb.db.structure

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import java.util.concurrent.ConcurrentHashMap

class RowMap(val schema: Schema) : Iterable<MutableMap.MutableEntry<Column, DbValue<*>?>> {
    private val content = ConcurrentHashMap<Column, DbValue<*>?>()

    private val cols = ConcurrentHashMap<String, Column>()

    init {
        for (i in schema) {
            content[i] = if (i.hasConstraint(DbConstraint.NotNull)) {
                i.type.default
            } else {
                null
            }

            cols[i.name] = i
        }
    }

    fun containsColumn(col: String): Boolean {
        return cols.containsKey(col)
    }

    fun map(): Map<Column, DbValue<*>?> {
        return content
    }

    operator fun get(col: String): DbValue<*>? {
        return content[cols[col]]
    }

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(col: String, value: DbValue<*>?) {
        if (!cols.containsKey(col)) {
            throw InvalidColumnProvidedException("This column is not part of the schema")
        }
        val c = cols[col]!!

        if ((c.hasConstraint(DbConstraint.NotNull) || c.hasConstraint(DbConstraint.Unique)) && (value == null)) {
            throw NullUsedInNonNullColumnException("The value provided is null, for a NonNull constraint column")
        }

        if ((value != null) && (c.type != value.type)) {
            throw MismatchedTypeException("Cannot put value of type: ${value.type} in ${c.type}")
        }
        content[c] = value
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<Column, DbValue<*>?>> {
        return content.iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is RowMap) {
            return false
        }

        if (schema != other.schema) {
            return false
        }
        if (cols != other.cols) {
            return false
        }
        return content == other.content
    }

    override fun hashCode(): Int {
        return 31 * content.hashCode() + schema.hashCode() + cols.hashCode()
    }
}