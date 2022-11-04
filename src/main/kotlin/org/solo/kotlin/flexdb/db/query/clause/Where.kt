package org.solo.kotlin.flexdb.db.query.clause

import org.solo.kotlin.flexdb.InvalidTypeException
import org.solo.kotlin.flexdb.db.query.operators.RelationalOperator
import org.solo.kotlin.flexdb.db.types.DbValue

data class Where(val one: DbValue<*>, val two: DbValue<*>, val op: RelationalOperator) {
    init {
        if (!one.typeEquals(two)) {
            throw InvalidTypeException("Db value one and two do not have matching types.")
        }
    }

    @Suppress("unused")
    fun isTrue(): Boolean {
        return op.isTrue(one, two)
    }
}
