package com.testy.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.testy.data.AndroidContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

actual object AppUpdater {
    private const val UPDATE_URL = "https://raw.githubusercontent.com/TetracodeStudio/TestyPrawoJazdy-Darmowe/refs/heads/master/update.json"

    actual suspend fun checkForUpdates() {
        val context = AndroidContextProvider.getContext() ?: return
        withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val conn = URL(UPDATE_URL).openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                if (conn.responseCode == 200) {
                    val json = JSONObject(conn.inputStream.bufferedReader().readText())
                    val latestCode = json.optInt("versionCode", 0)
                    val apkUrl = json.optString("apkUrl", "")

                    val currentCode = try {
                        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode.toInt() else @Suppress("DEPRECATION") pInfo.versionCode
                    } catch (_: Exception) { 1 }

                    if (latestCode > currentCode && apkUrl.isNotEmpty()) {
                        withContext(Dispatchers.Main) { startDownload(context, apkUrl) }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startDownload(context: Context, url: String) {
        Toast.makeText(context, "Pobieranie nowej wersji...", Toast.LENGTH_SHORT).show()
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Aktualizacja")
            .setDescription("Pobieranie pliku...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "TestyPrawoJazdy_Update.apk")

        val downloadId = dm.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    val fileUri = dm.getUriForDownloadedFile(downloadId) ?: return
                    val install = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    ctx.startActivity(install)
                    try { ctx.unregisterReceiver(this) } catch (_: Exception) {}
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }
}
