package com.testy.ui

sealed class Screen {
    data object Home : Screen()
    data object Exam : Screen()
    data object Result : Screen()
    data object Settings : Screen()
    data object Import : Screen()
    data object Practice : Screen()
}
