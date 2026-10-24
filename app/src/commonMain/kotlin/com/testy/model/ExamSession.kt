package com.testy.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAnswer(
    val questionId: String,
    val selectedAnswer: CorrectAnswer?,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val maxPoints: Int
)

@Serializable
data class ExamSession(
    val category: String,
    val questions: List<Question>,
    val userAnswers: Map<String, CorrectAnswer> = emptyMap(),
    val currentIndex: Int = 0,
    val isFinished: Boolean = false,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)

    val totalPointsEarned: Int get() {
        return questions.sumOf { question ->
            val userAnswer = userAnswers[question.id]
            if (userAnswer == question.correctAnswer) question.points else 0
        }
    }

    val maxPoints: Int get() = questions.sumOf { it.points }

    val isPassed: Boolean get() = totalPointsEarned >= 68

    fun answerCurrentQuestion(answer: CorrectAnswer): ExamSession {
        val q = currentQuestion ?: return this
        val updatedAnswers = userAnswers + (q.id to answer)
        val nextIndex = currentIndex + 1
        val finished = nextIndex >= questions.size
        return copy(
            userAnswers = updatedAnswers,
            currentIndex = if (finished) currentIndex else nextIndex,
            isFinished = finished
        )
    }

    fun getResult(): ExamResult {
        val answersList = questions.map { q ->
            val selected = userAnswers[q.id]
            val correct = selected == q.correctAnswer
            UserAnswer(
                questionId = q.id,
                selectedAnswer = selected,
                isCorrect = correct,
                pointsEarned = if (correct) q.points else 0,
                maxPoints = q.points
            )
        }
        return ExamResult(
            category = category,
            totalScore = totalPointsEarned,
            maxScore = maxPoints,
            isPassed = isPassed,
            answers = answersList,
            timestamp = endTimeMillis.takeIf { it > 0 } ?: startTimeMillis
        )
    }
}
