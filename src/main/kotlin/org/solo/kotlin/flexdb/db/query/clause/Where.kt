package org.solo.kotlin.flexdb.db.query.clause

import org.solo.kotlin.flexdb.db.types.DbValue

data class Where(val one: DbValue<*>, val two: DbValue<*>)
