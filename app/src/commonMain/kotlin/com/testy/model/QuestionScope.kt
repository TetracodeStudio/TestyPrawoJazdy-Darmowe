package com.testy.model

import kotlinx.serialization.Serializable

@Serializable
enum class QuestionScope {
    PODSTAWOWY,
    SPECJALISTYCZNY;

    companion object {
        fun parse(value: String): QuestionScope {
            return if (value.trim().uppercase().contains("SPECJALISTYCZNY")) {
                SPECJALISTYCZNY
            } else {
                PODSTAWOWY
            }
        }
    }
}
