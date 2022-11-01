package org.solo.kotlin.flexdb.zip

data class ZipArchiveItem(val content: ByteArray, val name: String) {
    override operator fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ZipArchiveItem) {
            return false
        }
        if (!content.contentEquals(other.content)) {
            return false
        }
        if (name != other.name) {
            return false
        }

        return true
    }

    override fun toString(): String {
        return name
    }

    override fun hashCode(): Int {
        return 31 * content.contentHashCode() + name.hashCode()
    }
}
