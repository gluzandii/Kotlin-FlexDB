package org.solo.kotlin.flexdb.db.engine

import org.solo.kotlin.flexdb.db.structure.Table
import java.util.*

class TableMap(val constant: Boolean) {
    private val mp: MutableMap<String, Table> = HashMap()

    operator fun get(key: String): Table? {
        return mp[key]
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
}