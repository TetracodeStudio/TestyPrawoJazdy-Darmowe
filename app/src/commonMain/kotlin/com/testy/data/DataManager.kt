package com.testy.data

import com.testy.model.ExamResult
import com.testy.model.Question
import com.testy.parser.PlatformZipUnpacker
import com.testy.parser.XlsxQuestionParser
import com.testy.parser.normalizeMediaKey
import com.testy.ui.AppThemeMode
import com.testy.ui.AppThemePalette
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

    private val _passedQuestionIds = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val passedQuestionIds: StateFlow<Map<String, Set<String>>> = _passedQuestionIds.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _themePalette = MutableStateFlow(AppThemePalette.BLUE)
    val themePalette: StateFlow<AppThemePalette> = _themePalette.asStateFlow()

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
                val savedMode = map["themeMode"]
                if (!savedMode.isNullOrBlank()) {
                    _themeMode.value = AppThemeMode.fromId(savedMode)
                }
                val savedPalette = map["themePalette"]
                if (!savedPalette.isNullOrBlank()) {
                    _themePalette.value = AppThemePalette.fromId(savedPalette)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadQuestionsFromDisk()
        loadExamHistoryFromDisk()
        loadLearningProgressFromDisk()
        updateExtractedMediaCount()
    }

    fun saveConfigToDisk() {
        try {
            val configPath = "${getAppMediaDirectoryPath()}/config.json"
            val map = mapOf(
                "customDir" to _customDataDir.value,
                "themeMode" to _themeMode.value.id,
                "themePalette" to _themePalette.value.id
            )
            val configData = json.encodeToString(map)
            saveTextToPath(configPath, configData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        saveConfigToDisk()
    }

    fun setThemePalette(palette: AppThemePalette) {
        _themePalette.value = palette
        saveConfigToDisk()
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
        saveConfigToDisk()
        loadQuestionsFromDisk()
        loadExamHistoryFromDisk()
        loadLearningProgressFromDisk()
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

    fun saveExamHistoryToDisk() {
        val list = _examHistory.value
        try {
            val serialized = json.encodeToString(list)
            val targetPath = "${_customDataDir.value}/exam_history.json"
            saveTextToPath(targetPath, serialized)

            if (_customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/exam_history.json"
                saveTextToPath(defaultPath, serialized)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadExamHistoryFromDisk() {
        try {
            val targetPath = "${_customDataDir.value}/exam_history.json"
            var text = readTextFromPath(targetPath)

            if (text.isNullOrBlank() && _customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/exam_history.json"
                text = readTextFromPath(defaultPath)
                if (!text.isNullOrBlank()) {
                    saveTextToPath(targetPath, text)
                }
            }

            if (!text.isNullOrBlank()) {
                val list = json.decodeFromString<List<ExamResult>>(text)
                _examHistory.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addExamResult(result: ExamResult) {
        _examHistory.value = listOf(result) + _examHistory.value
        saveExamHistoryToDisk()
    }

    fun clearExamHistory() {
        _examHistory.value = emptyList()
        saveExamHistoryToDisk()
    }

    fun isQuestionPassed(category: String, questionId: String): Boolean {
        return _passedQuestionIds.value[category]?.contains(questionId) == true
    }

    fun markQuestionPassed(category: String, questionId: String) {
        val current = _passedQuestionIds.value.toMutableMap()
        val set = (current[category] ?: emptySet()).toMutableSet()
        if (set.add(questionId)) {
            current[category] = set
            _passedQuestionIds.value = current
            saveLearningProgressToDisk()
        }
    }

    fun getPassedCountForCategory(category: String): Int {
        val passedSet = _passedQuestionIds.value[category] ?: return 0
        val categoryQuestions = getQuestionsForCategory(category).map { it.id }.toSet()
        return passedSet.count { it in categoryQuestions }
    }

    fun resetLearningProgressForCategory(category: String) {
        val current = _passedQuestionIds.value.toMutableMap()
        current.remove(category)
        _passedQuestionIds.value = current
        saveLearningProgressToDisk()
    }

    fun saveLearningProgressToDisk() {
        val data = _passedQuestionIds.value
        try {
            val serialized = json.encodeToString(data)
            val targetPath = "${_customDataDir.value}/learning_progress.json"
            saveTextToPath(targetPath, serialized)

            if (_customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/learning_progress.json"
                saveTextToPath(defaultPath, serialized)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadLearningProgressFromDisk() {
        try {
            val targetPath = "${_customDataDir.value}/learning_progress.json"
            var text = readTextFromPath(targetPath)

            if (text.isNullOrBlank() && _customDataDir.value != getAppMediaDirectoryPath()) {
                val defaultPath = "${getAppMediaDirectoryPath()}/learning_progress.json"
                text = readTextFromPath(defaultPath)
                if (!text.isNullOrBlank()) {
                    saveTextToPath(targetPath, text)
                }
            }

            if (!text.isNullOrBlank()) {
                val map = json.decodeFromString<Map<String, Set<String>>>(text)
                _passedQuestionIds.value = map
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getQuestionsForCategory(category: String): List<Question> {
        val all = questions.value
        val filtered = all.filter { it.matchesCategory(category) }
        return if (filtered.isNotEmpty()) filtered else all
    }
}
