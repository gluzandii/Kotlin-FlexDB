package org.solo.kotlin.flexdb.db.types

enum class DbEnumTypes {
    DbString,
    DbNumber,
    DbDecimal,
    DbBoolean,
}

sealed class DbValue<T>(val value: T, val type: DbEnumTypes) {
    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DbValue<*>

        if (value != other.value) return false
        if (type != other.type) return false

        return true
    }
}

class DbString(value: String) : DbValue<String>(value, DbEnumTypes.DbString)
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumTypes.DbNumber)
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumTypes.DbDecimal)
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumTypes.DbBoolean)
