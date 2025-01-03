package com.rperez.gpssurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rperez.gpssurface.ui.composables.PathDetails
import com.rperez.gpssurface.ui.composables.PointsMap
import com.rperez.gpssurface.ui.composables.SurfaceMap
import com.rperez.gpssurface.ui.theme.GPSsurfaceTheme
import com.rperez.gpssurface.viewmodel.PathViewModel

/**
 * The main activity for the GPS Surface application.
 *
 * This activity sets up the app's user interface using Jetpack Compose.
 * It utilizes a Material3 theme and a `Scaffold` layout to structure the UI.
 */
class MainActivity : ComponentActivity() {

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
        // Set the content view using Jetpack Compose
        setContent {
            // Apply the app's theme
            GPSsurfaceTheme {
                SurfaceMap()
            }
        }
    }
}