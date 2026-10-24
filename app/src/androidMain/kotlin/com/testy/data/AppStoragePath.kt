package com.testy.data

import android.content.Context
import android.net.Uri
import com.testy.parser.normalizeMediaKey
import java.io.File

object AndroidContextProvider {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getContext(): Context? = appContext
}

actual fun getAppMediaDirectoryPath(): String {
    val ctx = AndroidContextProvider.getContext()
    if (ctx != null) {
        val ext = ctx.getExternalFilesDir(null)
        val dir = ext ?: ctx.filesDir
        if (!dir.exists()) dir.mkdirs()
        return dir.absolutePath
    }
    val userHome = System.getProperty("user.home") ?: "."
    val dir = File(userHome, ".testy_app")
    if (!dir.exists()) dir.mkdirs()
    return dir.absolutePath
}

actual fun readBytesFromPath(filePath: String): ByteArray? {
    return try {
        if (filePath.startsWith("content://")) {
            val ctx = AndroidContextProvider.getContext()
            val uri = Uri.parse(filePath)
            ctx?.contentResolver?.openInputStream(uri)?.use { it.readBytes() }
        } else {
            val file = File(filePath)
            if (file.exists() && file.isFile && file.canRead()) file.readBytes() else null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

actual fun fileExists(filePath: String): Boolean {
    return try {
        if (filePath.startsWith("content://")) {
            val ctx = AndroidContextProvider.getContext()
            val uri = Uri.parse(filePath)
            ctx?.contentResolver?.openInputStream(uri)?.use { true } ?: false
        } else {
            val file = File(filePath)
            file.exists() && (file.canRead() || file.isDirectory)
        }
    } catch (e: Exception) {
        false
    }
}

actual fun readTextFromPath(filePath: String): String? {
    return try {
        if (filePath.startsWith("content://")) {
            val ctx = AndroidContextProvider.getContext()
            val uri = Uri.parse(filePath)
            ctx?.contentResolver?.openInputStream(uri)?.use {
                it.bufferedReader(Charsets.UTF_8).readText()
            }
        } else {
            val file = File(filePath)
            if (file.exists() && file.isFile && file.canRead()) file.readText(Charsets.UTF_8) else null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
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

