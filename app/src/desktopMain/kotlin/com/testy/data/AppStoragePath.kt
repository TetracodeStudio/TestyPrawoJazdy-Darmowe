package com.testy.data

import com.testy.parser.normalizeMediaKey
import java.io.File

actual fun getAppMediaDirectoryPath(): String {
    val userHome = System.getProperty("user.home") ?: "."
    val dir = File(userHome, ".testy_app")
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath
}

actual fun readBytesFromPath(filePath: String): ByteArray? {
    val file = File(filePath)
    return if (file.exists() && file.isFile) file.readBytes() else null
}

actual fun fileExists(filePath: String): Boolean {
    return File(filePath).exists()
}

actual fun readTextFromPath(filePath: String): String? {
    val file = File(filePath)
    return if (file.exists() && file.isFile) file.readText(Charsets.UTF_8) else null
}

actual fun saveTextToPath(filePath: String, text: String): Boolean {
    return try {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun saveBytesToPath(filePath: String, bytes: ByteArray): Boolean {
    return try {
        val file = File(filePath)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

actual fun countFilesInDirectory(dirPath: String): Int {
    return try {
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.size ?: 0
        } else 0
    } catch (e: Exception) {
        0
    }
}

actual fun findFileInDirectory(dirPath: String, fileName: String): String? {
    return try {
        val dir = File(dirPath)
        if (dir.exists() && dir.isDirectory) {
            val normTarget = normalizeMediaKey(fileName)
            val files = dir.listFiles() ?: return null
            for (f in files) {
                if (f.isFile) {
                    if (f.name == fileName) return f.absolutePath
                    val fNorm = normalizeMediaKey(f.name)
                    if (fNorm == normTarget) return f.absolutePath
                }
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}

