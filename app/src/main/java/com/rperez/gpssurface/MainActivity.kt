package com.rperez.gpssurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rperez.gpssurface.ui.composables.GraphScreen
import com.rperez.gpssurface.ui.theme.GPSsurfaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSsurfaceTheme {
                GraphScreen()
            }
        }
    }
}
