package com.testy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testy.data.DataManager
import com.testy.model.ExamResult
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onStartExam: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val questions by DataManager.questions.collectAsState()
    val history by DataManager.examHistory.collectAsState()
    val availableCategories = DataManager.availableCategories

    val categoryQuestionsCount = remember(questions, selectedCategory) {
        questions.count { it.matchesCategory(selectedCategory) }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selector
            Text(
                text = stringResource(Res.string.select_category),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableCategories) { cat ->
                    val isSelected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onCategorySelected(cat) }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(Res.string.category_label, cat),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

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
                        text = stringResource(Res.string.db_banner_title, selectedCategory),
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

            // Main Action Buttons
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

            Spacer(modifier = Modifier.weight(1f))

            // Exam History Section
            if (history.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.exam_history_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(history.take(5)) { res ->
                        HistoryChip(result = res)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryChip(result: ExamResult) {
    val bg = if (result.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val fg = if (result.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)

    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, fg.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.category_label, result.category),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = fg
            )
            Text(
                text = stringResource(Res.string.pts_format, result.totalScore, result.maxScore),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = fg
            )
            Text(
                text = if (result.isPassed) stringResource(Res.string.exam_passed) else stringResource(Res.string.exam_failed),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}
