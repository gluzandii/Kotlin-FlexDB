@file:Suppress("EqualsOrHashCode")

package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.InvalidEmailException
import org.solo.kotlin.flexdb.InvalidTypeException

private val emailRegex = Regex(
    "[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[A-Z]{2}|com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum)\\b"
)

fun emailMatches(email: String): String? {
    return if (emailRegex matches email) {
        email
    } else {
        null
    }
}

/**
 * The enum that stores all possible datatypes in FlexDB
 */
enum class DbEnumType {
    EMAIL,
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
                EMAIL -> DbEmail("")
                STRING -> DbString("")
                NUMBER -> DbNumber(0)
                DECIMAL -> DbDecimal(0.0)
                BOOLEAN -> DbBoolean(false)
            }
        }
}

/**
 * The base class for any database value in FlexDB
 */
sealed class DbValue<T>(
    val value: T,
    val type: DbEnumType,
) : Comparable<DbValue<*>>, Comparator<DbValue<*>> {
    override fun hashCode(): Int {
        return value.hashCode()
    }

    abstract override fun equals(other: Any?): Boolean

    /**
     * Compares 2 values of [DbValue]
     *
     * @param other the other value to compare to
     */
    @Throws(InvalidTypeException::class)
    override operator fun compareTo(other: DbValue<*>): Int {
        if (this === other) {
            return 0
        }
        if (other.type != type) {
            throw InvalidTypeException("The DbValue provided has a type not equal to this.")
        }

        return when (type) {
            DbEnumType.STRING, DbEnumType.EMAIL -> other.value.toString().compareTo(value.toString())
            DbEnumType.DECIMAL -> (other.value as Double).compareTo(value as Double)
            DbEnumType.NUMBER -> (other.value as Long).compareTo(value as Long)
            DbEnumType.BOOLEAN -> (other.value as Boolean).compareTo(value as Boolean)
        }
    }

    /**
     * Compares 2 values of [DbValue]
     *
     * @param o1 the first value to compare
     * @param o2 the second value to compare
     */
    override fun compare(o1: DbValue<*>, o2: DbValue<*>): Int {
        return o1.compareTo(o2)
    }
}

/**
 * [DbValue] which stores a string.
 */
class DbString(value: String) : DbValue<String>(value, DbEnumType.STRING) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is String) {
            return value == other
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type || other.value !is String) {
            return false
        }

        return value == other.value
    }
}

/**
 * [DbValue] which stores an email.
 */
class DbEmail(
    value: String,
) : DbValue<String>(
    emailMatches(value) ?: throw InvalidEmailException("The email provided: $value is invalid."),
    DbEnumType.STRING
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is String) {
            return if (emailMatches(other) != null) {
                value == other
            } else {
                false
            }
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type || other.value !is String) {
            return false
        }

        return if (emailMatches(other.value) != null) {
            value == other.value
        } else {
            false
        }
    }
}

/**
 * [DbValue] which stores a whole number.
 */
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumType.NUMBER) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Byte || other is Short || other is Int || other is Long) {
            return value == other
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type || (other.value !is Byte && other.value !is Short && other.value !is Int && other.value !is Long)) {
            return false
        }

        return value == other.value
    }
}

/**
 * [DbValue] which stores a decimal.
 */
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumType.DECIMAL) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Byte || other is Short || other is Int || other is Long || other is Float || other is Double) {
            return value == other
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type || (other.value !is Byte && other.value !is Short && other.value !is Int && other.value !is Long && other.value !is Float && other.value !is Double)) {
            return false
        }

        return value == other.value
    }
}

/**
 * [DbValue] which stores a boolean.
 */
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumType.BOOLEAN) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        
        if (other is Boolean) {
            return value == other
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (other.type != type || other.value !is Boolean) {
            return false
        }

        return value == other.value
    }
}
