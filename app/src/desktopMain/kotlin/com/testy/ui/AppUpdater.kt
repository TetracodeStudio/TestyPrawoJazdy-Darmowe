package com.testy.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

actual object AppUpdater {
    private const val UPDATE_URL = "https://raw.githubusercontent.com/TetracodeStudio/TestyPrawoJazdy-Darmowe/refs/heads/master/update.json"
    private const val CURRENT_VERSION_CODE = 1

    actual suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = fetchJson(UPDATE_URL)
                if (jsonString != null) {
                    val jsonElement = Json.parseToJsonElement(jsonString)
                    val jsonObject = jsonElement.jsonObject

                    val latestVersionCode = jsonObject["desktopVersionCode"]?.jsonPrimitive?.intOrNull
                        ?: jsonObject["versionCode"]?.jsonPrimitive?.intOrNull
                        ?: jsonObject["version_code"]?.jsonPrimitive?.intOrNull
                        ?: 0

                    val downloadUrl = jsonObject["desktopUrl"]?.jsonPrimitive?.content
                        ?: jsonObject["url"]?.jsonPrimitive?.content
                        ?: jsonObject["apkUrl"]?.jsonPrimitive?.content
                        ?: "https://github.com/TetracodeStudio/TestyPrawoJazdy-Darmowe/releases"

                    if (latestVersionCode > CURRENT_VERSION_CODE) {
                        openInBrowser(downloadUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchJson(urlString: String): String? {
        return try {
            @Suppress("DEPRECATION")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val result = reader.readText()
                connection.disconnect()
                result
            } else {
                connection.disconnect()
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun openInBrowser(urlString: String) {
        try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(urlString))
                    return
                }
            }
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> Runtime.getRuntime().exec(arrayOf("rundll32", "url.dll,FileProtocolHandler", urlString))
                os.contains("mac") -> Runtime.getRuntime().exec(arrayOf("open", urlString))
                else -> Runtime.getRuntime().exec(arrayOf("xdg-open", urlString))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
