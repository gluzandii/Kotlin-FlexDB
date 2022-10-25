package org.solo.kotlin.flexdb.db.types

import org.solo.kotlin.flexdb.db.DbEnumTypes

sealed class DbValue<T>(val value: T, val type: DbEnumTypes)
