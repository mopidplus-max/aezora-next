package com.aezora.next

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.aezora.next.ui.AezoraNavHost
import com.aezora.next.ui.screens.player.PlayerViewModel
import com.aezora.next.ui.theme.AezoraNextTheme
import com.aezora.next.ui.theme.AezoraTheme

class MainActivity : ComponentActivity() {

    private val playerVm: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var currentTheme by remember { mutableStateOf(AezoraTheme.BLUE_VIOLET) }

            AezoraNextTheme(theme = currentTheme) {
                AezoraNavHost(
                    playerVm = playerVm,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
