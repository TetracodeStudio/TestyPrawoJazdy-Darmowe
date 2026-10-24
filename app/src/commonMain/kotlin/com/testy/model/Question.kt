package com.testy.model

import com.testy.parser.normalizeMediaKey
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val question: String,
    val answerA: String? = null,
    val answerB: String? = null,
    val answerC: String? = null,
    val correctAnswer: CorrectAnswer,
    val mediaFileName: String? = null,
    val scope: QuestionScope,
    val points: Int,
    val categories: List<String>,
    val pjmMedia: String? = null,
    val englishQuestion: String? = null,
    val germanQuestion: String? = null,
    val ukrainianQuestion: String? = null
) {
    val isBasic: Boolean get() = scope == QuestionScope.PODSTAWOWY
    val isSpecialist: Boolean get() = scope == QuestionScope.SPECJALISTYCZNY

    val normalizedMediaFileName: String? get() = mediaFileName?.let { normalizeMediaKey(it) }

    fun matchesCategory(category: String): Boolean {
        if (categories.isEmpty()) return true
        val target = category.trim().uppercase()
        return categories.any { it.trim().uppercase() == target }
    }
}
