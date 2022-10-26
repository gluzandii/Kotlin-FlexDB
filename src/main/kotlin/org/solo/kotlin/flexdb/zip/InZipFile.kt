package org.solo.kotlin.flexdb.zip

class InZipFile(val content: ByteArray, val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InZipFile

        if (!content.contentEquals(other.content)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}
