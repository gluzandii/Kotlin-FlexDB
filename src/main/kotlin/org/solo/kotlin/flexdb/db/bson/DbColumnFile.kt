package org.solo.kotlin.flexdb.db.bson

import org.solo.kotlin.flexdb.internal.Binary
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import java.io.OutputStream

@Suppress("unused")
data class DbColumnFile(var columns: JsonColumns) : Binary {
    constructor() : this(JsonColumns())

    override fun writeBinary(out: OutputStream) {
        val mapper = JsonUtil.newBinaryObjectMapper()
        mapper.writeValue(out, this)
    }

    companion object {
        @JvmStatic
        fun deserialize(byte: ByteArray): DbColumnFile {
            val mapper = JsonUtil.newBinaryObjectMapper()
            return mapper.readValue(byte, DbColumnFile::class.java)
        }
    }
}
