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

    init {
        for (i in schema) {
            content[i] = if (i.hasConstraint(DbConstraint.NotNull)) {
                i.type.default
            } else {
                null
            }
        }
    }

    fun containsColumn(col: Column): Boolean {
        return content.containsKey(col)
    }

    fun cloneMap(): Map<Column, DbValue<*>?> {
        return content
    }

    operator fun get(col: Column): DbValue<*>? {
        return content[col]
    }

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(col: Column, value: DbValue<*>?) {
        if (!content.containsKey(col)) {
            throw InvalidColumnProvidedException("This column is not part of the schema")
        }

        if (col.hasConstraint(DbConstraint.Immutable)) {
            throw InvalidColumnProvidedException("Cannot change value for an immutable column.")
        }
        if ((col.hasConstraint(DbConstraint.NotNull) || col.hasConstraint(DbConstraint.Unique)) && (value == null)) {
            throw NullUsedInNonNullColumnException("The value provided is null, for a NonNull constraint column")
        }

        if ((value != null) && (col.type != value.type)) {
            throw MismatchedTypeException("Cannot put value of type: ${value.type} in ${col.type}")
        }
        content[col] = value
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
        return content == other.content
    }

    override fun hashCode(): Int {
        return 31 * content.hashCode() + schema.hashCode()
    }
}