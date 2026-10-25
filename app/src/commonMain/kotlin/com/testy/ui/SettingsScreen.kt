package com.testy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testy.data.DataManager
import com.testy.ui.picker.FilePicker
import com.testy.ui.picker.PickedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*
import androidx.compose.ui.platform.LocalUriHandler
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val questions by DataManager.questions.collectAsState()
    val isImporting by DataManager.isImporting.collectAsState()
    val extractedMediaCount by DataManager.extractedMediaCount.collectAsState()
    val customDir by DataManager.customDataDir.collectAsState()
    val availableCategories = DataManager.availableCategories

    val themeMode by DataManager.themeMode.collectAsState()
    val themePalette by DataManager.themePalette.collectAsState()

    var questionFile by remember { mutableStateOf<PickedFile?>(null) }
    var mediaParts by remember { mutableStateOf<List<PickedFile>>(emptyList()) }
    var statusLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var progressStatus by remember { mutableStateOf("") }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Theme Section
            AppearanceAndThemeCard(
                currentThemeMode = themeMode,
                onThemeModeSelected = { DataManager.setThemeMode(it) },
                currentPalette = themePalette,
                onPaletteSelected = { DataManager.setThemePalette(it) }
            )

            // Target Storage Directory Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.storage_folder_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = stringResource(Res.string.storage_path_format, customDir),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                    OutlinedButton(
                        onClick = {
                            FilePicker.pickDirectory { pickedDir ->
                                if (!pickedDir.isNullOrBlank()) {
                                    DataManager.setCustomDataDirectory(pickedDir)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.btn_change_storage))
                    }
                }
            }

            // Category Selection Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.default_category_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableCategories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelected(cat) },
                                label = { Text(stringResource(Res.string.category_label, cat), fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            // Question Catalog Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.question_catalog_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = questionFile?.let { stringResource(Res.string.file_selected_format, it.name) }
                            ?: stringResource(Res.string.questions_saved_count, questions.size),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            FilePicker.pickSingleFile(listOf("xlsx", "zip")) { picked ->
                                if (picked != null) {
                                    questionFile = picked
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(Res.string.btn_select_question_file))
                    }
                }
            }

            // Media Files Section (Streaming extraction for 8+ GB files)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.media_files_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = stringResource(Res.string.media_description, extractedMediaCount),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (mediaParts.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            mediaParts.forEachIndexed { idx, part ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.part_format, idx + 1, part.name),
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                        TextButton(onClick = {
                                            mediaParts = mediaParts.filterIndexed { i, _ -> i != idx }
                                        }) {
                                            Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                FilePicker.pickMultipleFiles(listOf("zip")) { pickedList ->
                                    if (pickedList.isNotEmpty()) {
                                        mediaParts = mediaParts + pickedList
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.btn_select_media_parts))
                        }

                        if (mediaParts.isNotEmpty()) {
                            TextButton(onClick = { mediaParts = emptyList() }) {
                                Text(stringResource(Res.string.btn_clear_parts), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            val uriHandler = LocalUriHandler.current

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.download_data_question),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Text(
                        text = stringResource(Res.string.download_data_text),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Button(
                        onClick = {
                            uriHandler.openUri("https://www.gov.pl/web/infrastruktura/prawo-jazdy")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.download_data_btn),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // App Updates Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.update_available_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isCheckingUpdate) stringResource(Res.string.update_checking) else "Sprawdź, czy dostępna jest nowa wersja",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isCheckingUpdate = true
                                AppUpdater.checkForUpdates()
                                isCheckingUpdate = false
                            }
                        },
                        enabled = !isCheckingUpdate
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(Res.string.check_for_updates))
                        }
                    }
                }
            }
            // Start Import Process Button
            Button(
                onClick = {
                    scope.launch {
                        val newLogs = mutableListOf<String>()
                        newLogs.add("Rozpoczynanie importu...")

                        withContext(Dispatchers.Default) {
                            // 1. Process Question File
                            questionFile?.let { qFile ->
                                newLogs.add("Wczytywanie i zapisywanie bazy pytań: ${qFile.name}...")
                                val count = DataManager.importQuestionCatalogFile(qFile)
                                newLogs.add("Pomyślnie załadowano i zapisano $count pytań w $customDir/questions.json!")
                            }

                            // 2. Process Media Parts Stream
                            if (mediaParts.isNotEmpty()) {
                                newLogs.add("Rozpoczynanie rozpakowywania ${mediaParts.size} części multimediów na dysk...")
                                val mCount = DataManager.importMediaPartsStream(mediaParts) { statusText ->
                                    progressStatus = statusText
                                }
                                newLogs.add("Pomyślnie rozpakowano $mCount plików multimediów w $customDir/media/")
                            }
                        }

                        newLogs.add("Import i zapis zakończony pomyślnie!")
                        statusLogs = newLogs
                        progressStatus = ""
                    }
                },
                enabled = !isImporting && (questionFile != null || mediaParts.isNotEmpty()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(Res.string.btn_importing))
                } else {
                    Text(stringResource(Res.string.btn_start_import), fontWeight = FontWeight.Bold)
                }
            }

            // Progress status display
            if (progressStatus.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = progressStatus,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Logs Output
            if (statusLogs.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(Res.string.logs_title), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        statusLogs.forEach { log ->
                            Text(text = log, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
