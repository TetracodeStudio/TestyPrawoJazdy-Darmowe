package com.testy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.testy.data.AndroidContextProvider
import com.testy.ui.App
import com.testy.ui.picker.FilePicker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextProvider.init(this)
        FilePicker.register(this)

        setContent {
            App()
        }
    }
}
