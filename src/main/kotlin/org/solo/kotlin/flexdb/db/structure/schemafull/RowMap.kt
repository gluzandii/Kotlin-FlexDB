package org.solo.kotlin.flexdb.db.structure.schemafull

import org.solo.kotlin.flexdb.InvalidColumnProvidedException
import org.solo.kotlin.flexdb.MismatchedTypeException
import org.solo.kotlin.flexdb.NullUsedInNonNullColumnException
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Column
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import java.util.concurrent.ConcurrentHashMap

/**
 * A `map` that stores the data of a row.
 *
 * Where the key is a [Column] and the value is a nullable [DbValue]
 */
internal class RowMap(val schema: Schema) : Iterable<Map.Entry<Column, DbValue<*>?>> {
    /**
     * The actual map that stores the data.
     */
    private val concurrentContentMap = ConcurrentHashMap<Column, DbValue<*>?>()

    /**
     * The map that stores the columns in accordance to its name.
     */
    private val concurrentStringColumnMap = ConcurrentHashMap<String, Column>()

    init {
        for (i in schema) {
            concurrentContentMap[i] = if (i.hasConstraint(DbConstraint.NOTNULL)) {
                i.type
            } else {
                null
            }

            concurrentStringColumnMap[i.name] = i
        }
    }

    /**
     * Checks if the given column name exists in this [RowMap]
     */
    @Suppress("unused")
    fun containsColumn(col: String): Boolean {
        return concurrentStringColumnMap.containsKey(col)
    }

    /**
     * Returns [RowMap.concurrentContentMap] in a [Map], which is immutable.
     */
    fun map(): Map<Column, DbValue<*>?> {
        return concurrentContentMap
    }

    /**
     * Returns the [DbValue] of the column with the given name.
     *
     * @param col The name of the column
     */
    operator fun get(col: String): DbValue<*>? {
        return concurrentContentMap[concurrentStringColumnMap[col]]
    }

    /**
     * Sets the value of the column with the given [DbValue].
     *
     * @throws InvalidColumnProvidedException If the column does not exist in this [RowMap]
     * @throws MismatchedTypeException If the type of the [DbValue] does not match the type of the column
     * @throws NullUsedInNonNullColumnException If the column does not allow null values and the [DbValue] is null
     */
    @Throws(
        NullUsedInNonNullColumnException::class,
        MismatchedTypeException::class,
        InvalidColumnProvidedException::class
    )
    operator fun set(col: String, value: DbValue<*>?) {
        if (!concurrentStringColumnMap.containsKey(col)) {
            throw InvalidColumnProvidedException("This column is not part of the schema")
        }
        val c = concurrentStringColumnMap[col]!!

        if ((c.hasConstraint(DbConstraint.NOTNULL) || c.hasConstraint(DbConstraint.UNIQUE)) && (value == null)) {
            throw NullUsedInNonNullColumnException("The value provided is null, for a NonNull constraint column")
        }

        if ((value != null) && (c.type != value)) {
            throw MismatchedTypeException("Cannot put value of type: $value in ${c.type}")
        }
        concurrentContentMap[c] = value
    }

    /**
     * Returns an [Iterator] that is immutable.
     *
     * Iterates over every column and row in this [RowMap].
     */
    override fun iterator(): Iterator<Map.Entry<Column, DbValue<*>?>> {
        return concurrentContentMap.iterator()
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
        if (concurrentStringColumnMap != other.concurrentStringColumnMap) {
            return false
        }
        return concurrentContentMap == other.concurrentContentMap
    }

    override fun hashCode(): Int {
        return 31 * concurrentContentMap.hashCode() + schema.hashCode() + concurrentStringColumnMap.hashCode()
    }
}