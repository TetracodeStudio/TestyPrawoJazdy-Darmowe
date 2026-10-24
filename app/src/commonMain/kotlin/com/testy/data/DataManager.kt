package com.testy.data

import com.testy.model.ExamResult
import com.testy.model.Question
import com.testy.parser.PlatformZipUnpacker
import com.testy.parser.XlsxQuestionParser
import com.testy.parser.normalizeMediaKey
import com.testy.ui.picker.PickedFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DataManager {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val _customDataDir = MutableStateFlow(getAppMediaDirectoryPath())
    val customDataDir: StateFlow<String> = _customDataDir.asStateFlow()

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _examHistory = MutableStateFlow<List<ExamResult>>(emptyList())
    val examHistory: StateFlow<List<ExamResult>> = _examHistory.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importedMediaMap = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    val importedMediaMap: StateFlow<Map<String, ByteArray>> = _importedMediaMap.asStateFlow()

    private val _extractedMediaCount = MutableStateFlow(0)
    val extractedMediaCount: StateFlow<Int> = _extractedMediaCount.asStateFlow()

    val availableCategories = listOf(
        "A", "A1", "A2", "AM", "B", "B1", "C", "C1", "D", "D1", "T"
    )

    init {
        initData()
    }

    private fun initData() {
        val configPath = "${getAppMediaDirectoryPath()}/config.json"
        val configJson = readTextFromPath(configPath)
        if (!configJson.isNullOrBlank()) {
            try {
                val map = json.decodeFromString<Map<String, String>>(configJson)
                val savedDir = map["customDir"]
                if (!savedDir.isNullOrBlank()) {
                    _customDataDir.value = savedDir
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadQuestionsFromDisk()
        updateExtractedMediaCount()
    }

    fun updateExtractedMediaCount() {
        try {
            val mediaDir = "${_customDataDir.value}/media"
            var count = countFilesInDirectory(mediaDir)
            if (count == 0 && _customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultMediaDir = "${getAppMediaDirectoryPath()}/media"
                count = countFilesInDirectory(defaultMediaDir)
            }
            _extractedMediaCount.value = count
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCustomDataDirectory(newPath: String) {
        if (newPath.isBlank()) return
        _customDataDir.value = newPath
        try {
            val configPath = "${getAppMediaDirectoryPath()}/config.json"
            val configData = json.encodeToString(mapOf("customDir" to newPath))
            saveTextToPath(configPath, configData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        loadQuestionsFromDisk()
        updateExtractedMediaCount()
    }

    fun saveQuestionsToDisk() {
        val list = _questions.value
        if (list.isEmpty()) return
        try {
            val serialized = json.encodeToString(list)
            val targetPath = "${_customDataDir.value}/questions.json"
            saveTextToPath(targetPath, serialized)

            if (_customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/questions.json"
                saveTextToPath(defaultPath, serialized)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadQuestionsFromDisk() {
        try {
            val targetPath = "${_customDataDir.value}/questions.json"
            var text = readTextFromPath(targetPath)

            if (text.isNullOrBlank() && _customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/questions.json"
                text = readTextFromPath(defaultPath)
                if (!text.isNullOrBlank()) {
                    saveTextToPath(targetPath, text)
                }
            }

            if (!text.isNullOrBlank()) {
                val list = json.decodeFromString<List<Question>>(text)
                _questions.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveEntriesToDisk(entries: Map<String, ByteArray>, mediaDirStr: String) {
        try {
            for ((key, bytes) in entries) {
                val normKey = normalizeMediaKey(key)
                if (normKey.isNotBlank() && !normKey.endsWith(".xml") && !normKey.endsWith(".json") && !normKey.endsWith(".xlsx")) {
                    val targetFile = "$mediaDirStr/$normKey"
                    saveBytesToPath(targetFile, bytes)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importQuestionCatalogFile(file: PickedFile): Int {
        _isImporting.value = true
        try {
            val bytes = if (file.bytes != null) {
                file.bytes
            } else if (file.path != null) {
                readBytesFromPath(file.path)
            } else null

            if (bytes == null) return 0

            val entries = PlatformZipUnpacker.extractZipEntries(bytes)
            _importedMediaMap.value = _importedMediaMap.value + entries
            val mediaDir = "${_customDataDir.value}/media"
            saveEntriesToDisk(entries, mediaDir)
            updateExtractedMediaCount()

            val sharedStringsData = entries.entries.find { it.key.endsWith("sharedStrings.xml") }?.value
            val sheet1Data = entries.entries.find { it.key.endsWith("sheet1.xml") }?.value

            if (sharedStringsData != null && sheet1Data != null) {
                val ssXml = sharedStringsData.decodeToString()
                val sheetXml = sheet1Data.decodeToString()
                val parsed = XlsxQuestionParser.parseXlsxFromXmlStrings(ssXml, sheetXml)
                _questions.value = parsed
                saveQuestionsToDisk()
                return parsed.size
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isImporting.value = false
        }
        return 0
    }

    fun importMediaPartsStream(
        files: List<PickedFile>,
        onProgress: (statusText: String) -> Unit
    ): Int {
        _isImporting.value = true
        var totalExtracted = 0
        val mediaDir = "${_customDataDir.value}/media"

        try {
            files.forEachIndexed { index, file ->
                val filePath = file.path
                onProgress("Przetwarzanie części ${index + 1}/${files.size}: ${file.name}...")

                if (filePath != null) {
                    val count = PlatformZipUnpacker.extractZipFileToDirectory(
                        zipFilePath = filePath,
                        outputDir = mediaDir,
                        onProgress = { currentCount, currentFile ->
                            onProgress("Część ${index + 1}: Rozpakowano $currentCount plików (Ostatni: $currentFile)")
                        }
                    )
                    totalExtracted += count
                } else if (file.bytes != null) {
                    val entries = PlatformZipUnpacker.extractZipEntries(file.bytes)
                    _importedMediaMap.value = _importedMediaMap.value + entries
                    saveEntriesToDisk(entries, mediaDir)
                    totalExtracted += entries.size
                }
            }
            updateExtractedMediaCount()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isImporting.value = false
        }
        return totalExtracted
    }

    fun getMediaFilePath(rawFileName: String?): String? {
        if (rawFileName.isNullOrBlank()) return null
        val normalized = normalizeMediaKey(rawFileName)
        val mediaDir = "${_customDataDir.value}/media"

        val filePath = "$mediaDir/$normalized"
        if (fileExists(filePath)) {
            return filePath
        }

        val rawPath = "$mediaDir/$rawFileName"
        if (fileExists(rawPath)) {
            return rawPath
        }

        val match = findFileInDirectory(mediaDir, rawFileName)
        if (match != null) {
            return match
        }

        if (_customDataDir.value != getAppMediaDirectoryPath()) {
            val defaultMediaDir = "${getAppMediaDirectoryPath()}/media"
            val defaultFilePath = "$defaultMediaDir/$normalized"
            if (fileExists(defaultFilePath)) {
                return defaultFilePath
            }
            val defaultRawPath = "$defaultMediaDir/$rawFileName"
            if (fileExists(defaultRawPath)) {
                return defaultRawPath
            }
            val defaultMatch = findFileInDirectory(defaultMediaDir, rawFileName)
            if (defaultMatch != null) {
                return defaultMatch
            }
        }

        return null
    }

    fun getMediaBytes(rawFileName: String?): ByteArray? {
        if (rawFileName.isNullOrBlank()) return null

        val inMemMap = _importedMediaMap.value
        inMemMap[rawFileName]?.let { return it }
        val normalized = normalizeMediaKey(rawFileName)
        inMemMap[normalized]?.let { return it }

        val filePath = getMediaFilePath(rawFileName)
        if (filePath != null) {
            val bytes = readBytesFromPath(filePath)
            if (bytes != null) return bytes
        }

        return null
    }

    fun addExamResult(result: ExamResult) {
        _examHistory.value = listOf(result) + _examHistory.value
    }

    fun getQuestionsForCategory(category: String): List<Question> {
        val all = questions.value
        val filtered = all.filter { it.matchesCategory(category) }
        return if (filtered.isNotEmpty()) filtered else all
    }
}
