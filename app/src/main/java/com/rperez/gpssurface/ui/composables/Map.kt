package com.rperez.gpssurface.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.rperez.gpssurface.data.MapData
import com.rperez.gpssurface.util.PointsUtil
import com.rperez.gpssurface.viewmodel.PathViewModel
import com.rperez.gpssurface.viewmodel.PointsViewModel
import kotlin.math.abs

/**
 * A composable function that renders an interactive map, displaying points,
 * paths, and allowing for user interaction to select points and refresh data.
 *
 * This function integrates various utilities and view models to handle
 * point generation, pathfinding, and rendering using a Compose `Canvas`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Map() {
    // ViewModels for managing points and pathfinding logic
    val pointsViewModel = PointsViewModel()
    val pathViewModel = PathViewModel()

    // State to track selected points and their colors
    val pointsSelected = remember { mutableStateListOf<Int>() }
    val pathList = remember { pathViewModel.pathList }
    var colors = remember { MutableList(10) { Color.Red } }
    var globalCoordsSet = remember { false }

    // Data and utility instances
    val mapData = MapData()
    val pointsUtil = PointsUtil()

    // Canvas for rendering the map
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                if (!globalCoordsSet) {
                    pointsViewModel.setHW(it.size.height.toFloat(), it.size.width.toFloat())
                    pointsViewModel.generateScreenPoints()
                    globalCoordsSet = true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset state on double tap
                        colors = MutableList(10) { Color.Red }
                        pathList.clear()
                        pointsSelected.clear()
                    },
                    onLongPress = {
                        // Refresh random points on long press
                        pointsViewModel.refreshRandomPoints()
                        colors = MutableList(10) { Color.Red }
                        pathList.clear()
                        pointsSelected.clear()
                    },
                    onTap = { offset ->
                        // Handle single tap for point selection
                        pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
                            if (pointsUtil.isPointNear(
                                    offset, Offset(screenX.toFloat(), screenY.toFloat()), 50f
                                )
                            ) {
                                if (!pointsSelected.contains(index)) {
                                    if (pointsSelected.size == mapData.maxSelectable) {
                                        pointsSelected.forEach {
                                            colors[it] = Color.Red
                                        }
                                        pointsSelected.clear()
                                    }
                                    pointsSelected.add(index)
                                    colors[index] = Color.Green
                                    if (pointsSelected.size < mapData.maxSelectable) {
                                        pathList.clear()
                                    }
                                }
                                return@detectTapGestures
                            }
                        }
                    }
                )
            }
    ) {
        // Draw background
        drawRect(
            color = Color.Blue,
            topLeft = Offset(0f, 0f),
            size = Size(size.width.toFloat(), size.height.toFloat())
        )

        // Draw points with labels
        pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    mapData.labels[index],
                    screenX.toFloat(),
                    screenY.toFloat(),
                    Paint().apply {
                        color = colors[index].toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    }
                )
            }
        }

        // Draw edges of the graph
        mapData.graph.forEachIndexed { root, value ->
            val rootPoint = pointsViewModel.screenPoints[root]
            val pointAx = rootPoint.first
            val pointAy = rootPoint.second
            value.forEach { (dest, dist) ->
                val destPoint = pointsViewModel.screenPoints[root + dest]
                val itemX = destPoint.first
                val itemY = destPoint.second

                drawLine(
                    color = Color.Black,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat())
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        dist.toString(),
                        abs((pointAx.toFloat() + itemX.toFloat()) / 2),
                        abs((pointAy.toFloat() + itemY.toFloat()) / 2),
                        Paint().apply {
                            color = Color.LightGray.toArgb()
                            textSize = 30f
                            textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // Update and draw the shortest path if two points are selected
        if (pointsSelected.size == mapData.maxSelectable) {
            pointsSelected.sort()
            pathViewModel.minDepth = Int.MAX_VALUE
            pathViewModel.updatePathList(
                mapData.graph, pointsSelected[0], pointsSelected[1], mapData.labels
            )
        }

        // Draw the path
        lateinit var prev: Pair<Double, Double>
        lateinit var current: Pair<Double, Double>
        pathList.forEachIndexed { index, value ->
            if (index == 0) {
                prev = pointsViewModel.screenPoints[value]
            } else {
                current = pointsViewModel.screenPoints[value]
                val pointAx = prev.first
                val pointAy = prev.second
                val itemX = current.first
                val itemY = current.second
                drawLine(
                    color = Color.Magenta,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat()),
                    strokeWidth = Stroke.DefaultMiter
                )
                prev = current
            }
        }
    }
}
