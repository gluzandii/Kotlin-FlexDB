package org.solo.kotlin.flexdb.db.types

class Column(val name: String, val type: DbEnumTypes?) {
    override fun hashCode() = name.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Column

        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun toString() = "Column: {'name': $name, 'type': ${type?.name}}"

    companion object {
        @JvmStatic
        fun nameOnly(name: String) = Column(name, null)
    }
}

class Row(val rowNum: Long, schema: Array<Column>) {
    private val content: MutableMap<Column, DbValue<*>?> = hashMapOf()

    init {
        schema.forEach { content[it] = null }
    }

    @Throws(Throwable::class)
    operator fun set(colName: String, value: DbValue<*>?) = set(Column.nameOnly(colName), value)

    @Throws(Throwable::class)
    operator fun set(colName: Column, value: DbValue<*>?) {
        if (!content.containsKey(colName)) {
            throw IllegalArgumentException("The key: ${colName.name} does not exist in the row.")
        }
        content[colName] = value
    }

    operator fun get(colName: String) = get(Column.nameOnly(colName))
    operator fun get(colName: Column) = content.getOrDefault(colName, null)
}
