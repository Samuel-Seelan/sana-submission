package com.sana.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import com.sana.app.ui.theme.SanaTheme

/*
 * MainActivity.kt — the single activity that hosts the Compose UI.
 * What: applies the Sana theme and shows the navigation graph.
 * Who: Sana team (shared infrastructure).
 * When: Goal 6 — UI skeleton.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                }
            }
        }
    }
}
