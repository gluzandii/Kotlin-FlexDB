package org.solo.kotlin.flexdb.structure

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue

class SchemaMap(schema: Set<Column>) {
    private val hm = hashMapOf<Column, DbValue<*>?>()

    val schema: Set<Column>
        get() = hm.keys

    init {
        schema.forEach { hm[it] = null }
    }

    fun containsColumn(col: Column) = hm.containsKey(col)

    operator fun get(col: Column) = hm[col]

    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(col: Column, value: DbValue<*>?) {
        if (!hm.containsKey(col)) {
            throw InvalidColumnProvidedException("This column is not part of the schema")
        }

        if (col.hasConstraint(DbConstraint.NotNull) && value == null) {
            throw NullUsedInNonNullColumnException("The value provided is null, for a NonNull constraint column")
        }
        if ((value != null) && (col.type != value.type)) {
            throw MismatchedTypeException("Cannot put value of type: ${value.type} in ${col.type}")
        }

        hm[col] = value
    }
}