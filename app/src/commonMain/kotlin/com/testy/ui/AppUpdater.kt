package com.testy.ui

expect object AppUpdater {
    suspend fun checkForUpdates()
}
