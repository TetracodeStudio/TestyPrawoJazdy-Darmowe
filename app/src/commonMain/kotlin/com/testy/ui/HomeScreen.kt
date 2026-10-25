package com.testy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testy.data.DataManager
import com.testy.model.ExamResult
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

import androidx.compose.material.icons.filled.School

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onStartExam: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val questions by DataManager.questions.collectAsState()
    val history by DataManager.examHistory.collectAsState()
    val passedMap by DataManager.passedQuestionIds.collectAsState()
    val availableCategories = DataManager.availableCategories

    LaunchedEffect(Unit) {
        AppUpdater.checkForUpdates()
    }

    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val categoryQuestionsCount = remember(questions, selectedCategory) {
        questions.count { it.matchesCategory(selectedCategory) }
    }

    val categoryPassedCount = remember(passedMap, categoryQuestionsCount, selectedCategory) {
        DataManager.getPassedCountForCategory(selectedCategory)
    }

    val learningPercent = if (categoryQuestionsCount > 0) {
        ((categoryPassedCount.toFloat() / categoryQuestionsCount) * 100).toInt()
    } else 0

    // Calculated Statistics
    val totalExams = history.size
    val passedExams = history.count { it.isPassed }
    val failedExams = totalExams - passedExams
    val passRatePercent = if (totalExams > 0) ((passedExams.toFloat() / totalExams) * 100).toInt() else 0
    val averageScore = if (totalExams > 0) (history.map { it.totalScore }.average()).toInt() else 0
    val bestScore = if (totalExams > 0) history.maxOf { it.totalScore } else 0

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(Res.string.clear_history_dialog_title)) },
            text = { Text(stringResource(Res.string.clear_history_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    DataManager.clearExamHistory()
                    showClearHistoryDialog = false
                }) {
                    Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.settings_title)
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
            // Category Selector
            Text(
                text = stringResource(Res.string.select_category, selectedCategory),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Database info banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.db_banner_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (questions.isEmpty()) {
                            stringResource(Res.string.db_empty_message)
                        } else {
                            stringResource(Res.string.db_questions_count, selectedCategory, categoryQuestionsCount, questions.size)
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Main Action Button
            Button(
                onClick = onStartExam,
                enabled = questions.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    stringResource(Res.string.btn_start_exam),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Learning Progress & Practice Mode Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.learning_progress_title, selectedCategory),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$categoryPassedCount / $categoryQuestionsCount ($learningPercent%)",
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { if (categoryQuestionsCount > 0) categoryPassedCount.toFloat() / categoryQuestionsCount.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2E7D32),
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                    )

                    Button(
                        onClick = onOpenPractice,
                        enabled = questions.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.btn_start_learning),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Statistics Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = stringResource(Res.string.stats_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(Res.string.stats_title),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (totalExams == 0) {
                        Text(
                            text = stringResource(Res.string.no_history_message),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = stringResource(Res.string.stats_pass_rate),
                                value = "$passRatePercent%",
                                valueColor = when {
                                    passRatePercent >= 80 -> Color(0xFF2E7D32)
                                    passRatePercent >= 50 -> Color(0xFFF57F17)
                                    else -> Color(0xFFC62828)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = stringResource(Res.string.stats_total_exams),
                                value = "$totalExams",
                                subtitle = "$passedExams zdał / $failedExams oblał",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = stringResource(Res.string.stats_avg_score),
                                value = "$averageScore / 74",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = stringResource(Res.string.stats_best_score),
                                value = "$bestScore / 74",
                                valueColor = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Pass Rate Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(Res.string.stats_pass_rate),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$passRatePercent%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (passRatePercent >= 80) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { passRatePercent.toFloat() / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (passRatePercent >= 80) Color(0xFF2E7D32) else Color(0xFFC62828),
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }

            // Exam History Section
            if (history.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.exam_history_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showClearHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = stringResource(Res.string.btn_clear_history),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.btn_clear_history),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    history.take(10).forEach { res ->
                        HistoryCard(result = res)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HistoryCard(result: ExamResult) {
    val isPassed = result.isPassed
    val bg = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val statusColor = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
    val statusText = if (isPassed) stringResource(Res.string.exam_passed) else stringResource(Res.string.exam_failed)

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.category_label, result.category),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = statusColor
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatTimestamp(result.timestamp),
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
                Text(
                    text = stringResource(Res.string.pts_format, result.totalScore, result.maxScore),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }

            Surface(
                color = statusColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

fun formatTimestamp(epochMs: Long): String {
    if (epochMs <= 0) return "—"
    val totalSec = epochMs / 1000
    val min = ((totalSec / 60) % 60).toInt()
    val hour = ((totalSec / 3600) % 24).toInt()

    var days = (totalSec / 86400).toInt()
    var year = 1970
    while (true) {
        val daysInYear = if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) 366 else 365
        if (days >= daysInYear) {
            days -= daysInYear
            year++
        } else {
            break
        }
    }

    val daysInMonths = if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
        intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    } else {
        intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    }

    var month = 0
    while (month < 12 && days >= daysInMonths[month]) {
        days -= daysInMonths[month]
        month++
    }
    val day = days + 1
    val monthNumber = month + 1

    fun pad(n: Int) = if (n < 10) "0$n" else "$n"
    return "${pad(day)}.${pad(monthNumber)}.$year ${pad(hour)}:${pad(min)}"
}
