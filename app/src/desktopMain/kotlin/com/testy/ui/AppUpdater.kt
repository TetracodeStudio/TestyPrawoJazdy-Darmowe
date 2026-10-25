package com.testy.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

actual object AppUpdater {
    private const val UPDATE_URL = "https://raw.githubusercontent.com/TetracodeStudio/TestyPrawoJazdy-Darmowe/refs/heads/master/update.json"
    const val versionCode = 1
    actual suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val conn = URL(UPDATE_URL).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                if (conn.responseCode == 200) {
                    val json = Json.parseToJsonElement(conn.inputStream.bufferedReader().readText()).jsonObject
                    val latestCode = json["versionCode"]?.jsonPrimitive?.intOrNull ?: 0
                    val desktopUrl = json["desktopUrl"]?.jsonPrimitive?.content ?: ""

                    if (latestCode > versionCode && desktopUrl.isNotEmpty()) {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI(desktopUrl))
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
