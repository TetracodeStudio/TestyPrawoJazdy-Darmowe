package com.testy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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

enum class ExamQuestionPhase {
    READING,       // 20s initial reading phase with blurred media cover + START button
    MEDIA_PLAYING, // Video media is unblurred and actively playing
    ANSWERING      // 15s (or 50s for ABC) answering phase with active buttons
}

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

    val isBasic = currentQuestion?.isBasic == true

    val lowerMedia = remember(currentQuestion?.mediaFileName) {
        currentQuestion?.mediaFileName?.lowercase().orEmpty()
    }
    val isVideoMedia = remember(lowerMedia) {
        lowerMedia.endsWith(".wmv") || lowerMedia.endsWith(".mp4") ||
                lowerMedia.endsWith(".avi") || lowerMedia.endsWith(".mov") || lowerMedia.endsWith(".mkv")
    }

    // Phase State
    var currentPhase by remember(questionIndex) {
        mutableStateOf(if (isBasic) ExamQuestionPhase.READING else ExamQuestionPhase.ANSWERING)
    }

    var readingSecondsLeft by remember(questionIndex) { mutableStateOf(20) }
    var answeringSecondsLeft by remember(questionIndex) { mutableStateOf(if (isBasic) 15 else 50) }

    var selectedAnswer by remember(questionIndex) { mutableStateOf<CorrectAnswer?>(session.userAnswers[currentQuestion?.id]) }
    var showQuitDialog by remember { mutableStateOf(false) }

    var globalSecondsLeft by remember { mutableStateOf(1500) } // 25 min total

    val startMediaOrAnswering = remember(questionIndex, isVideoMedia, currentPhase) {
        {
            if (currentPhase == ExamQuestionPhase.READING) {
                if (isVideoMedia) {
                    currentPhase = ExamQuestionPhase.MEDIA_PLAYING
                } else {
                    currentPhase = ExamQuestionPhase.ANSWERING
                }
            }
        }
    }

    val onVideoEnded = remember(questionIndex) {
        {
            if (currentPhase == ExamQuestionPhase.MEDIA_PLAYING) {
                currentPhase = ExamQuestionPhase.ANSWERING
            }
        }
    }

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

    // 1. Reading Phase Timer Ticking (20s)
    LaunchedEffect(questionIndex, currentPhase, readingSecondsLeft) {
        if (isBasic && currentPhase == ExamQuestionPhase.READING) {
            if (readingSecondsLeft > 0) {
                delay(1000L)
                readingSecondsLeft--
            } else {
                startMediaOrAnswering()
            }
        }
    }

    // 2. Video Playback Safety Fallback Timer (max 35s)
    LaunchedEffect(questionIndex, currentPhase) {
        if (isBasic && currentPhase == ExamQuestionPhase.MEDIA_PLAYING) {
            delay(35000L)
            if (currentPhase == ExamQuestionPhase.MEDIA_PLAYING) {
                currentPhase = ExamQuestionPhase.ANSWERING
            }
        }
    }

    // 3. Answering Phase Timer Ticking & Auto-Advance on Timeout
    LaunchedEffect(questionIndex, currentPhase, answeringSecondsLeft) {
        if (currentPhase == ExamQuestionPhase.ANSWERING) {
            if (answeringSecondsLeft > 0) {
                delay(1000L)
                answeringSecondsLeft--
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
                // Live Phase Timer & Progress Bar
                val (progressRatio, timerText, timerColor) = when (currentPhase) {
                    ExamQuestionPhase.READING -> {
                        Triple(
                            readingSecondsLeft.toFloat() / 20f,
                            stringResource(Res.string.reading_phase_time, readingSecondsLeft),
                            Color(0xFF0288D1)
                        )
                    }
                    ExamQuestionPhase.MEDIA_PLAYING -> {
                        Triple(
                            1f,
                            stringResource(Res.string.video_playing_status),
                            Color(0xFF7B1FA2)
                        )
                    }
                    ExamQuestionPhase.ANSWERING -> {
                        val maxAnsTime = if (isBasic) 15 else 50
                        val color = when {
                            answeringSecondsLeft > 10 -> Color(0xFF2E7D32)
                            answeringSecondsLeft > 5 -> Color(0xFFF57F17)
                            else -> Color(0xFFC62828)
                        }
                        Triple(
                            answeringSecondsLeft.toFloat() / maxAnsTime.toFloat(),
                            stringResource(Res.string.answering_phase_time, answeringSecondsLeft),
                            color
                        )
                    }
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
                                    text = timerText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = timerColor
                                )
                            }
                            Text(
                                text = if (currentPhase == ExamQuestionPhase.ANSWERING && answeringSecondsLeft <= 5)
                                    stringResource(Res.string.auto_advance_warning) else "",
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

                // Media Preview Container with Blur Overlay and START button
                val mediaFile = currentQuestion.mediaFileName
                if (!mediaFile.isNullOrBlank()) {
                    val mediaBytes = remember(mediaFile) { DataManager.getMediaBytes(mediaFile) }
                    MediaViewer(
                        mediaFile = mediaFile,
                        mediaBytes = mediaBytes,
                        isBlurred = isBasic && currentPhase == ExamQuestionPhase.READING,
                        onStartMedia = startMediaOrAnswering,
                        onVideoEnded = onVideoEnded
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
                val isAnsweringEnabled = currentPhase == ExamQuestionPhase.ANSWERING

                if (currentQuestion.isBasic) {
                    // YES / NO
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnswerOptionButton(
                            text = stringResource(Res.string.btn_yes),
                            isSelected = selectedAnswer == CorrectAnswer.TAK,
                            enabled = isAnsweringEnabled,
                            onClick = { selectedAnswer = CorrectAnswer.TAK },
                            modifier = Modifier.weight(1f)
                        )
                        AnswerOptionButton(
                            text = stringResource(Res.string.btn_no),
                            isSelected = selectedAnswer == CorrectAnswer.NIE,
                            enabled = isAnsweringEnabled,
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
                                enabled = isAnsweringEnabled,
                                onClick = { selectedAnswer = CorrectAnswer.A }
                            )
                        }
                        currentQuestion.answerB?.let { b ->
                            AnswerCard(
                                label = "B",
                                text = b,
                                isSelected = selectedAnswer == CorrectAnswer.B,
                                enabled = isAnsweringEnabled,
                                onClick = { selectedAnswer = CorrectAnswer.B }
                            )
                        }
                        currentQuestion.answerC?.let { c ->
                            AnswerCard(
                                label = "C",
                                text = c,
                                isSelected = selectedAnswer == CorrectAnswer.C,
                                enabled = isAnsweringEnabled,
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
                    enabled = selectedAnswer != null && isAnsweringEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (questionIndex + 1 >= totalQuestions)
                            stringResource(Res.string.btn_finish_exam) else stringResource(Res.string.btn_confirm_next),
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
    mediaBytes: ByteArray?,
    isBlurred: Boolean,
    onStartMedia: () -> Unit,
    onVideoEnded: () -> Unit
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
            if (isBlurred) {
                // Blurred Cover + START Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImage && imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = stringResource(Res.string.question_image_desc),
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(20.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            onClick = onStartMedia,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = stringResource(Res.string.btn_start_media),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onStartMedia,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(Res.string.btn_start_media),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = stringResource(Res.string.start_media_hint),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Unblurred Media Display
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
                        modifier = Modifier.fillMaxSize(),
                        onVideoEnded = onVideoEnded
                    )
                } else if (mediaBytes != null) {
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
}

@Composable
fun AnswerOptionButton(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(16.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            isSelected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = if (isSelected && enabled) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 1f else 0.3f)
        ),
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = { if (enabled) onClick() },
        shape = RoundedCornerShape(14.dp),
        color = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected && enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = if (enabled) 0.5f else 0.2f)
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
                    .background(
                        when {
                            !enabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = when {
                        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
