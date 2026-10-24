package com.testy.parser

actual object PlatformZipUnpacker {
    actual fun extractZipEntries(zipData: ByteArray): Map<String, ByteArray> {
        return ZipUnpacker.extractZipEntries(zipData)
    }

    actual fun extractZipFileToDirectory(
        zipFilePath: String,
        outputDir: String,
        onProgress: (extractedCount: Int, currentFileName: String) -> Unit
    ): Int {
        return 0
    }
}
