package org.solo.kotlin.flexdb.db.query.operators

import org.solo.kotlin.flexdb.db.types.DbValue

interface RelationalOperators {
    fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean
}

class EqualsRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one == two
    }
}

class NotEqualsRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one != two
    }
}

class LessThanRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one < two
    }
}

class LessThanEqualsRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one <= two
    }
}

class GreaterThanRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one > two
    }
}

class GreaterThanEqualsRelational : RelationalOperators {
    override fun isTrue(one: DbValue<*>, two: DbValue<*>): Boolean {
        return one >= two
    }
}