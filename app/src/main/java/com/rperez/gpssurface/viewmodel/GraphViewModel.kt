package com.rperez.gpssurface.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.rperez.gpssurface.data.MapData
import com.rperez.gpssurface.graph.GraphPath
import com.rperez.gpssurface.graph.findPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GraphUiState(
    val positions: List<Offset> = emptyList(),
    val selected: List<Int> = emptyList(),
    val path: GraphPath? = null,
)

class GraphViewModel : ViewModel() {

    var uiState by mutableStateOf(GraphUiState())
        private set

    private var canvasWidth = 0f
    private var canvasHeight = 0f

    fun onCanvasSize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val sizeChanged = width != canvasWidth || height != canvasHeight
        canvasWidth = width
        canvasHeight = height
        if (uiState.positions.isEmpty() || sizeChanged) {
            uiState = uiState.copy(positions = circleLayout(width, height))
        }
    }

    fun onTap(tap: Offset) {
        val index = hitIndex(tap) ?: return
        if (index in uiState.selected) return

        val selected = if (uiState.selected.size >= 2) listOf(index) else uiState.selected + index
        val path = if (selected.size == 2) {
            findPath(MapData.edges, selected[0], selected[1])
        } else {
            null
        }
        uiState = uiState.copy(selected = selected, path = path)
    }

    fun clear() {
        uiState = uiState.copy(selected = emptyList(), path = null)
    }

    fun scramble() {
        if (canvasWidth <= 0f || canvasHeight <= 0f) return
        uiState = GraphUiState(positions = randomLayout(canvasWidth, canvasHeight))
    }

    private fun hitIndex(tap: Offset): Int? {
        val radiusSq = HIT_RADIUS * HIT_RADIUS
        return uiState.positions.indices.firstOrNull { i ->
            val pos = uiState.positions[i]
            val dx = tap.x - pos.x
            val dy = tap.y - pos.y
            dx * dx + dy * dy <= radiusSq
        }
    }

    private fun circleLayout(width: Float, height: Float): List<Offset> {
        val cx = width / 2f
        val cy = height / 2f
        val rx = width * 0.38f
        val ry = height * 0.38f
        return List(MapData.labels.size) { i ->
            val angle = 2.0 * PI * i / MapData.labels.size - PI / 2.0
            Offset(
                cx + rx * cos(angle).toFloat(),
                cy + ry * sin(angle).toFloat(),
            )
        }
    }

    private fun randomLayout(width: Float, height: Float): List<Offset> {
        val pad = 48f
        return List(MapData.labels.size) {
            Offset(
                Random.nextFloat() * (width - 2 * pad) + pad,
                Random.nextFloat() * (height - 2 * pad) + pad,
            )
        }
    }

    companion object {
        const val HIT_RADIUS = 50f
    }
}
