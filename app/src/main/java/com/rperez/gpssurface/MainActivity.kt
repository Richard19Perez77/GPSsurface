package com.rperez.gpssurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.ViewModel
import com.rperez.gpssurface.ui.theme.GPSsurfaceTheme
import kotlin.arrayOf
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPSsurfaceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        Map()
                    }
                }
            }
        }
    }
}

class PathViewModel : ViewModel() {
    var minDepth = Int.MAX_VALUE
    var pathList = mutableListOf<Int>()

    fun updatePathList(
        graph: Array<List<Pair<Int, Int>>>, start: Int, end: Int, labels: List<String>
    ) {
        pathList.clear()
        var resString = searchDepthListing(
            graph, start, end
        )

        if (resString.first.isNotEmpty()) {
            var resList = resString.first.toCharArray()
            resList.forEach {
                pathList.add(labels.indexOf(it.toString()))
            }
        }
    }

    private fun searchDepthListing(
        lists: Array<List<Pair<Int, Int>>>, start: Int, end: Int
    ): Pair<String, Int> {
        var result: Pair<String, Int> = Pair("${'A' + start}", 0)

        fun buildPathPair(
            start: Int, end: Int, currentPair: Pair<String, Int>
        ) {
            var pairs = lists[start]
            pairs.forEach {
                var nextChar = Char(currentPair.first.last().code + it.first)
                var nextPair =
                    Pair("${currentPair.first}${nextChar}", it.second + currentPair.second)
                if (it.first + start == end) {
                    if (nextPair.second < minDepth) {
                        result = nextPair
                        minDepth = nextPair.second
                    }
                } else {
                    buildPathPair(start + it.first, end, nextPair)
                }
            }
        }

        lists[start].forEach {
            buildPathPair(start, end, result)
        }

        return result
    }
}

class PointsViewModel : ViewModel() {

    var h = 0.0f
    var w = 0.0f

    val randomPoints = List(10) {
        Pair(
            (-90..90).random().toDouble(), (-180..180).random().toDouble()
        )
    }

    val screenPoints = mutableListOf<Pair<Double, Double>>()

    fun setHW(h: Float, w: Float) {
        this.h = h
        this.w = w
    }

    fun getScreenXY(latitude: Double, longitude: Double): Pair<Double, Double> {
        val x = ((longitude + 180) / 360) * w
        val y = ((90 - latitude) / 180) * h
        return Pair(x, y)
    }

    fun generateScreenPoints() {
        screenPoints.clear()
        randomPoints.forEach {
            screenPoints.add(getScreenXY(it.first, it.second))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Map() {
    val pointsViewModel = PointsViewModel()
    val pathViewModel = PathViewModel()

    val pointsSelected = remember { mutableStateListOf<Int>() }
    val pathList = remember { pathViewModel.pathList }
    var colors = remember { MutableList(10) { Color.Red } }

    val maxSelectable = 2
    val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")

    val graph = arrayOf<List<Pair<Int, Int>>>(
        listOf(Pair(1, 1), Pair(3, 3)), // a -> b 1 a to d 3
        listOf(Pair(1, 2)), // b -> c 2
        listOf(Pair(1, 3), Pair(3, 4)), // c -> d 3 c to f 3
        listOf(Pair(1, 4)), // d -> e 4
        listOf(Pair(1, 5), Pair(3, 5)), // e -> f 5 e to h 3
        listOf(Pair(1, 6)), // f -> g 6
        listOf(Pair(1, 7), Pair(3, 6)), // g -> h 7 g to i 3
        listOf(Pair(1, 8)), // h -> i 8
        listOf(Pair(1, 9)), // i -> j 9
        emptyList()
    )

    fun isPointNear(point: Offset, target: Offset, radius: Float): Boolean {
        val dx = point.x - target.x
        val dy = point.y - target.y
        return dx * dx + dy * dy <= radius * radius
    }
    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    colors = MutableList(10) { Color.Red }
                    pathList.clear()
                    pointsViewModel.screenPoints.clear()
                    pointsSelected.clear()
                },
                onTap = { offset ->
                    pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
                        if ((isPointNear(
                                offset, Offset(screenX.toFloat(), screenY.toFloat()), 50f
                            ))
                        ) {
                            if (!pointsSelected.contains(index)) {
                                if (pointsSelected.size == maxSelectable) {
                                    pointsSelected.forEach {
                                        colors[it] = Color.Red
                                    }
                                    pointsSelected.clear()
                                }

                                pointsSelected.add(index)
                                colors[index] = Color.Green
                                if (pointsSelected.size < maxSelectable) {
                                    pathList.clear()
                                }
                            }
                            return@detectTapGestures
                        }
                    }
                })
        }
    ) {
        pointsViewModel.setHW(size.height, size.width)
        pointsViewModel.generateScreenPoints()
        drawRect(
            color = Color.Blue,
            topLeft = Offset(0f, 0f),
            size = Size(size.width.toFloat(), size.height.toFloat())
        )
        pointsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(labels[index],
                    screenX.toFloat(),
                    screenY.toFloat(),
                    android.graphics.Paint().apply {
                        color = colors[index].toArgb()
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    })
            }
        }
        graph.forEachIndexed { root, value ->
            var rootPoint = pointsViewModel.screenPoints[root]
            var pointAx = rootPoint.first
            var pointAy = rootPoint.second
            value.forEach { (dest, dist) ->
                var destPoint = pointsViewModel.screenPoints[root + dest]
                var itemX = destPoint.first
                var itemY = destPoint.second

                drawLine(
                    color = Color.Black,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat()),
                )

                drawContext.canvas.nativeCanvas.apply {
                    drawText(dist.toString(),
                        abs((pointAx.toFloat() + itemX.toFloat()) / 2),
                        abs((pointAy.toFloat() + itemY.toFloat()) / 2),
                        android.graphics.Paint().apply {
                            color = Color.LightGray.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        })
                }
            }
        }

        if (pointsSelected.size == maxSelectable) {
            pointsSelected.sort()
            pathViewModel.minDepth = Int.MAX_VALUE
            pathViewModel.updatePathList(
                graph, pointsSelected[0], pointsSelected[1], labels
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
                    color = Color.Magenta,
                    start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                    end = Offset(itemX.toFloat(), itemY.toFloat()),
                    strokeWidth = Stroke.DefaultMiter,
                )
                prev = current
            }
        }
    }
}