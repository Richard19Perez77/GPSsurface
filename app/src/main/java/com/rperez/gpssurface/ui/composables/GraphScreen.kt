package com.rperez.gpssurface.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rperez.gpssurface.data.MapData
import com.rperez.gpssurface.viewmodel.GraphViewModel

@Composable
fun GraphScreen(
    viewModel: GraphViewModel = viewModel(),
) {
    val state = viewModel.uiState
    val path = state.path
    val pathLabel = path?.nodes?.joinToString(" -> ") { MapData.labels[it] }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    when {
                        pathLabel != null -> pathLabel
                        state.selected.size == 2 -> "No path"
                        else -> "Tap two nodes"
                    }
                )
                Text(if (path != null) "Cost ${path.cost}" else " ")
            }
            TextButton(onClick = viewModel::clear) {
                Text("Clear")
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .onSizeChanged { viewModel.onCanvasSize(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = viewModel::onTap,
                        onDoubleTap = { viewModel.clear() },
                        onLongPress = { viewModel.scramble() },
                    )
                }
        ) {
            if (state.positions.isEmpty()) return@Canvas

            MapData.edges.forEach { edge ->
                val from = state.positions[edge.from]
                val to = state.positions[edge.to]
                drawLine(Color.Black, from, to)
                drawContext.canvas.nativeCanvas.drawText(
                    edge.weight.toString(),
                    (from.x + to.x) / 2f,
                    (from.y + to.y) / 2f,
                    Paint().apply {
                        color = Color.DarkGray.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    },
                )
            }

            val pathNodes = state.path?.nodes.orEmpty()
            pathNodes.zipWithNext().forEach { (a, b) ->
                drawLine(
                    color = Color.Magenta,
                    start = state.positions[a],
                    end = state.positions[b],
                    strokeWidth = 6f,
                )
            }

            state.positions.forEachIndexed { index, pos ->
                val color = if (index in state.selected) Color.Green else Color.Red
                drawCircle(color, radius = 8f, center = pos)
                drawContext.canvas.nativeCanvas.drawText(
                    MapData.labels[index],
                    pos.x,
                    pos.y - 16f,
                    Paint().apply {
                        this.color = color.toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    },
                )
            }
        }
    }
}
