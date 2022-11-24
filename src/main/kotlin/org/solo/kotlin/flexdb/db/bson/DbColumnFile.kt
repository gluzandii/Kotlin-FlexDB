package org.solo.kotlin.flexdb.db.bson

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    suspend fun serialize(): ByteArray {
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
        suspend fun deserialize(byte: ByteArray): DbColumnFile {
            var exp: IOException? = null
            val b = withContext(Dispatchers.Default) {
                return@withContext try {
                    val mapper = JsonUtil.newBinaryObjectMapper()
                    val b = mapper.readValue(byte, DbColumnFile::class.java)

                    b
                } catch (io: IOException) {
                    exp = io
                    null
                }
            }
            if (exp != null) {
                throw exp!!
            }

            return b ?: throw IOException("Could not serialize this rowc")
        }
    }
}
