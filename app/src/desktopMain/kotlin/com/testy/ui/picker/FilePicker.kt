package com.testy.ui.picker

import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

actual object FilePicker {
    actual fun pickSingleFile(
        allowedExtensions: List<String>,
        onFilePicked: (PickedFile?) -> Unit
    ) {
        SwingUtilities.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "Wybierz plik pytań (.xlsx / .zip)"
                isMultiSelectionEnabled = false
                if (allowedExtensions.isNotEmpty()) {
                    fileFilter = FileNameExtensionFilter(
                        "Pliki ${allowedExtensions.joinToString(", ")}",
                        *allowedExtensions.toTypedArray()
                    )
                }
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION && chooser.selectedFile != null) {
                val file = chooser.selectedFile
                onFilePicked(PickedFile(name = file.name, path = file.absolutePath))
            } else {
                onFilePicked(null)
            }
        }
    }

    actual fun pickMultipleFiles(
        allowedExtensions: List<String>,
        onFilesPicked: (List<PickedFile>) -> Unit
    ) {
        SwingUtilities.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "Wybierz pliki multimediów (.zip) - Zaznacz części"
                isMultiSelectionEnabled = true
                if (allowedExtensions.isNotEmpty()) {
                    fileFilter = FileNameExtensionFilter(
                        "Archiwa ZIP (${allowedExtensions.joinToString(", ")})",
                        *allowedExtensions.toTypedArray()
                    )
                }
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION && chooser.selectedFiles != null) {
                val picked = chooser.selectedFiles.map { file ->
                    PickedFile(name = file.name, path = file.absolutePath)
                }
                onFilesPicked(picked)
            } else {
                onFilesPicked(emptyList())
            }
        }
    }

    actual fun pickDirectory(
        onDirectoryPicked: (String?) -> Unit
    ) {
        SwingUtilities.invokeLater {
            val chooser = JFileChooser().apply {
                dialogTitle = "Wybierz folder docelowy zapisu danych i multimediów"
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                isAcceptAllFileFilterUsed = false
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION && chooser.selectedFile != null) {
                onDirectoryPicked(chooser.selectedFile.absolutePath)
            } else {
                onDirectoryPicked(null)
            }
        }
    }
}
