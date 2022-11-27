package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.InvalidTypeException

/**
 * The enum that stores all possible datatypes in FlexDB
 */
enum class DbEnumType {
    STRING,
    NUMBER,
    DECIMAL,
    BOOLEAN;

    /**
     * Returns the default value of a given datatype.
     */
    val default: DbValue<*>
        get() {
            return when (this) {
                STRING -> DbString("")
                NUMBER -> DbNumber(0)
                DECIMAL -> DbDecimal(0.0)
                BOOLEAN -> DbBoolean(false)
            }
        }
}

// CREATE JSON SERIALIZATION AND DESERIALIZATION LOGIC

/**
 * The base class for any database value in FlexDB
 */
sealed class DbValue<T>(val value: T, val type: DbEnumType) {
    override fun hashCode(): Int {
        return value.hashCode()
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type) {
            return false
        }

        return value == other.value
    }

    /**
     * Compares 2 values of [DbValue]
     *
     * @param other the other value to compare to
     */
    @Throws(InvalidTypeException::class)
    operator fun compareTo(other: DbValue<*>): Int {
        if (this === other) {
            return 0
        }
        if (other.type != type) {
            throw InvalidTypeException("The DbValue provided has a type not equal to this.")
        }

        return when (type) {
            DbEnumType.STRING -> other.value.toString().compareTo(value.toString())
            DbEnumType.DECIMAL -> (other.value as Double).compareTo(value as Double)
            DbEnumType.NUMBER -> (other.value as Long).compareTo(value as Long)
            DbEnumType.BOOLEAN -> (other.value as Boolean).compareTo(value as Boolean)
        }
    }
}

/**
 * [DbValue] which stores a string.
 */
class DbString(value: String) : DbValue<String>(value, DbEnumType.STRING)

/**
 * [DbValue] which stores a whole number.
 */
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumType.NUMBER)

/**
 * [DbValue] which stores a decimal.
 */
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumType.DECIMAL)

/**
 * [DbValue] which stores a boolean.
 */
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumType.BOOLEAN)
