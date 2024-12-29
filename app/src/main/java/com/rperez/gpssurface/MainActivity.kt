package com.rperez.gpssurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rperez.gpssurface.ui.composables.Map
import com.rperez.gpssurface.ui.theme.GPSsurfaceTheme

/**
 * The main activity for the GPS Surface application.
 *
 * This activity sets up the app's user interface using Jetpack Compose.
 * It utilizes a Material3 theme and a `Scaffold` layout to structure the UI.
 */
class MainActivity: ComponentActivity() {

    /**
     * Called when the activity is first created.
     *
     * This method sets up the content view, enables edge-to-edge drawing,
     * and applies the app's theme. The `Map` composable is displayed as the
     * main content within a `Scaffold`.
     *
     * @param savedInstanceState A `Bundle` containing the activity's previously
     * saved state, or null if there is no saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge rendering for the activity
        enableEdgeToEdge()

        // Set the content view using Jetpack Compose
        setContent {
            // Apply the app's theme
            GPSsurfaceTheme {
                // Use a Scaffold to structure the UI
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Define a surface that respects the Scaffold's inner padding
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        // Render the Map composable
                        Map()
                    }
                }
            }
        }
    }
}
