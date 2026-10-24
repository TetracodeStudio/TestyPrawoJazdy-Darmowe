package com.testy.data

actual fun getAppMediaDirectoryPath(): String = "."
actual fun readBytesFromPath(filePath: String): ByteArray? = null
actual fun fileExists(filePath: String): Boolean = false
actual fun readTextFromPath(filePath: String): String? = null
actual fun saveTextToPath(filePath: String, text: String): Boolean = false
actual fun saveBytesToPath(filePath: String, bytes: ByteArray): Boolean = false
actual fun countFilesInDirectory(dirPath: String): Int = 0
actual fun findFileInDirectory(dirPath: String, fileName: String): String? = null

