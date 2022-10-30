package org.solo.kotlin.flexdb.json

import org.solo.kotlin.flexdb.InvalidTypeException
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumTypes

internal object InternalUtil {
    @JvmStatic
    @Throws(InvalidTypeException::class)
    fun toColumn(c: InternalColumn): Column {
        try {
            val type = DbEnumTypes.valueOf(c.type)
            val cons = hashSetOf<DbConstraint>()

            for (i in c.constraints ?: return Column(c.name, type, cons)) {
                cons.add(DbConstraint.valueOf(i))
            }

            return Column(c.name, type, cons)
        } catch (illegal: IllegalArgumentException) {
            throw InvalidTypeException(illegal.message ?: "No message was provided, but an invalid type was found.")
        }
    }

    @JvmStatic
    fun toInternalColumn(c: Column): InternalColumn {
        val cons = hashSetOf<String>()
        for (i in c.constraints) {
            cons.add(i.name)
        }

        return InternalColumn(c.name, c.type.name, cons)
    }
}