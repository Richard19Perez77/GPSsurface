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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Map() {
    val pointsViewModel = PointsViewModel()
    val pathViewModel = PathViewModel()

    val pointsSelected = remember { mutableStateListOf<Int>() }
    val pathList = remember { pathViewModel.pathList }
    var colors = remember { MutableList(10) { Color.Companion.Red } }
    var globalCoordsSet = remember { false }

    var mapData = MapData()
    var pointsUtil = PointsUtil()

    Canvas(modifier = Modifier.Companion
        .fillMaxSize()
        .onGloballyPositioned {
            if (!globalCoordsSet) {
                pointsViewModel.setHW(
                    it.size.height.toFloat(),
                    it.size.width.toFloat()
                )
                pointsViewModel.generateScreenPoints()
                globalCoordsSet = true
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    colors = MutableList(10) { Color.Companion.Red }
                    pathList.clear()
                    pointsSelected.clear()
                },
                onLongPress = {
                    pointsViewModel.refreshRandomPoints()
                    colors = MutableList(10) { Color.Companion.Red }
                    pathList.clear()
                    pointsSelected.clear()
                },
                onTap = { offset ->
                    pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
                        if ((pointsUtil.isPointNear(
                                offset, Offset(screenX.toFloat(), screenY.toFloat()), 50f
                            ))
                        ) {
                            if (!pointsSelected.contains(index)) {
                                if (pointsSelected.size == mapData.maxSelectable) {
                                    pointsSelected.forEach {
                                        colors[it] = Color.Companion.Red
                                    }
                                    pointsSelected.clear()
                                }

                                pointsSelected.add(index)
                                colors[index] = Color.Companion.Green
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
        drawRect(
            color = Color.Companion.Blue,
            topLeft = Offset(0f, 0f),
            size = Size(size.width.toFloat(), size.height.toFloat())
        )
        pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
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
        mapData.graph.forEachIndexed { root, value ->
            var rootPoint = pointsViewModel.screenPoints[root]
            var pointAx = rootPoint.first
            var pointAy = rootPoint.second
            value.forEach { (dest, dist) ->
                var destPoint = pointsViewModel.screenPoints[root + dest]
                var itemX = destPoint.first
                var itemY = destPoint.second

                drawLine(
                    color = Color.Companion.Black,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat()),
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(dist.toString(),
                        abs((pointAx.toFloat() + itemX.toFloat()) / 2),
                        abs((pointAy.toFloat() + itemY.toFloat()) / 2),
                        Paint().apply {
                            color = Color.Companion.LightGray.toArgb()
                            textSize = 30f
                            textAlign = Paint.Align.CENTER
                        })
                }
            }
        }

        if (pointsSelected.size == mapData.maxSelectable) {
            pointsSelected.sort()
            pathViewModel.minDepth = Int.MAX_VALUE
            pathViewModel.updatePathList(
                mapData.graph, pointsSelected[0], pointsSelected[1], mapData.labels
            )
        }
        lateinit var prev: Pair<Double, Double>
        lateinit var current: Pair<Double, Double>
        pathList.forEachIndexed { index, value ->
            if (index == 0) {
                prev = pointsViewModel.screenPoints[value]
            } else {
                current = pointsViewModel.screenPoints[value]
                var pointAx = prev.first
                var pointAy = prev.second
                var itemX = current.first
                var itemY = current.second
                drawLine(
                    color = Color.Companion.Magenta,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat()),
                    strokeWidth = Stroke.Companion.DefaultMiter,
                )
                prev = current
            }
        }
    }
}