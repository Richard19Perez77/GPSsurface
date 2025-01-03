package com.rperez.gpssurface.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rperez.gpssurface.viewmodel.PathViewModel

@Composable
fun SurfaceMap() {
    val pathViewModel = PathViewModel()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.LightGray)
        ) {
            PathDetails(pathViewModel.pathResult)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(3f)
        ) {
            PointsMap(
                pathViewModel.pathList,
                pathViewModel::updatePathList,
                pathViewModel::clearPathResult
            )
        }
    }
}