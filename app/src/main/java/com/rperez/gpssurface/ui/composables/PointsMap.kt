package com.rperez.gpssurface.ui.composables

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import com.rperez.gpssurface.data.MapData
import com.rperez.gpssurface.util.PointsUtil
import com.rperez.gpssurface.viewmodel.PointsSelectedViewModel
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
fun PointsMap(
    pathList: SnapshotStateList<Int>,
    updatePathResult: (Array<List<Pair<Int, Int>>>, Int, Int, List<String>) -> Unit,
    clearPathResult: () -> Unit
) {

    // ViewModels for managing points and pathfinding logic
    val screenPointsViewModel = ScreenPointsViewModel()
    val pointSelectedViewModel = PointsSelectedViewModel()

    var screenPoints = remember { screenPointsViewModel.screenPoints }
    var pointsSelected = remember { pointSelectedViewModel.pointsSelected }
    var pathList = remember { pathList }

    var colors = remember {
        mutableStateListOf<Color>(
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red,
            Color.Red
        )
    }

    // Data and utility instances
    val mapData = MapData()
    val pointsUtil = PointsUtil()

    // Canvas for rendering the map
    Canvas(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged {
//                if (!globalCoordsSet) {
//                    screenPointsViewModel.initPoints(
//                        it.height.toFloat(),
//                        it.width.toFloat()
//                    )
//                    globalCoordsSet = true
//                }
        }
        .onPlaced {
//                if (!globalCoordsSet) {
//                    screenPointsViewModel.initPoints(
//                        it.size.height.toFloat(),
//                        it.size.width.toFloat()
//                    )
//                    globalCoordsSet = true
//                }
        }
        .onGloballyPositioned {
            // create screen points based on available canvas size
            screenPointsViewModel.setHW(
                it.size.height.toFloat(), it.size.width.toFloat()
            )
        }
        .pointerInput(Unit) {
            detectTapGestures(onDoubleTap = {
                // Reset state on double tap
                colors.forEachIndexed { index, color ->
                    colors[index] = Color.Red
                }
                pathList.clear()
                pointSelectedViewModel.pointsSelected.value = mutableListOf<Int>()
            }, onLongPress = {
                // Refresh random points on long press
                screenPointsViewModel.refreshRandomPoints()
                colors.forEachIndexed { index, color ->
                    colors[index] = Color.Red
                }

                pathList.clear()
                pointsSelected.value = mutableListOf<Int>()
            }, onTap = { offset ->
                // Handle single tap for point selection
                screenPoints.value.forEachIndexed { index, (screenX, screenY) ->
                    if (pointsUtil.isPointNear(
                            offset, Offset(screenX.toFloat(), screenY.toFloat()), 50f
                        )
                    ) {
                        if (!pointsSelected.value.contains(index)) {
                            if (pointsSelected.value.size == mapData.maxSelectable) {
                                pointsSelected.value.forEach {
                                    colors[it] = Color.Red
                                }
                                pointsSelected.value = mutableListOf()
                            }
                            pointsSelected.value.add(index)
                            colors[index] = Color.Green
                            if (pointsSelected.value.size < mapData.maxSelectable) {
                                pathList.clear()
                            }
                        }
                        return@detectTapGestures
                    }
                }
            })
        }) {

        screenPointsViewModel.generateScreenPoints()

        // Draw background
        drawRect(
            color = Color.Blue,
            topLeft = Offset(0f, 0f),
            size = Size(size.width.toFloat(), size.height.toFloat())
        )

        // Draw points with labels
        screenPoints.value.forEachIndexed { index, (screenX, screenY) ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(mapData.labels[index],
                    screenX.toFloat(),
                    screenY.toFloat(),
                    Paint().apply {
                        color = colors[index].toArgb()
                        textSize = 30f
                        textAlign = Paint.Align.CENTER
                    })
            }
        }

        // Draw edges of the graph
        mapData.graph.forEachIndexed { root, value ->
            val rootPoint = screenPoints.value[root]
            val pointAx = rootPoint.first
            val pointAy = rootPoint.second

            value.forEach { (dest, dist) ->
                val destPoint = screenPoints.value[root + dest]
                val itemX = destPoint.first
                val itemY = destPoint.second

                drawLine(
                    color = Color.Black,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat())
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(dist.toString(),
                        abs((pointAx.toFloat() + itemX.toFloat()) / 2),
                        abs((pointAy.toFloat() + itemY.toFloat()) / 2),
                        Paint().apply {
                            color = Color.LightGray.toArgb()
                            textSize = 30f
                            textAlign = Paint.Align.CENTER
                        })
                }
            }
        }

        // Update and draw the shortest path if two points are selected
        if (pointsSelected.value.size == mapData.maxSelectable) {
            pointsSelected.value.sort()
            updatePathResult(
                mapData.graph, pointsSelected.value[0], pointsSelected.value[1], mapData.labels
            )
        } else {
            clearPathResult()
        }

        // Draw the path
        lateinit var prev: Pair<Double, Double>
        lateinit var current: Pair<Double, Double>
        pathList.forEachIndexed { index, value ->
            if (index == 0) {
                prev = screenPoints.value[value]
            } else {
                current = screenPoints.value[value]
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
