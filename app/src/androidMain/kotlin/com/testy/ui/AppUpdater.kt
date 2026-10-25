package com.testy.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.testy.R
import com.testy.data.AndroidContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

actual object AppUpdater {
    private const val UPDATE_URL = "https://raw.githubusercontent.com/TetracodeStudio/TestyPrawoJazdy-Darmowe/refs/heads/master/update.json"

    private var downloadId: Long = -1L

    actual suspend fun checkForUpdates() {
        val context = AndroidContextProvider.getContext() ?: return
        withContext(Dispatchers.IO) {
            try {
                val jsonString = fetchJson(UPDATE_URL)
                if (jsonString != null) {
                    val jsonObject = JSONObject(jsonString)
                    val latestVersionCode = when {
                        jsonObject.has("versionCode") -> jsonObject.getInt("versionCode")
                        jsonObject.has("version_code") -> jsonObject.getInt("version_code")
                        else -> 0
                    }
                    val apkUrl = when {
                        jsonObject.has("apkUrl") -> jsonObject.getString("apkUrl")
                        jsonObject.has("apk_url") -> jsonObject.getString("apk_url")
                        jsonObject.has("url") -> jsonObject.getString("url")
                        else -> ""
                    }

                    val currentVersionCode = getAppVersionCode(context)

                    if (latestVersionCode > currentVersionCode && apkUrl.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            startDownload(context, apkUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchJson(urlString: String): String? {
        return try {
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

    private fun getAppVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (_: PackageManager.NameNotFoundException) {
            1
        }
    }

    private fun startDownload(context: Context, urlString: String) {
        val downloadText = try {
            context.getString(R.string.update_downloading)
        } catch (_: Exception) {
            "Pobieranie nowej wersji aplikacji..."
        }
        val titleText = try {
            context.getString(R.string.update_available_title)
        } catch (_: Exception) {
            "Aktualizacja aplikacji Testy Prawo Jazdy"
        }

        Toast.makeText(context, downloadText, Toast.LENGTH_LONG).show()

        val fileName = "TestyPrawoJazdy_Update.apk"
        val uri = Uri.parse(urlString)
        val request = DownloadManager.Request(uri)
            .setTitle(titleText)
            .setDescription(downloadText)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    promptInstall(ctxt)
                    try {
                        ctxt.unregisterReceiver(this)
                    } catch (_: Exception) {}
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun promptInstall(context: Context) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = downloadManager.getUriForDownloadedFile(downloadId)

            if (uri != null) {
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(installIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
