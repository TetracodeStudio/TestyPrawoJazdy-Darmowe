package com.testy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    onBack: () -> Unit
) {
    val questions by DataManager.questions.collectAsState()
    val mediaMap by DataManager.importedMediaMap.collectAsState()
    val isImporting by DataManager.isImporting.collectAsState()

    var questionFile by remember { mutableStateOf<PickedFile?>(null) }
    var mediaParts by remember { mutableStateOf<List<PickedFile>>(emptyList()) }
    var statusLogs by remember { mutableStateOf<List<String>>(emptyList()) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.import_screen_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("◀ " + stringResource(Res.string.back))
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
            // Summary Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.db_memory_status),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(Res.string.db_memory_summary, questions.size, mediaMap.size),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Section 1: Question Catalog File Selection
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
                            ?: stringResource(Res.string.none_selected),
                        fontSize = 13.sp,
                        color = if (questionFile != null) MaterialTheme.colorScheme.primary else Color.Gray
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
                        Text("📁 " + stringResource(Res.string.btn_select_question_file))
                    }
                }
            }

            // Section 2: Media Files Selection (Multi-part support!)
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
                        text = stringResource(Res.string.media_description, mediaMap.size),
                        fontSize = 12.sp,
                        color = Color.DarkGray
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
                    } else {
                        Text(stringResource(Res.string.no_media_selected), fontSize = 13.sp, color = Color.Gray)
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
                            Text("🎬 " + stringResource(Res.string.btn_select_media_parts))
                        }

                        if (mediaParts.isNotEmpty()) {
                            TextButton(onClick = { mediaParts = emptyList() }) {
                                Text(stringResource(Res.string.btn_clear_parts), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Section 3: Process & Import Button
            Button(
                onClick = {
                    scope.launch {
                        val newLogs = mutableListOf<String>()
                        newLogs.add("▶ Rozpoczynanie przetwarzania...")

                        withContext(Dispatchers.Default) {
                            questionFile?.let { file ->
                                newLogs.add("Wczytywanie pliku pytań ${file.name}...")
                                val qCount = DataManager.importQuestionCatalogFile(file)
                                newLogs.add("✔ Załadowano $qCount pytań z pliku ${file.name}.")
                            }

                            if (mediaParts.isNotEmpty()) {
                                newLogs.add("Wczytywanie ${mediaParts.size} części multimediów...")
                                val mCount = DataManager.importMediaPartsStream(mediaParts) { status ->
                                    newLogs.add(status)
                                }
                                newLogs.add("✔ Pomyślnie rozpakowano $mCount plików multimedialnych.")
                            }
                        }

                        newLogs.add("✅ Przetwarzanie zakończone pomyślnie!")
                        statusLogs = newLogs
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

            // Logs output
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
