package com.testy.data

expect fun getAppMediaDirectoryPath(): String
expect fun readBytesFromPath(filePath: String): ByteArray?
expect fun fileExists(filePath: String): Boolean
expect fun readTextFromPath(filePath: String): String?
expect fun saveTextToPath(filePath: String, text: String): Boolean
expect fun saveBytesToPath(filePath: String, bytes: ByteArray): Boolean
expect fun countFilesInDirectory(dirPath: String): Int
expect fun findFileInDirectory(dirPath: String, fileName: String): String?
