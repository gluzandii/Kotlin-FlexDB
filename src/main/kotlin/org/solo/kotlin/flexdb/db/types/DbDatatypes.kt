package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.InvalidTypeException

enum class DbEnumType {
    STRING,
    NUMBER,
    DECIMAL,
    BOOLEAN;

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

    @Throws(InvalidTypeException::class)
    operator fun compareTo(v: DbValue<*>): Int {
        if (this === v) {
            return 0
        }
        if (v.type != type) {
            throw InvalidTypeException("The DbValue provided has a type not equal to this.")
        }

        return when (type) {
            DbEnumType.STRING -> v.value.toString().compareTo(value.toString())
            DbEnumType.DECIMAL -> (v.value as Double).compareTo(value as Double)
            DbEnumType.NUMBER -> (v.value as Long).compareTo(value as Long)
            DbEnumType.BOOLEAN -> (v.value as Boolean).compareTo(value as Boolean)
        }
    }
}

class DbString(value: String) : DbValue<String>(value, DbEnumType.STRING)
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumType.NUMBER)
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumType.DECIMAL)
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumType.BOOLEAN)
