package com.rperez.gpssurface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
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

class GPSViewModel() : ViewModel() {

    var h = 0.0f
    var w = 0.0f

    val randomPoints = List(10) {
        Pair(
            (-90..90).random().toDouble(),
            (-180..180).random().toDouble()
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

@Composable
fun Map() {
    val colors = remember { mutableStateListOf(*Array(10) { Color.Red }) }
    var gpsViewModel = GPSViewModel()
    val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")
    var pointsSelected = mutableListOf<Int>()
    var maxSelectable = 2

    val graph = arrayOf<List<Pair<Int, Int>>>(
        listOf(
            Pair(1, 1),
            Pair(2, 2),
            Pair(3, 3),
            Pair(4, 4),
            Pair(5, 5),
            Pair(6, 6),
            Pair(7, 7),
            Pair(8, 8),
            Pair(9, 9),
        )
    )

//    val graph = arrayOf<List<Pair<Int, Int>>>(
//        listOf(Pair(1, 1), Pair(3, 3)), // a -> b 1 a to d 3
//        listOf(Pair(1, 2)), // b -> c 2
//        listOf(Pair(1, 3), Pair(3, 4)), // c -> d 3 c to f 3
//        listOf(Pair(1, 4)), // d -> e 4
//        listOf(Pair(1, 5), Pair(3, 5)), // e -> f 5 e to h 3
//        listOf(Pair(1, 6)), // f -> g 6
//        listOf(Pair(1, 7), Pair(3, 6)), // g -> h 7 g to i 3
//        listOf(Pair(1, 8)), // h -> i 8
//        listOf(Pair(1, 9)), // i -> j 9
//        emptyList()
//    )

    fun isPointNear(point: Offset, target: Offset, radius: Float): Boolean {
        val dx = point.x - target.x
        val dy = point.y - target.y
        return dx * dx + dy * dy <= radius * radius
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    gpsViewModel.screenPoints.forEachIndexed { index, (screenX, screenY) ->
                        if ((isPointNear(
                                offset,
                                Offset(screenX.toFloat(), screenY.toFloat()),
                                50f
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
                            }
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        gpsViewModel.setHW(size.height, size.width)
        gpsViewModel.generateScreenPoints()
        drawRect(
            color = Color.Blue,
            topLeft = Offset(0f, 0f),
            size = Size(size.width.toFloat(), size.height.toFloat())
        )
        var index = 0
        gpsViewModel.screenPoints.forEach { (screenX, screenY) ->
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    labels[index],
                    screenX.toFloat(),
                    screenY.toFloat(),
                    android.graphics.Paint().apply {
                        color = colors[index].toArgb()
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
            index++
        }
        graph.forEachIndexed { root, value ->
            var rootPoint = gpsViewModel.screenPoints[root]
            var pointAx = rootPoint.first
            var pointAy = rootPoint.second
            value.forEach { (dest, dist) ->
                var destPoint = gpsViewModel.screenPoints[root + dest]
                var itemX = destPoint.first
                var itemY = destPoint.second
                drawContext.canvas.nativeCanvas.apply {
                    drawLine(
                        color = Color.Black,
                        start = Offset(pointAx.toFloat(), pointAy.toFloat()),
                        end = Offset(itemX.toFloat(), itemY.toFloat()),
                        cap = Stroke.DefaultCap,
                    )

                    drawText(
                        dist.toString(),
                        abs((pointAx.toFloat() + itemX.toFloat()) / 2),
                        abs((pointAy.toFloat() + itemY.toFloat()) / 2),
                        android.graphics.Paint().apply {
                            color = Color.LightGray.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}