package com.testy.parser

/**
 * Normalizes media paths and file names.
 * Strips directory folders, backslashes, leading dot-slashes, whitespace, and lowercases.
 * Example: "dane\pliki\.\AK_D05_06_org.WMV " -> "ak_d05_06_org.wmv"
 */
fun normalizeMediaKey(path: String): String {
    if (path.isBlank()) return ""
    var clean = path.trim().replace('\\', '/')
    while (clean.startsWith("./")) {
        clean = clean.removePrefix("./")
    }
    val baseName = clean.substringAfterLast('/')
    return baseName.trim().lowercase()
}
