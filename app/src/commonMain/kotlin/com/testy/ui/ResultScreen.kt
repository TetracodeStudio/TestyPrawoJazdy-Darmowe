package com.testy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testy.model.ExamResult
import com.testy.model.Question
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: ExamResult,
    questions: List<Question>,
    onRestartExam: () -> Unit,
    onReturnHome: () -> Unit
) {
    val passed = result.isPassed
    val score = result.totalScore
    val maxScore = result.maxScore
    val questionsMap = questions.associateBy { it.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.result_title), fontWeight = FontWeight.Bold) },
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
            // Main Result Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (passed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (passed) stringResource(Res.string.result_positive) else stringResource(Res.string.result_negative),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    Text(
                        text = if (passed) stringResource(Res.string.result_passed_banner) else stringResource(Res.string.result_failed_banner),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (passed) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = (if (passed) Color(0xFF2E7D32) else Color(0xFFC62828)).copy(alpha = 0.3f)
                    )

                    Text(
                        text = stringResource(Res.string.result_pts_format, score, maxScore),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (passed) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    Text(
                        text = stringResource(Res.string.result_min_required),
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Text(
                text = stringResource(Res.string.result_review_title, result.answers.size),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Questions review list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(result.answers) { index, ans ->
                    val q = questionsMap[ans.questionId]
                    val isCorrect = ans.isCorrect
                    val cardBg = if (isCorrect) Color(0xFFF1F8E9) else Color(0xFFFFEBEE)
                    val statusText = if (isCorrect) stringResource(Res.string.review_correct_pts, ans.pointsEarned) else stringResource(Res.string.review_incorrect_pts, ans.maxPoints)
                    val statusColor = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(Res.string.review_question_label, index + 1, q?.scope?.name ?: "—"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = statusText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = statusColor
                                )
                            }
                            q?.let { question ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = question.question,
                                    fontSize = 13.sp,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(Res.string.review_answers_summary, ans.selectedAnswer?.name ?: "—", question.correctAnswer.name),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(Res.string.btn_main_menu))
                }

                Button(
                    onClick = onRestartExam,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(Res.string.btn_new_exam), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
