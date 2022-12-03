package org.solo.kotlin.flexdb

/**
 * This function loops forever, or until an exception is thrown,
 * or the [block] returns.
 *
 * It calls the [block] function every time it loops.
 */
@Throws(Throwable::class)
inline fun loop(crossinline block: () -> Unit?) {
    while (true) {
        block() ?: return
    }
}
