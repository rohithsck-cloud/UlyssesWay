package com.example.ulyssesway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.ulyssesway.ui.MainContent
import com.example.ulyssesway.ui.LoginScreen
import com.example.ulyssesway.ui.theme.UlyssesWayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UlyssesWayTheme {
                var loggedIn by remember { mutableStateOf(false) }

                if (!loggedIn) {
                    LoginScreen(
                        onLoginClick = {
                            loggedIn = true
                        }
                    )
                } else {
                    MainContent() // <- This ensures the bottom navigation is visible
                }
            }
        }
    }
}
