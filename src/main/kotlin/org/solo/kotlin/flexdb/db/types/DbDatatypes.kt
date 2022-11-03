package org.solo.kotlin.flexdb.db.types

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

sealed class DbValue<T>(val value: T, val type: DbEnumTypes) {
    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is DbValue<*>) {
            return false
        }
        if (type != other.type) {
            return false
        }

        return value == other.value
    }
}

class DbString(value: String) : DbValue<String>(value, DbEnumTypes.String)
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumTypes.Number)
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumTypes.Decimal)
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumTypes.Boolean)
