package com.testy.ui.picker

actual object FilePicker {
    actual fun pickSingleFile(
        allowedExtensions: List<String>,
        onFilePicked: (PickedFile?) -> Unit
    ) {
        onFilePicked(null)
    }

    actual fun pickMultipleFiles(
        allowedExtensions: List<String>,
        onFilesPicked: (List<PickedFile>) -> Unit
    ) {
        onFilesPicked(emptyList())
    }

    actual fun pickDirectory(
        onDirectoryPicked: (String?) -> Unit
    ) {
        onDirectoryPicked(null)
    }
}
