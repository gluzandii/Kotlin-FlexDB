package org.solo.kotlin.flexdb.zip

data class ZipArchiveItem(val content: ByteArray, val name: String) {
    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ZipArchiveItem

        if (!content.contentEquals(other.content)) return false
        if (name != other.name) return false

        return true
    }

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
