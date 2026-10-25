package com.testy.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RestartAlt
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
import com.testy.model.Question
import com.testy.ui.player.VideoPlayer
import com.testy.ui.util.decodeImageBitmap
import org.jetbrains.compose.resources.stringResource
import testy.app.generated.resources.*

data class QuestionTopic(
    val id: String,
    val name: String,
    val description: String,
    val filter: (Question) -> Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    selectedCategory: String,
    onBack: () -> Unit
) {
    val questions by DataManager.questions.collectAsState()
    val passedMap by DataManager.passedQuestionIds.collectAsState()

    val categoryQuestions = remember(questions, selectedCategory) {
        DataManager.getQuestionsForCategory(selectedCategory)
    }

    val passedSet = remember(passedMap, selectedCategory) {
        passedMap[selectedCategory] ?: emptySet()
    }

    var selectedTopic by remember { mutableStateOf<QuestionTopic?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val topics = remember(selectedCategory) {
        listOf(
            QuestionTopic("all", "Pełna baza pytań", "Wszystkie pytania dla Kat. $selectedCategory") { true },
            QuestionTopic("basic_3", "Podstawowe - 3 pkt", "Pytania o wysokim znaczeniu dla bezpieczeństwa (TAK/NIE)") { it.isBasic && it.points == 3 },
            QuestionTopic("basic_2", "Podstawowe - 2 pkt", "Pytania o średnim znaczeniu dla bezpieczeństwa (TAK/NIE)") { it.isBasic && it.points == 2 },
            QuestionTopic("basic_1", "Podstawowe - 1 pkt", "Pytania o niskim znaczeniu dla bezpieczeństwa (TAK/NIE)") { it.isBasic && it.points == 1 },
            QuestionTopic("spec_3", "Specjalistyczne - 3 pkt", "Wiedza specjalistyczna (A/B/C)") { it.isSpecialist && it.points == 3 },
            QuestionTopic("spec_2", "Specjalistyczne - 2 pkt", "Wiedza specjalistyczna (A/B/C)") { it.isSpecialist && it.points == 2 },
            QuestionTopic("spec_1", "Specjalistyczne - 1 pkt", "Wiedza specjalistyczna (A/B/C)") { it.isSpecialist && it.points == 1 }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(Res.string.reset_progress_dialog_title)) },
            text = { Text(stringResource(Res.string.reset_progress_dialog_text, selectedCategory)) },
            confirmButton = {
                TextButton(onClick = {
                    DataManager.resetLearningProgressForCategory(selectedCategory)
                    showResetDialog = false
                }) {
                    Text(stringResource(Res.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    if (selectedTopic == null) {
        // Topic Selection View
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.practice_screen_title, selectedCategory), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                // Category Progress Overview Card
                val totalCatCount = categoryQuestions.size
                val passedCatCount = categoryQuestions.count { it.id in passedSet }
                val percentCat = if (totalCatCount > 0) ((passedCatCount.toFloat() / totalCatCount) * 100).toInt() else 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.learning_progress_title, selectedCategory),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { showResetDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = stringResource(Res.string.btn_reset_category_progress, selectedCategory),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            text = stringResource(Res.string.learning_progress_summary, passedCatCount, totalCatCount, percentCat),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        LinearProgressIndicator(
                            progress = { if (totalCatCount > 0) passedCatCount.toFloat() / totalCatCount.toFloat() else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF2E7D32),
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.all_topics_tab),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // List of Działy (Topics)
                topics.forEach { topic ->
                    val topicQuestions = remember(categoryQuestions, topic) {
                        categoryQuestions.filter(topic.filter)
                    }
                    if (topicQuestions.isNotEmpty()) {
                        val totalCount = topicQuestions.size
                        val passedCount = topicQuestions.count { it.id in passedSet }
                        val percent = if (totalCount > 0) ((passedCount.toFloat() / totalCount) * 100).toInt() else 0

                        Card(
                            onClick = { selectedTopic = topic },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = topic.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = topic.description,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = { if (totalCount > 0) passedCount.toFloat() / totalCount.toFloat() else 0f },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF2E7D32),
                                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                        Text(
                                            text = "$passedCount / $totalCount ($percent%)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (passedCount == totalCount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Active Topic Practice Flow View
        val topic = selectedTopic!!
        val topicQuestions = remember(categoryQuestions, topic) {
            categoryQuestions.filter(topic.filter)
        }

        TopicPracticeView(
            topicName = topic.name,
            category = selectedCategory,
            questions = topicQuestions,
            passedSet = passedSet,
            onBackToTopics = { selectedTopic = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicPracticeView(
    topicName: String,
    category: String,
    questions: List<Question>,
    passedSet: Set<String>,
    onBackToTopics: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var hidePassedOnly by remember { mutableStateOf(false) }

    val activeQuestions = remember(questions, hidePassedOnly, passedSet) {
        if (hidePassedOnly) {
            questions.filter { it.id !in passedSet }
        } else {
            questions
        }
    }

    val safeIndex = currentIndex.coerceIn(0, (activeQuestions.size - 1).coerceAtLeast(0))
    val currentQuestion = activeQuestions.getOrNull(safeIndex)

    var selectedAnswer by remember(safeIndex, currentQuestion?.id) { mutableStateOf<CorrectAnswer?>(null) }
    var hasAnswered by remember(safeIndex, currentQuestion?.id) { mutableStateOf(false) }

    val isPassed = currentQuestion != null && currentQuestion.id in passedSet

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(topicName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (activeQuestions.isNotEmpty()) {
                            Text(
                                text = "Pytanie ${safeIndex + 1} / ${activeQuestions.size}  |  Zaliczonych: ${questions.count { it.id in passedSet }}/${questions.size}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToTopics) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ukryj zaliczone",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = hidePassedOnly,
                            onCheckedChange = {
                                hidePassedOnly = it
                                currentIndex = 0
                            },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    ) { padding ->
        if (currentQuestion == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Wszystkie pytania w tym dziale zostały zaliczone!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBackToTopics) {
                        Text("Powrót do działów")
                    }
                }
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
                // Status Badge & Points Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPassed) stringResource(Res.string.question_passed_badge) else stringResource(Res.string.question_unpassed_badge),
                            color = if (isPassed) Color(0xFF2E7D32) else Color(0xFFE65100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.points_tag, currentQuestion.points),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Media Preview
                val mediaFile = currentQuestion.mediaFileName
                if (!mediaFile.isNullOrBlank()) {
                    val mediaBytes = remember(mediaFile) { DataManager.getMediaBytes(mediaFile) }
                    PracticeMediaViewer(
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

                // Answer Options (TAK/NIE or A/B/C)
                fun handleSelectAnswer(ans: CorrectAnswer) {
                    selectedAnswer = ans
                    hasAnswered = true
                    if (ans == currentQuestion.correctAnswer) {
                        DataManager.markQuestionPassed(category, currentQuestion.id)
                    }
                }

                if (currentQuestion.isBasic) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PracticeAnswerButton(
                            text = stringResource(Res.string.btn_yes),
                            optionAnswer = CorrectAnswer.TAK,
                            correctAnswer = currentQuestion.correctAnswer,
                            selectedAnswer = selectedAnswer,
                            hasAnswered = hasAnswered,
                            onClick = { handleSelectAnswer(CorrectAnswer.TAK) },
                            modifier = Modifier.weight(1f)
                        )
                        PracticeAnswerButton(
                            text = stringResource(Res.string.btn_no),
                            optionAnswer = CorrectAnswer.NIE,
                            correctAnswer = currentQuestion.correctAnswer,
                            selectedAnswer = selectedAnswer,
                            hasAnswered = hasAnswered,
                            onClick = { handleSelectAnswer(CorrectAnswer.NIE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        currentQuestion.answerA?.let { a ->
                            PracticeAnswerCard(
                                label = "A",
                                text = a,
                                optionAnswer = CorrectAnswer.A,
                                correctAnswer = currentQuestion.correctAnswer,
                                selectedAnswer = selectedAnswer,
                                hasAnswered = hasAnswered,
                                onClick = { handleSelectAnswer(CorrectAnswer.A) }
                            )
                        }
                        currentQuestion.answerB?.let { b ->
                            PracticeAnswerCard(
                                label = "B",
                                text = b,
                                optionAnswer = CorrectAnswer.B,
                                correctAnswer = currentQuestion.correctAnswer,
                                selectedAnswer = selectedAnswer,
                                hasAnswered = hasAnswered,
                                onClick = { handleSelectAnswer(CorrectAnswer.B) }
                            )
                        }
                        currentQuestion.answerC?.let { c ->
                            PracticeAnswerCard(
                                label = "C",
                                text = c,
                                optionAnswer = CorrectAnswer.C,
                                correctAnswer = currentQuestion.correctAnswer,
                                selectedAnswer = selectedAnswer,
                                hasAnswered = hasAnswered,
                                onClick = { handleSelectAnswer(CorrectAnswer.C) }
                            )
                        }
                    }
                }

                // Result Notification Banner
                if (hasAnswered) {
                    val isCorrect = selectedAnswer == currentQuestion.correctAnswer
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCorrect)
                                stringResource(Res.string.answer_correct)
                            else
                                stringResource(Res.string.answer_incorrect, currentQuestion.correctAnswer.name),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Navigation (Prev / Next)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (safeIndex > 0) currentIndex = safeIndex - 1
                        },
                        enabled = safeIndex > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(Res.string.btn_prev_question))
                    }

                    Button(
                        onClick = {
                            if (safeIndex + 1 < activeQuestions.size) {
                                currentIndex = safeIndex + 1
                            }
                        },
                        enabled = safeIndex + 1 < activeQuestions.size,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(Res.string.btn_next_question), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PracticeMediaViewer(
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
            .height(240.dp),
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
                Text(
                    text = stringResource(Res.string.media_bytes_format, mediaBytes.size / 1024),
                    color = Color(0xFFA5D6A7),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = stringResource(Res.string.media_missing_file, mediaFile),
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PracticeAnswerButton(
    text: String,
    optionAnswer: CorrectAnswer,
    correctAnswer: CorrectAnswer,
    selectedAnswer: CorrectAnswer?,
    hasAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        hasAnswered && optionAnswer == correctAnswer -> Color(0xFF2E7D32)
        hasAnswered && selectedAnswer == optionAnswer && optionAnswer != correctAnswer -> Color(0xFFC62828)
        selectedAnswer == optionAnswer -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentBg = when {
        hasAnswered && (optionAnswer == correctAnswer || selectedAnswer == optionAnswer) -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bg,
        contentColor = contentBg,
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
fun PracticeAnswerCard(
    label: String,
    text: String,
    optionAnswer: CorrectAnswer,
    correctAnswer: CorrectAnswer,
    selectedAnswer: CorrectAnswer?,
    hasAnswered: Boolean,
    onClick: () -> Unit
) {
    val isCorrectOption = optionAnswer == correctAnswer
    val isSelectedOption = selectedAnswer == optionAnswer

    val cardBg = when {
        hasAnswered && isCorrectOption -> Color(0xFFE8F5E9)
        hasAnswered && isSelectedOption && !isCorrectOption -> Color(0xFFFFEBEE)
        isSelectedOption -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        hasAnswered && isCorrectOption -> Color(0xFF2E7D32)
        hasAnswered && isSelectedOption && !isCorrectOption -> Color(0xFFC62828)
        isSelectedOption -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(2.dp, borderColor),
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
                            hasAnswered && isCorrectOption -> Color(0xFF2E7D32)
                            hasAnswered && isSelectedOption && !isCorrectOption -> Color(0xFFC62828)
                            isSelectedOption -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (hasAnswered && (isCorrectOption || isSelectedOption) || isSelectedOption) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelectedOption || (hasAnswered && isCorrectOption)) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
