package org.solo.kotlin.flexdb.db.types

class DbString(value: String) : DbValue<String>(value, DbEnumTypes.String)
class DbNumber(value: Long) : DbValue<Long>(value, DbEnumTypes.Number)
class DbDecimal(value: Double) : DbValue<Double>(value, DbEnumTypes.Decimal)
class DbBoolean(value: Boolean) : DbValue<Boolean>(value, DbEnumTypes.Boolean)
