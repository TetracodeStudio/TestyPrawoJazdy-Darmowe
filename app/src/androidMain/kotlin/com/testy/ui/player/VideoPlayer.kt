package com.testy.ui.player

import android.net.Uri
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

@Composable
actual fun VideoPlayer(
    filePath: String,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setMediaController(null)
                setOnTouchListener { _, _ -> true }
                setOnErrorListener { _, _, _ -> true }
                setOnPreparedListener { mp ->
                    mp.isLooping = false
                    start()
                }
            }
        },
        update = { videoView ->
            try {
                videoView.setMediaController(null)
                videoView.setOnTouchListener { _, _ -> true }
                videoView.setOnErrorListener { _, _, _ -> true }

                val currentUri = Uri.fromFile(File(filePath))
                val tagUri = videoView.tag as? Uri
                if (tagUri != currentUri) {
                    videoView.tag = currentUri
                    videoView.setVideoURI(currentUri)
                    videoView.setOnPreparedListener { mp ->
                        mp.isLooping = false
                        videoView.start()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
}
