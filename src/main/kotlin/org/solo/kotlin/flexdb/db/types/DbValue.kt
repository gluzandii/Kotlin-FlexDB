package org.solo.kotlin.flexdb.db.types

sealed class DbValue<T>(val value: T, val type: DbEnumTypes)
