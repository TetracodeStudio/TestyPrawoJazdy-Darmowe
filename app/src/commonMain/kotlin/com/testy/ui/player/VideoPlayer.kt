package com.testy.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    filePath: String,
    modifier: Modifier = Modifier
)
