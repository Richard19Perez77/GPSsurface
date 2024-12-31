package com.rperez.gpssurface.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PathDetails(
    resultPath: MutableState<Pair<String, Int>>
) {

    var pathResult = remember { resultPath }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        var pathString = if (pathResult.value.first.isNotEmpty()) {
            pathResult.value.first.toCharArray().joinToString(" -> ")
        } else {
            "No two points for a path selected"
        }

        var resultString = if (pathResult.value.second > 0) {
            "${pathResult.value.second}"
        } else {
            "No distance calculated yet"
        }

        Text(pathString)
        Text(resultString)
    }
}
