package com.testy.model

import kotlinx.serialization.Serializable

@Serializable
data class ExamResult(
    val category: String,
    val totalScore: Int,
    val maxScore: Int = 74,
    val isPassed: Boolean,
    val answers: List<UserAnswer>,
    val timestamp: Long
)
