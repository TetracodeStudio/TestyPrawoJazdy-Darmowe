package com.testy.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        val bufferedImage = ImageIO.read(ByteArrayInputStream(bytes))
        bufferedImage?.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
