package com.testy.parser

/**
 * Pure Kotlin Zip file entry data holder.
 */
data class ZipEntry(
    val name: String,
    val data: ByteArray,
    val isDirectory: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZipEntry) return false
        if (name != other.name) return false
        if (!data.contentEquals(other.data)) return false
        if (isDirectory != other.isDirectory) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + isDirectory.hashCode()
        return result
    }
}

object ZipUnpacker {

    /**
     * Extracts files from a ZIP byte array.
     * Normalizes nested subfolder paths (e.g. dane/pliki/media.wmv -> media.wmv).
     */
    fun extractZipEntries(zipData: ByteArray): Map<String, ByteArray> {
        val resultMap = mutableMapOf<String, ByteArray>()
        var offset = 0

        while (offset + 30 <= zipData.size) {
            val sig = readIntLe(zipData, offset)
            if (sig != 0x04034b50) {
                break
            }

            val flag = readShortLe(zipData, offset + 6)
            val compMethod = readShortLe(zipData, offset + 8)
            val compressedSize = readIntLe(zipData, offset + 18)
            val uncompressedSize = readIntLe(zipData, offset + 22)
            val nameLen = readShortLe(zipData, offset + 26)
            val extraLen = readShortLe(zipData, offset + 28)

            val nameOffset = offset + 30
            if (nameOffset + nameLen > zipData.size) break

            val fileName = zipData.decodeToString(nameOffset, nameOffset + nameLen)
            val dataOffset = nameOffset + nameLen + extraLen

            if (dataOffset + compressedSize > zipData.size) break

            if (!fileName.endsWith("/")) {
                val fileData = zipData.copyOfRange(dataOffset, dataOffset + compressedSize)
                resultMap[fileName] = fileData
                val normalizedKey = normalizeMediaKey(fileName)
                if (normalizedKey.isNotBlank()) {
                    resultMap[normalizedKey] = fileData
                }
            }

            offset = dataOffset + compressedSize
        }

        return resultMap
    }

    private fun readShortLe(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or ((data[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun readIntLe(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                ((data[offset + 2].toInt() and 0xFF) shl 16) or
                ((data[offset + 3].toInt() and 0xFF) shl 24)
    }
}
