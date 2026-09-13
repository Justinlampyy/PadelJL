package com.example.padeljl.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.padeljl.presentation.theme.PadelJLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Zorg dat het scherm aan blijft tijdens de wedstrijd
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            PadelJLTheme {
                PadelScoreScreen()
            }
        }
    }
}
