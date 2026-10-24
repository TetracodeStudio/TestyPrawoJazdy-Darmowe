package com.testy.ui.picker

data class PickedFile(
    val name: String,
    val path: String? = null,
    val bytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        if (name != other.name) return false
        if (path != other.path) return false
        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (bytes?.contentHashCode() ?: 0)
        return result
    }
}

expect object FilePicker {
    fun pickSingleFile(
        allowedExtensions: List<String>,
        onFilePicked: (PickedFile?) -> Unit
    )

    fun pickMultipleFiles(
        allowedExtensions: List<String>,
        onFilesPicked: (List<PickedFile>) -> Unit
    )

    fun pickDirectory(
        onDirectoryPicked: (String?) -> Unit
    )
}
