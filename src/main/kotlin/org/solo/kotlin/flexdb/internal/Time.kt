package org.solo.kotlin.flexdb.internal

/**
 * Static utils for converted minutes or seconds to other units.
 */
object Time {
    /**
     * Converts the given minutes to seconds.
     *
     * @param minutes the minutes to convert
     */
    @JvmStatic
    fun minutesToMillis(minutes: Int): Long {
        return minutes * 60 * 1000L
    }
}