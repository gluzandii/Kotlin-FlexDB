package org.solo.kotlin.flexdb.db.bson

import org.solo.kotlin.flexdb.internal.Binary
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.ByteArrayOutputStream
import java.io.IOException

@Suppress("unused")
data class DbColumnFile(var columns: JsonColumns) : Binary {
    constructor() : this(JsonColumns())

    @Throws(IOException::class)
    override fun serialize(): ByteArray {
        val mapper = JsonUtil.newBinaryObjectMapper()
        val bytes = ByteArrayOutputStream()
        mapper.writeValue(bytes, this)

        return bytes.toByteArray()
    }

    companion object {
        @JvmStatic
        fun deserialize(byte: ByteArray): DbColumnFile {
            val mapper = JsonUtil.newBinaryObjectMapper()
            return mapper.readValue(byte, DbColumnFile::class.java)
        }
    }
}
