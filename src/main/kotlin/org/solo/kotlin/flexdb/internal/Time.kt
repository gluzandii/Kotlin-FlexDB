package org.solo.kotlin.flexdb.internal

object Time {
    @JvmStatic
    fun minutesToMillis(minutes: Int): Long {
        return minutes * 60 * 1000L
    }
}