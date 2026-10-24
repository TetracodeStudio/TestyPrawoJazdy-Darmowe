package com.testy.parser

expect object PlatformZipUnpacker {
    fun extractZipEntries(zipData: ByteArray): Map<String, ByteArray>
    fun extractZipFileToDirectory(
        zipFilePath: String,
        outputDir: String,
        onProgress: (extractedCount: Int, currentFileName: String) -> Unit
    ): Int
}
