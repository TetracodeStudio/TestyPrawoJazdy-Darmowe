package com.testy.ui.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

actual object FilePicker {

    private var singleFileLauncher: ActivityResultLauncher<Array<String>>? = null
    private var multipleFilesLauncher: ActivityResultLauncher<Array<String>>? = null
    private var directoryLauncher: ActivityResultLauncher<Uri?>? = null

    private var onSingleFilePicked: ((PickedFile?) -> Unit)? = null
    private var onMultipleFilesPicked: ((List<PickedFile>) -> Unit)? = null
    private var onDirectoryPicked: ((String?) -> Unit)? = null

    fun register(activity: ComponentActivity) {
        singleFileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            val callback = onSingleFilePicked
            onSingleFilePicked = null
            if (uri != null) {
                try {
                    activity.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val file = processUri(activity, uri)
                callback?.invoke(file)
            } else {
                callback?.invoke(null)
            }
        }

        multipleFilesLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris: List<Uri> ->
            val callback = onMultipleFilesPicked
            onMultipleFilesPicked = null
            if (uris.isNotEmpty()) {
                val files = uris.mapNotNull { uri ->
                    try {
                        activity.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    processUri(activity, uri)
                }
                callback?.invoke(files)
            } else {
                callback?.invoke(emptyList())
            }
        }

        directoryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            val callback = onDirectoryPicked
            onDirectoryPicked = null
            if (uri != null) {
                try {
                    activity.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val dirPath = resolveDirectoryPath(activity, uri)
                callback?.invoke(dirPath)
            } else {
                callback?.invoke(null)
            }
        }
    }

    actual fun pickSingleFile(
        allowedExtensions: List<String>,
        onFilePicked: (PickedFile?) -> Unit
    ) {
        onSingleFilePicked = onFilePicked
        val mimeTypes = arrayOf("*/*")
        val launcher = singleFileLauncher
        if (launcher != null) {
            try {
                launcher.launch(mimeTypes)
            } catch (e: Exception) {
                e.printStackTrace()
                onFilePicked(null)
            }
        } else {
            onFilePicked(null)
        }
    }

    actual fun pickMultipleFiles(
        allowedExtensions: List<String>,
        onFilesPicked: (List<PickedFile>) -> Unit
    ) {
        onMultipleFilesPicked = onFilesPicked
        val mimeTypes = arrayOf("*/*")
        val launcher = multipleFilesLauncher
        if (launcher != null) {
            try {
                launcher.launch(mimeTypes)
            } catch (e: Exception) {
                e.printStackTrace()
                onFilesPicked(emptyList())
            }
        } else {
            onFilesPicked(emptyList())
        }
    }

    actual fun pickDirectory(
        onDirectoryPicked: (String?) -> Unit
    ) {
        this.onDirectoryPicked = onDirectoryPicked
        val launcher = directoryLauncher
        if (launcher != null) {
            try {
                launcher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                onDirectoryPicked(null)
            }
        } else {
            onDirectoryPicked(null)
        }
    }

    private fun processUri(context: Context, uri: Uri): PickedFile? {
        return try {
            var fileName = "file_${System.currentTimeMillis()}"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) {
                        fileName = name
                    }
                }
            }

            val realPath = resolveRealPath(context, uri)
            val finalPath = realPath ?: uri.toString()

            PickedFile(
                name = fileName,
                path = finalPath,
                bytes = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resolveRealPath(context: Context, uri: Uri): String? {
        try {
            if ("file".equals(uri.scheme, ignoreCase = true)) {
                val file = File(uri.path ?: "")
                if (file.exists() && file.canRead()) return file.absolutePath
            }
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                if (docId.startsWith("raw:")) {
                    val rawPath = docId.substring(4)
                    val file = File(rawPath)
                    if (file.exists() && file.canRead()) return file.absolutePath
                }
                val split = docId.split(":")
                if (split.size >= 2) {
                    val type = split[0]
                    val relativePath = split[1]
                    val targetPath = if (type.equals("primary", ignoreCase = true)) {
                        "/storage/emulated/0/$relativePath"
                    } else {
                        "/storage/$type/$relativePath"
                    }
                    val file = File(targetPath)
                    if (file.exists() && file.canRead()) return file.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun resolveDirectoryPath(context: Context, uri: Uri): String {
        try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val split = docId.split(":")
            if (split.size >= 2) {
                val type = split[0]
                val relativePath = split[1]
                val rawTarget = if (type.equals("primary", ignoreCase = true)) {
                    File("/storage/emulated/0/$relativePath")
                } else {
                    File("/storage/$type/$relativePath")
                }

                if (isDirectoryWritable(rawTarget)) {
                    return rawTarget.absolutePath
                }

                val appBaseDir = context.getExternalFilesDir(null) ?: context.filesDir
                val safeTarget = File(appBaseDir, relativePath.ifBlank { "app_data" })
                safeTarget.mkdirs()
                if (safeTarget.exists()) {
                    return safeTarget.absolutePath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val fallback = File(context.getExternalFilesDir(null) ?: context.filesDir, "app_data").apply { mkdirs() }
        return fallback.absolutePath
    }

    private fun isDirectoryWritable(dir: File): Boolean {
        return try {
            if (!dir.exists()) dir.mkdirs()
            if (!dir.exists()) return false
            val testFile = File(dir, ".test_write_${System.currentTimeMillis()}")
            if (testFile.createNewFile()) {
                testFile.delete()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

}
