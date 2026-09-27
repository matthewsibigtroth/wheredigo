package com.wheredigo.hikingtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wheredigo.hikingtracker.ui.HikingViewModel
import com.wheredigo.hikingtracker.ui.screens.HikingTrackerScreen
import com.wheredigo.hikingtracker.ui.theme.WhereDiGoTheme

/**
 * Main Activity host for the WhereDiGo single-screen hiking tracker application.
 */
class MainActivity : ComponentActivity() {

    private val hikingViewModel: HikingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WhereDiGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    HikingTrackerScreen(viewModel = hikingViewModel)
                }
            }
        }
    }
}
