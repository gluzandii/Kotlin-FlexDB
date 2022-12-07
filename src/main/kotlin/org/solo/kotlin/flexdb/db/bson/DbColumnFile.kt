package org.solo.kotlin.flexdb.db.bson

import org.solo.kotlin.flexdb.db.structure.schemafull.Schema
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Column
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.internal.JsonCreatePayload
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.JsonUtil.newBinaryObjectMapper
import java.io.IOException
import java.util.*

/**
 * Implementation of a column file in the database.
 *
 * ```
 * {
 *      "columns": {
 *          "name": {
 *              "type": "String",
 *              "constraints": [
 *                  "NotNull"
 *              ]
 *          },
 *          "email": {
 *              "type": "String",
 *              "constraints": [
 *                  "Unique"
 *              ]
 *          }
 *      }
 * }
 */
@Suppress("unused")
data class DbColumnFile(var columns: JsonCreatePayload) {
    constructor() : this(JsonCreatePayload())

    @Throws(IOException::class)
    fun serialize(): ByteArray {
        return JsonUtil.binaryJsonSerialize(this)
    }

    fun toSchema(): Schema {
        val set = hashSetOf<Column>()

        for ((k, v) in columns) {
            val type = DbValue.fromClassName(v.type)

            val cons = EnumSet.noneOf(DbConstraint::class.java)
            for (i in v.constraints) {
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
            return newBinaryObjectMapper().readValue(byte, DbColumnFile::class.java)
                ?: throw IOException("Could not parse column file.")
        }
    }
}
