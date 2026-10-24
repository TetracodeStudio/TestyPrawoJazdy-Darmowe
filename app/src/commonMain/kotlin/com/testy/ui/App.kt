package com.testy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.testy.data.DataManager
import com.testy.domain.ExamGenerator
import com.testy.model.ExamResult
import com.testy.model.ExamSession

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedCategory by remember { mutableStateOf("B") }
    var currentSession by remember { mutableStateOf<ExamSession?>(null) }
    var lastResult by remember { mutableStateOf<ExamResult?>(null) }

    MaterialTheme {
        Surface {
            when (currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        onStartExam = {
                            val questions = DataManager.getQuestionsForCategory(selectedCategory)
                            val session = ExamGenerator.generateExam(selectedCategory, questions)
                            currentSession = session
                            currentScreen = Screen.Exam
                        },
                        onOpenSettings = {
                            currentScreen = Screen.Settings
                        }
                    )
                }

                is Screen.Exam -> {
                    currentSession?.let { session ->
                        ExamScreen(
                            session = session,
                            onAnswerSelected = { answer ->
                                val updated = session.answerCurrentQuestion(answer)
                                currentSession = updated
                            },
                            onFinishExam = {
                                currentSession?.let { finishedSession ->
                                    val result = finishedSession.getResult()
                                    DataManager.addExamResult(result)
                                    lastResult = result
                                    currentScreen = Screen.Result
                                }
                            },
                            onCancelExam = {
                                currentSession = null
                                currentScreen = Screen.Home
                            }
                        )
                    } ?: run {
                        currentScreen = Screen.Home
                    }
                }

                is Screen.Result -> {
                    lastResult?.let { res ->
                        ResultScreen(
                            result = res,
                            questions = currentSession?.questions ?: emptyList(),
                            onRestartExam = {
                                val questions = DataManager.getQuestionsForCategory(selectedCategory)
                                val session = ExamGenerator.generateExam(selectedCategory, questions)
                                currentSession = session
                                currentScreen = Screen.Exam
                            },
                            onReturnHome = {
                                currentSession = null
                                currentScreen = Screen.Home
                            }
                        )
                    } ?: run {
                        currentScreen = Screen.Home
                    }
                }

                is Screen.Settings -> {
                    SettingsScreen(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        onBack = { currentScreen = Screen.Home }
                    )
                }

                is Screen.Import -> {
                    SettingsScreen(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        onBack = { currentScreen = Screen.Home }
                    )
                }

                is Screen.Practice -> {
                    SettingsScreen(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        onBack = { currentScreen = Screen.Home }
                    )
                }
            }
        }
    }
}
