package com.testy

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.testy.ui.App
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() {
    System.setProperty("skiko.renderApi", "OPENGL")
    System.setProperty("skiko.direct3d.enabled", "false")

    application {
        val windowState = rememberWindowState(
            width = 800.dp,
            height = 900.dp
        )
        val icon = painterResource("icon.png")
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Testy Na Prawo Jazdy",
            icon = icon
        ) {
            window.minimumSize = Dimension(360, 640)
            App()
        }
    }
}
