package com.lokishaw.acerremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.lokishaw.acerremote.ui.RemoteScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var mode by rememberSaveable { mutableStateOf(ConnectionMode.WIFI) }
            val controller = remember(mode) { LoggingController(mode) }
            RemoteScreen(
                mode = mode,
                onModeChange = { mode = it },
                onAction = controller::send,
            )
        }
    }
}
