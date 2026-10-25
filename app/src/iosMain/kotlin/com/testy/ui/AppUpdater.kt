package com.testy.ui

actual object AppUpdater {
    actual suspend fun checkForUpdates() {
        // iOS update check or no-op
    }
}
