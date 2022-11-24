package org.solo.kotlin.flexdb.internal

import java.io.IOException

interface Binary {
    @Throws(IOException::class)
    fun serialize(): ByteArray
}