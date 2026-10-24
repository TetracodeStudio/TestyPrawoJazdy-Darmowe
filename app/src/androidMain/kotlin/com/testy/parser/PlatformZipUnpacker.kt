package com.testy.parser

import android.net.Uri
import com.testy.data.AndroidContextProvider
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

actual object PlatformZipUnpacker {
    actual fun extractZipEntries(zipData: ByteArray): Map<String, ByteArray> {
        val map = mutableMapOf<String, ByteArray>()
        try {
            ByteArrayInputStream(zipData).use { bais ->
                ZipInputStream(bais).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val name = entry.name
                            val bytes = zis.readBytes()
                            map[name] = bytes
                            val normalizedKey = normalizeMediaKey(name)
                            if (normalizedKey.isNotBlank()) {
                                map[normalizedKey] = bytes
                            }
                        }
                        entry = zis.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    actual fun extractZipFileToDirectory(
        zipFilePath: String,
        outputDir: String,
        onProgress: (extractedCount: Int, currentFileName: String) -> Unit
    ): Int {
        var count = 0
        val targetDir = File(outputDir)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        val buffer = ByteArray(64 * 1024)

        try {
            val inputStream = try {
                if (zipFilePath.startsWith("content://")) {
                    val ctx = AndroidContextProvider.getContext()
                    ctx?.contentResolver?.openInputStream(Uri.parse(zipFilePath))
                } else {
                    val file = File(zipFilePath)
                    if (file.exists() && file.canRead()) {
                        FileInputStream(file)
                    } else {
                        val ctx = AndroidContextProvider.getContext()
                        ctx?.contentResolver?.openInputStream(Uri.parse(zipFilePath))
                    }
                }
            } catch (e: Exception) {
                val ctx = AndroidContextProvider.getContext()
                ctx?.contentResolver?.openInputStream(Uri.parse(zipFilePath))
            }

            if (inputStream != null) {
                inputStream.use { stream ->
                    ZipInputStream(stream).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null) {
                            if (!entry.isDirectory) {
                                val name = entry.name
                                val normKey = normalizeMediaKey(name)
                                if (normKey.isNotBlank()) {
                                    val outFile = File(targetDir, normKey)
                                    FileOutputStream(outFile).use { fos ->
                                        var read: Int
                                        while (zis.read(buffer).also { read = it } != -1) {
                                            fos.write(buffer, 0, read)
                                        }
                                    }
                                    count++
                                    if (count % 20 == 0) {
                                        onProgress(count, normKey)
                                    }
                                }
                            }
                            entry = zis.nextEntry
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return count
    }
}
