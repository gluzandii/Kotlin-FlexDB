package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.InvalidTableNameException
import org.solo.kotlin.flexdb.db.structure.Table
import java.util.*

class TableMap(private val size: Int?) {
    private val mp: MutableMap<String, Table> = HashMap()

    var length: Int = 0
        private set

    init {
        if ((size != null) and (size!! < 0)) {
            throw IllegalArgumentException("The size: $size, is negative")
        }
    }

    operator fun get(key: String): Table? {
        return mp[key]
    }

    @Throws(IndexOutOfBoundsException::class)
    operator fun set(key: String, value: Table) {
        if (!mp.containsKey(key)) {
            incrementCount()
        }

        mp[key] = value
    }

    fun remove(key: String) {
        if (!mp.containsKey(key)) {
            throw InvalidTableNameException("The name: $key is not present in this TableMap")
        }

        mp.remove(key)
        decrementCount()
    }

    fun clear() {
        mp.clear()
    }

    fun getAllOfThisTable(table: String): Set<Table> {
        val regex = Regex("$table\\d+")
        val ll = TreeSet<Table>()

        for (i in mp.keys) {
            if (regex.matches(i)) {
                ll.add(mp[i]!!)
            }
        }

        return ll
    }

    @Throws(IndexOutOfBoundsException::class)
    private inline fun boundCheck() {
        if (size == null) {
            return
        }

        val c = length + 1
        if (c > size) {
            throw IndexOutOfBoundsException("This key cannot be added, since the maximum size has been reached.")
        }
    }

    private inline fun decrementCount() {
        length = (length - 1).coerceAtLeast(0)
    }

    @Throws(IndexOutOfBoundsException::class)
    private inline fun incrementCount() {
        boundCheck()
        length++
    }
}