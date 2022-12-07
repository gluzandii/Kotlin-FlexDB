@file:Suppress("EqualsOrHashCode")

package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.InvalidEmailException
import org.solo.kotlin.flexdb.InvalidTypeException

private val emailRegex = Regex(
    "[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+(?:[A-Z]{2}|com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum)\\b"
)

private fun emailMatches(email: String): String? {
    return if (emailRegex matches email) {
        email
    } else {
        null
    }
}


/**
 * The base class for any database value in FlexDB
 */
sealed class DbValue<T>(
    val value: T,
) : Comparable<DbValue<*>>, Comparator<DbValue<*>> {
    final override fun hashCode(): Int {
        return value.hashCode()
    }

    final override fun toString(): String {
        return className()
    }

    abstract override fun equals(other: Any?): Boolean

    abstract fun className(): String

    /**
     * Compares 2 values of [DbValue]
     *
     * @param other the other value to compare to
     */
    @Throws(InvalidTypeException::class)
    final override operator fun compareTo(other: DbValue<*>): Int {
        if (this === other) {
            return 0
        }
        if (this::class != other::class) {
            throw InvalidTypeException("The DbValue provided has a type not equal to this.")
        }

        return when (other) {
            is DbString, is DbEmail -> this.value as String compareTo other.value as String
            is DbNumber -> this.value as Long compareTo other.value
            is DbDecimal -> this.value as Double compareTo other.value
            is DbBoolean -> this.value as Boolean compareTo other.value
        }
    }

    /**
     * Compares 2 values of [DbValue]
     *
     * @param o1 the first value to compare
     * @param o2 the second value to compare
     */
    final override fun compare(o1: DbValue<*>, o2: DbValue<*>): Int {
        return o1 compareTo o2
    }

    companion object {
        @JvmStatic
        fun fromClassName(className: String): DbValue<*> {
            return when (className) {
                "String" -> String
                "Email" -> Email
                "Number" -> Number
                "Decimal" -> Decimal
                "Boolean" -> Boolean
                else -> throw InvalidTypeException("Invalid type: $className")
            }
        }

        @JvmStatic
        val String = DbString("")

        @JvmStatic
        val Email = DbEmail("")

        @JvmStatic
        val Number = DbNumber(0)

        @JvmStatic
        val Decimal = DbDecimal(0.0)

        @JvmStatic
        val Boolean = DbBoolean(false)
    }
}

/**
 * [DbValue] which stores a string.
 */
class DbString(value: String) : DbValue<String>(value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is String) {
            return value == other
        }
        if (other !is DbValue<*> || other::class != this::class) {
            return false
        }
        if (other.value !is String) {
            return false
        }

        return value == other.value
    }

    override fun className(): String {
        return "String"
    }
}

/**
 * [DbValue] which stores an email.
 */
class DbEmail(
    value: String,
) : DbValue<String>(
    emailMatches(value) ?: throw InvalidEmailException("The email provided: $value is invalid."),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is String) {
            if (emailMatches(other) == null) {
                return false
            }
            return value == other
        }
        if (other !is DbValue<*> || other::class != this::class) {
            return false
        }
        if (other.value !is String || emailMatches(other.value) == null) {
            return false
        }

        return value == other.value
    }

    override fun className(): String {
        return "Email"
    }
}

/**
 * [DbValue] which stores a whole number.
 */
class DbNumber(value: Long) : DbValue<Long>(value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Byte || other is Short || other is Int || other is Long) {
            return value == other
        }
        if (other !is DbValue<*> || other::class != this::class) {
            return false
        }
        if (other.value !is Byte && other.value !is Short && other.value !is Int && other.value !is Long) {
            return false
        }

        return value == other.value
    }

    override fun className(): String {
        return "Number"
    }
}

/**
 * [DbValue] which stores a decimal.
 */
class DbDecimal(value: Double) : DbValue<Double>(value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Byte || other is Short || other is Int || other is Long || other is Float || other is Double) {
            return value == other
        }
        if (other !is DbValue<*> || other::class != this::class) {
            return false
        }
        if (other.value !is Byte && other.value !is Short && other.value !is Int && other.value !is Long && other.value !is Float && other.value !is Double) {
            return false
        }

        return value == other.value
    }

    override fun className(): String {
        return "Decimal"
    }
}

/**
 * [DbValue] which stores a boolean.
 */
class DbBoolean(value: Boolean) : DbValue<Boolean>(value) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other is Boolean) {
            return value == other
        }
        if (other !is DbValue<*> || other::class != this::class) {
            return false
        }
        if (other.value !is Boolean) {
            return false
        }

        return value == other.value
    }

    override fun className(): String {
        return "Boolean"
    }
}
