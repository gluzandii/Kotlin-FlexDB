package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.InvalidTypeException

enum class DbEnumTypes {
    String,
    Number,
    Decimal,
    Boolean;

    val default: DbValue<*>
        get() {
            return when (this) {
                String -> DbString("")
                Number -> DbNumber(0)
                Decimal -> DbDecimal(0.0)
                Boolean -> DbBoolean(false)
            }
        }
}

// CREATE JSON SERIALIZATION AND DESERIALIZATION LOGIC

sealed class DbValue<T>(val value: T, val type: DbEnumTypes) {
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

    @Throws(InvalidTypeException::class)
    operator fun compareTo(v: DbValue<*>): Int {
        if (this === v) {
            return 0
        }
        if (v.type != type) {
            throw InvalidTypeException("The DbValue provided has a type not equal to this.")
        }

        return when (type) {
            DbEnumTypes.String -> v.value.toString().compareTo(value.toString())
            DbEnumTypes.Decimal -> (v.value as Double).compareTo(value as Double)
            DbEnumTypes.Number -> (v.value as Long).compareTo(value as Long)
            DbEnumTypes.Boolean -> (v.value as Boolean).compareTo(value as Boolean)
        }
    }
}

class DbString(value: String) : DbValue<String>(value, DbEnumTypes.String)
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumTypes.Number)
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumTypes.Decimal)
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumTypes.Boolean)
