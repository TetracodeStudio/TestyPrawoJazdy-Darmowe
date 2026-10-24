package com.testy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testy.data.DataManager
import com.testy.model.CorrectAnswer
import com.testy.model.ExamSession
import com.testy.ui.player.VideoPlayer
import com.testy.ui.util.decodeImageBitmap
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    session: ExamSession,
    onAnswerSelected: (CorrectAnswer) -> Unit,
    onFinishExam: () -> Unit,
    onCancelExam: () -> Unit
) {
    val currentQuestion = session.currentQuestion
    val questionIndex = session.currentIndex
    val totalQuestions = session.questions.size
    var selectedAnswer by remember(questionIndex) { mutableStateOf<CorrectAnswer?>(session.userAnswers[currentQuestion?.id]) }
    var showQuitDialog by remember { mutableStateOf(false) }

    val isBasic = currentQuestion?.isBasic == true
    val maxQuestionTime = if (isBasic) 35 else 50
    var questionSecondsLeft by remember(questionIndex) { mutableStateOf(maxQuestionTime) }
    var globalSecondsLeft by remember { mutableStateOf(1500) } // 25 min total

    // Global Exam Timer Ticking
    LaunchedEffect(Unit) {
        while (globalSecondsLeft > 0) {
            delay(1000L)
            globalSecondsLeft--
            if (globalSecondsLeft == 0) {
                onFinishExam()
            }
        }
    }

    // Question Timer Ticking & Auto-Advance on Timeout
    LaunchedEffect(questionIndex, questionSecondsLeft) {
        if (questionSecondsLeft > 0) {
            delay(1000L)
            questionSecondsLeft--
        } else {
            selectedAnswer?.let { ans ->
                onAnswerSelected(ans)
            } ?: run {
                onAnswerSelected(if (isBasic) CorrectAnswer.TAK else CorrectAnswer.A)
            }
            if (questionIndex + 1 >= totalQuestions) {
                onFinishExam()
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text(stringResource(Res.string.quit_dialog_title)) },
            text = { Text(stringResource(Res.string.quit_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onCancelExam()
                }) {
                    Text(stringResource(Res.string.quit_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    val globalMinutes = globalSecondsLeft / 60
    val globalSeconds = globalSecondsLeft % 60
    val formattedGlobalTime = remember(globalSecondsLeft) {
        val secStr = if (globalSeconds < 10) "0$globalSeconds" else "$globalSeconds"
        "$globalMinutes:$secStr min"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.question_progress, questionIndex + 1, totalQuestions) + "  |  ",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = stringResource(Res.string.time),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedGlobalTime,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isBasic) stringResource(Res.string.part1_title) else stringResource(Res.string.part2_title),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    currentQuestion?.let { q ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.points_tag, q.points),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    TextButton(onClick = { showQuitDialog = true }) {
                        Text(stringResource(Res.string.quit), color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { padding ->
        if (currentQuestion == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.no_questions))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Question Timer Progress Bar
                val progressRatio = questionSecondsLeft.toFloat() / maxQuestionTime.toFloat()
                val timerColor = when {
                    questionSecondsLeft > 10 -> Color(0xFF2E7D32)
                    questionSecondsLeft > 5 -> Color(0xFFF57F17)
                    else -> Color(0xFFC62828)
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = stringResource(Res.string.time),
                                    tint = timerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(Res.string.time_left_question, questionSecondsLeft),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = timerColor
                                )
                            }
                            Text(
                                text = if (questionSecondsLeft <= 5) stringResource(Res.string.auto_advance_warning) else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.Red
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progressRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = timerColor,
                            trackColor = timerColor.copy(alpha = 0.2f)
                        )
                    }
                }

                // Media preview container
                val mediaFile = currentQuestion.mediaFileName
                if (!mediaFile.isNullOrBlank()) {
                    val mediaBytes = remember(mediaFile) { DataManager.getMediaBytes(mediaFile) }
                    MediaViewer(
                        mediaFile = mediaFile,
                        mediaBytes = mediaBytes
                    )
                }

                // Question Text Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = currentQuestion.question,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Answer Options
                if (currentQuestion.isBasic) {
                    // YES / NO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnswerOptionButton(
                            text = stringResource(Res.string.btn_yes),
                            isSelected = selectedAnswer == CorrectAnswer.TAK,
                            onClick = { selectedAnswer = CorrectAnswer.TAK },
                            modifier = Modifier.weight(1f)
                        )
                        AnswerOptionButton(
                            text = stringResource(Res.string.btn_no),
                            isSelected = selectedAnswer == CorrectAnswer.NIE,
                            onClick = { selectedAnswer = CorrectAnswer.NIE },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // A / B / C
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.answerA?.let { a ->
                            AnswerCard(
                                label = "A",
                                text = a,
                                isSelected = selectedAnswer == CorrectAnswer.A,
                                onClick = { selectedAnswer = CorrectAnswer.A }
                            )
                        }
                        currentQuestion.answerB?.let { b ->
                            AnswerCard(
                                label = "B",
                                text = b,
                                isSelected = selectedAnswer == CorrectAnswer.B,
                                onClick = { selectedAnswer = CorrectAnswer.B }
                            )
                        }
                        currentQuestion.answerC?.let { c ->
                            AnswerCard(
                                label = "C",
                                text = c,
                                isSelected = selectedAnswer == CorrectAnswer.C,
                                onClick = { selectedAnswer = CorrectAnswer.C }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm / Next Button
                Button(
                    onClick = {
                        selectedAnswer?.let { ans ->
                            onAnswerSelected(ans)
                            if (questionIndex + 1 >= totalQuestions) {
                                onFinishExam()
                            }
                        }
                    },
                    enabled = selectedAnswer != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (questionIndex + 1 >= totalQuestions) stringResource(Res.string.btn_finish_exam) else stringResource(Res.string.btn_confirm_next),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MediaViewer(
    mediaFile: String,
    mediaBytes: ByteArray?
) {
    val lower = remember(mediaFile) { mediaFile.lowercase() }
    val isImage = remember(lower) {
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                lower.endsWith(".webp") || lower.endsWith(".bmp") || lower.endsWith(".gif")
    }
    val isVideo = remember(lower) {
        lower.endsWith(".wmv") || lower.endsWith(".mp4") || lower.endsWith(".avi") ||
                lower.endsWith(".mov") || lower.endsWith(".mkv")
    }

    val videoFilePath = remember(mediaFile) { DataManager.getMediaFilePath(mediaFile) }
    val imageBitmap = remember(mediaBytes) {
        if (isImage && mediaBytes != null) {
            decodeImageBitmap(mediaBytes)
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isImage && imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = stringResource(Res.string.question_image_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else if (isVideo && videoFilePath != null) {
                VideoPlayer(
                    filePath = videoFilePath,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (mediaBytes != null) {
                // Fallback for media loaded in memory
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.media_bytes_format, mediaBytes.size / 1024),
                        color = Color(0xFFA5D6A7),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = mediaFile,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // Missing media file
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.media_missing_title),
                        color = Color(0xFFFFCC80),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(Res.string.media_missing_file, mediaFile),
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = stringResource(Res.string.media_missing_hint),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerOptionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.height(60.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnswerCard(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
