package com.testy.model

import kotlinx.serialization.Serializable

@Serializable
enum class CorrectAnswer {
    TAK,
    NIE,
    A,
    B,
    C;

    companion object {
        fun parse(value: String): CorrectAnswer {
            return when (value.trim().uppercase()) {
                "T", "TAK", "YES" -> TAK
                "N", "NIE", "NO" -> NIE
                "A" -> A
                "B" -> B
                "C" -> C
                else -> TAK
            }
        }
    }
}
