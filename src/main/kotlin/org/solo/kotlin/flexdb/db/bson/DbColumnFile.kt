package org.solo.kotlin.flexdb.db.bson

import org.solo.kotlin.flexdb.db.structure.Schema
import org.solo.kotlin.flexdb.db.structure.primitive.Column
import org.solo.kotlin.flexdb.db.structure.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbEnumType
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.IOException
import java.util.*

@Suppress("unused")
data class DbColumnFile(var columns: JsonColumns) {
    constructor() : this(JsonColumns())

    @Throws(IOException::class)
    fun serialize(): ByteArray {
        return JsonUtil.binaryJsonSerialize(this)
    }

    fun toSchema(): Schema {
        val set = hashSetOf<Column>()

        for ((k, v) in columns) {
            val type = DbEnumType.valueOf(v.type)
            val cs = v.constraints

            val cons = EnumSet.noneOf(DbConstraint::class.java)
            for (i in cs) {
                cons.add(DbConstraint.valueOf(i))
            }

            set.add(Column(name = k, type, cons))
        }
        return Schema(set)
    }

    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun deserialize(byte: ByteArray): DbColumnFile {
            return JsonUtil.newBinaryObjectMapper().readValue(byte, DbColumnFile::class.java)
                ?: throw IOException("Could not parse column file.")
        }
    }
}
