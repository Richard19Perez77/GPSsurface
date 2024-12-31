package com.rperez.gpssurface.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.rperez.gpssurface.PointsSelectedViewModel
import com.rperez.gpssurface.data.MapData
import com.rperez.gpssurface.util.PointsUtil
import com.rperez.gpssurface.viewmodel.PathViewModel
import com.rperez.gpssurface.viewmodel.ScreenPointsViewModel
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
    val screenPointsViewModel = ScreenPointsViewModel()
    val pathViewModel = PathViewModel()
    val pointSelectedViewModel = PointsSelectedViewModel()

    var colors = MutableList(10) { Color.Red }
    var globalCoordsSet = remember { false }

    // Data and utility instances
    val mapData = MapData()
    val pointsUtil = PointsUtil()

    // Canvas for rendering the map
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                // create screen points based on available canvas size
                if (!globalCoordsSet) {
                    screenPointsViewModel.initPoints(
                        it.size.height.toFloat(),
                        it.size.width.toFloat()
                    )
                    globalCoordsSet = true
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Reset state on double tap
                        colors.forEachIndexed { index, color ->
                            colors[index] = Color.Red
                        }
                        pathViewModel.pathList.value.clear()
                        pointSelectedViewModel.pointsSelected.value.clear()
                    },
                    onLongPress = {
                        // Refresh random points on long press
                        screenPointsViewModel.refreshRandomPoints()
                        colors.forEachIndexed { index, color ->
                            colors[index] = Color.Red
                        }
                        pathViewModel.pathList.value.clear()
                        pointSelectedViewModel.pointsSelected.value.clear()
                    },
                    onTap = { offset ->
                        // Handle single tap for point selection
                        screenPointsViewModel.screenPoints.value.forEachIndexed { index, (screenX, screenY) ->
                            if (pointsUtil.isPointNear(
                                    offset, Offset(screenX.toFloat(), screenY.toFloat()), 50f
                                )
                            ) {
                                if (!pointSelectedViewModel.pointsSelected.value.toList().contains(index)) {
                                    if (pointSelectedViewModel.pointsSelected.value.size == mapData.maxSelectable) {
                                        pointSelectedViewModel.pointsSelected.value.forEach {
                                            colors[it] = Color.Red
                                        }
                                        pointSelectedViewModel.pointsSelected.value.clear()
                                    }
                                    pointSelectedViewModel.pointsSelected.value.add(index)
                                    colors[index] = Color.Green
                                    if (pointSelectedViewModel.pointsSelected.value.size < mapData.maxSelectable) {
                                        pathViewModel.pathList.value.clear()
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
        screenPointsViewModel.screenPoints.value.forEachIndexed { index, (screenX, screenY) ->
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
            val rootPoint = screenPointsViewModel.screenPoints.value[root]
            val pointAx = rootPoint.first
            val pointAy = rootPoint.second

            value.forEach { (dest, dist) ->
                val destPoint = screenPointsViewModel.screenPoints.value[root + dest]
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
        if (pointSelectedViewModel.pointsSelected.value.size == mapData.maxSelectable) {
            pointSelectedViewModel.pointsSelected.value.sort()
            pathViewModel.updatePathList(
                mapData.graph, pointSelectedViewModel.pointsSelected.value[0], pointSelectedViewModel.pointsSelected.value[1], mapData.labels
            )
        }

        // Draw the path
        lateinit var prev: Pair<Double, Double>
        lateinit var current: Pair<Double, Double>
        pathViewModel.pathList.value.forEachIndexed { index, value ->
            if (index == 0) {
                prev = screenPointsViewModel.screenPoints.value[value]
            } else {
                current = screenPointsViewModel.screenPoints.value[value]
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
