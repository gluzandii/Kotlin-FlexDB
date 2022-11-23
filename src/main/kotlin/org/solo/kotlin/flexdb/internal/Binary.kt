package org.solo.kotlin.flexdb.internal

import java.io.IOException
import java.io.OutputStream

interface Binary {
    @Throws(IOException::class)
    fun writeBinary(out: OutputStream)
}