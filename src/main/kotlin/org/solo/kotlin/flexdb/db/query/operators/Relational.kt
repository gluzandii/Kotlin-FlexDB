package org.solo.kotlin.flexdb.db.query.operators

import org.solo.kotlin.flexdb.db.types.DbValue

interface RelationalOperator {
    fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean
}

class EqualsRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one == two
    }
}

class NotEqualsRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one != two
    }
}

class LessThanRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one < two
    }
}

class LessThanEqualsRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one <= two
    }
}

class GreaterThanRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one > two
    }
}

class GreaterThanEqualsRelational : RelationalOperator {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one >= two
    }
}