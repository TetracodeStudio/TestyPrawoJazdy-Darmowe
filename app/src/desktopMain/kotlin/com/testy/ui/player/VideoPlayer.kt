package com.testy.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.BorderLayout
import java.awt.Color as AwtColor
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.io.File
import javax.swing.JPanel
import javax.swing.SwingUtilities

@Composable
actual fun VideoPlayer(
    filePath: String,
    modifier: Modifier,
) {
    val file = remember(filePath) { File(filePath) }

    if (!file.exists()) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🎬 PLIK NIE ISTNIEJE: ${file.name}",
                color = Color(0xFFA5D6A7),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
        return
    }

    val isVlcAvailable = remember {
        ensureVlcInitialized()
        try {
            NativeDiscovery().discover()
        } catch (e: Exception) {
            false
        }
    }

    if (!isVlcAvailable) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "⚠️ Nie udało się zainicjować silnika odtwarzania wideo (LibVLC).",
                color = Color(0xFFFFCC80),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }
        return
    }

    var mediaPlayerComponentRef by remember { mutableStateOf<EmbeddedMediaPlayerComponent?>(null) }
    var loadedFilePath by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayerComponentRef?.let { component ->
                try {
                    component.mediaPlayer().controls().stop()
                    component.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mediaPlayerComponentRef = null
        }
    }

    SwingPanel(
        background = Color.Black,
        modifier = modifier.background(Color.Black),
        factory = {
            val container = JPanel(BorderLayout()).apply {
                background = AwtColor.BLACK
                isOpaque = true
            }

            val component = EmbeddedMediaPlayerComponent()
            component.background = AwtColor.BLACK
            val videoCanvas = component.videoSurfaceComponent()
            videoCanvas.background = AwtColor.BLACK

            mediaPlayerComponentRef = component
            container.add(component, BorderLayout.CENTER)

            val mediaPlayer = component.mediaPlayer()

            // Nasłuchiwanie na koniec odtwarzania - pauza na ostatniej klatce
            mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
                override fun finished(mediaPlayer: MediaPlayer) {
                    SwingUtilities.invokeLater {
                        try {
                            mediaPlayer.controls().pause()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })

            container
        },
        update = {
            val component = mediaPlayerComponentRef ?: return@SwingPanel
            val absolutePath = file.absolutePath

            if (loadedFilePath != absolutePath) {
                loadedFilePath = absolutePath
                val mediaPlayer = component.mediaPlayer()
                val videoCanvas = component.videoSurfaceComponent()

                fun startPlayback() {
                    try {
                        mediaPlayer.controls().stop()

                        // Wyłączenie dźwięku
                        mediaPlayer.audio().setMute(true)
                        mediaPlayer.audio().setVolume(0)

                        // Brak powtarzania
                        mediaPlayer.controls().setRepeat(false)

                        // Odtwarzanie z pauzą po zakończeniu
                        mediaPlayer.media().play(
                            absolutePath,
                            ":vout=directx,any",
                            ":file-caching=50",
                            ":live-caching=50",
                            ":network-caching=50",
                            ":no-audio",
                            ":play-and-pause"
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                if (videoCanvas.isDisplayable) {
                    SwingUtilities.invokeLater { startPlayback() }
                } else {
                    lateinit var hierarchyListener: HierarchyListener
                    hierarchyListener = HierarchyListener { e ->
                        if ((e.changeFlags and HierarchyEvent.DISPLAYABILITY_CHANGED.toLong()) != 0L) {
                            if (videoCanvas.isDisplayable) {
                                videoCanvas.removeHierarchyListener(hierarchyListener)
                                SwingUtilities.invokeLater { startPlayback() }
                            }
                        }
                    }
                    videoCanvas.addHierarchyListener(hierarchyListener)
                }
            }
        }
    )
}

/**
 * Automatyczne wyszukiwanie i konfiguracja ścieżek do biblioteki LibVLC w systemie Windows.
 */
private fun ensureVlcInitialized() {
    try {
        val jnaPath = System.getProperty("jna.library.path")
        if (jnaPath.isNullOrEmpty()) {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            if (os.contains("win")) {
                val possiblePaths = listOf(
                    "C:\\Program Files\\VideoLAN\\VLC",
                    "C:\\Program Files (x86)\\VideoLAN\\VLC",
                    System.getenv("ProgramFiles")?.let { "$it\\VideoLAN\\VLC" },
                    System.getenv("ProgramFiles(x86)")?.let { "$it\\VideoLAN\\VLC" },
                    System.getenv("LOCALAPPDATA")?.let { "$it\\Programs\\VideoLAN\\VLC" },
                ).filterNotNull()

                for (path in possiblePaths) {
                    val vlcDll = File(path, "libvlc.dll")
                    if (vlcDll.exists()) {
                        System.setProperty("jna.library.path", path)
                        System.setProperty("VLC_PLUGIN_PATH", "$path\\plugins")
                        break
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}