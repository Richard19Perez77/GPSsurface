package com.rperez.gpssurface

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.ViewModel
import com.rperez.gpssurface.ui.theme.GPSsurfaceTheme
import kotlin.math.abs

/**
 * The main activity for the GPS Surface application.
 *
 * This activity sets up the app's user interface using Jetpack Compose.
 * It utilizes a Material3 theme and a `Scaffold` layout to structure the UI.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     *
     * This method sets up the content view, enables edge-to-edge drawing,
     * and applies the app's theme. The `Map` composable is displayed as the
     * main content within a `Scaffold`.
     *
     * @param savedInstanceState A `Bundle` containing the activity's previously
     * saved state, or null if there is no saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view using Jetpack Compose
        setContent {
            // Apply the app's theme
            GPSsurfaceTheme {
                // Use a Scaffold to structure the UI
                val pathViewModel = PathViewModel()
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        ShowPath(pathViewModel.pathResult)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Map(
                            pathViewModel.pathList, pathViewModel::updatePathList,
                            pathViewModel::clearPathResult
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShowPath(
    resultPath: MutableState<Pair<String, Int>>
) {

    var pathResult = remember { resultPath }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(pathResult.value.first.toCharArray().joinToString(" -> "))
        Text("${pathResult.value.second}")
    }
}

class PointsSelectedViewModel : ViewModel() {
    private val _pointsSelected = mutableStateOf(mutableListOf<Int>())
    val pointsSelected: MutableState<MutableList<Int>> get() = _pointsSelected
}

/**
 * Represents the data structure used for mapping labels to a graph with edges and weights.
 *
 * This class defines a simple graph representation using an adjacency list, where each node
 * is connected to other nodes with associated weights.
 */
class MapData {

    /**
     * The maximum number of nodes that can be selected.
     */
    val maxSelectable = 2

    /**
     * A list of labels representing nodes in the graph.
     */
    val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")

    /**
     * The adjacency list representing the graph. Each element in the array corresponds to a node,
     * and contains a list of pairs. Each pair represents:
     * - The weight of the edge.
     * - The index of the destination node.
     *
     * Example:
     * - `graph[0] = listOf(Pair(1, 1), Pair(3, 3))` indicates:
     *   - Node "A" is connected to node "B" with weight 1.
     *   - Node "A" is connected to node "D" with weight 3.
     */
    val graph = arrayOf<List<Pair<Int, Int>>>(
        listOf(Pair(1, 1), Pair(3, 3)), // A -> B (1), A -> D (3)
        listOf(Pair(1, 2)), // B -> C (2)
        listOf(Pair(1, 3), Pair(3, 4)), // C -> D (3), C -> F (3)
        listOf(Pair(1, 4)), // D -> E (4)
        listOf(Pair(1, 5), Pair(3, 5)), // E -> F (5), E -> H (3)
        listOf(Pair(1, 6)), // F -> G (6)
        listOf(Pair(1, 7), Pair(3, 6)), // G -> H (7), G -> I (3)
        listOf(Pair(1, 8)), // H -> I (8)
        listOf(Pair(1, 9)), // I -> J (9)
        emptyList() // J has no outgoing edges
    )
}


/**
 * A composable function that renders an interactive map, displaying points,
 * paths, and allowing for user interaction to select points and refresh data.
 *
 * This function integrates various utilities and view models to handle
 * point generation, pathfinding, and rendering using a Compose `Canvas`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Map(
    pathList: MutableState<MutableList<Int>>,
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
                pathList.value = mutableListOf<Int>()
                pointSelectedViewModel.pointsSelected.value = mutableListOf<Int>()
            }, onLongPress = {
                // Refresh random points on long press
                screenPointsViewModel.refreshRandomPoints()
                colors.forEachIndexed { index, color ->
                    colors[index] = Color.Red
                }

                pathList.value = mutableListOf<Int>()
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
                                pathList.value = mutableListOf()
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
        pathList.value.forEachIndexed { index, value ->
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


/**
 * ViewModel for handling pathfinding logic in a weighted graph.
 *
 * This ViewModel provides functionality for finding and updating the shortest path
 * between two nodes using a depth-first search approach.
 */
class PathViewModel : ViewModel() {

    /**
     * Tracks the minimum depth (or cost) of the shortest path found.
     */
    var minDepth = Int.MAX_VALUE

    /**
     * Stores the indices of the nodes in the shortest path found.
     */
    private var _pathList = mutableStateOf(mutableListOf<Int>())
    var pathList = _pathList

    private var _pathResult = mutableStateOf<Pair<String, Int>>(Pair("", 0))
    var pathResult = _pathResult

    /**
     * Updates the `pathList` by finding the shortest path between two nodes in the graph.
     *
     * @param graph The graph represented as an adjacency list, where each node points to
     * a list of pairs (edge weight and destination node index).
     * @param start The starting node index.
     * @param end The target node index.
     * @param labels A list of labels representing the nodes.
     */
    fun updatePathList(
        graph: Array<List<Pair<Int, Int>>>, start: Int, end: Int, labels: List<String>
    ) {
        minDepth = Int.MAX_VALUE

        _pathList.value = mutableListOf<Int>()
        _pathResult.value = searchDepthListing(graph, start, end)

        if (_pathResult.value.first.isNotEmpty()) {
            val resList = _pathResult.value.first.toCharArray()
            resList.forEach {
                _pathList.value.add(labels.indexOf(it.toString()))
            }
        }
    }

    fun clearPathResult() {
        _pathResult.value = Pair("", 0)
    }

    /**
     * Performs a depth-first search to find the shortest path between two nodes in the graph.
     *
     * @param lists The graph represented as an adjacency list.
     * @param start The starting node index.
     * @param end The target node index.
     * @return A pair where:
     * - The first element is a string representing the path.
     * - The second element is the total weight (or cost) of the path.
     */
    private fun searchDepthListing(
        lists: Array<List<Pair<Int, Int>>>, start: Int, end: Int
    ): Pair<String, Int> {
        var result: Pair<String, Int> = Pair("${'A' + start}", 0)

        /**
         * Recursive function to build paths and update the shortest path found.
         *
         * @param start The current node index.
         * @param end The target node index.
         * @param currentPair A pair containing the current path string and its total weight.
         */
        fun buildPathPair(
            start: Int, end: Int, currentPair: Pair<String, Int>
        ) {
            val pairs = lists[start]
            pairs.forEach {
                val nextChar = Char(currentPair.first.last().code + it.first)
                val nextPair =
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


/**
 * ViewModel for managing random geographic points and mapping them to screen coordinates.
 *
 * This ViewModel provides functionality to generate random geographic points,
 * convert them to screen coordinates, and manage the display dimensions.
 */
class ScreenPointsViewModel : ViewModel() {

    /**
     * The height of the screen in pixels.
     */
    var h = 0.0f

    /**
     * The width of the screen in pixels.
     */
    var w = 0.0f

    /**
     * A list of random geographic points represented as latitude and longitude pairs.
     * Latitude ranges from -90 to 90, and longitude ranges from -180 to 180.
     */
    var randomPoints = List(10) {
        Pair(
            (-90..90).random().toDouble(), (-180..180).random().toDouble()
        )
    }

    /**
     * A mutable list of points mapped to screen coordinates.
     * Each point is represented as a pair of x and y coordinates.
     */
    private val _screenPoints = mutableStateOf(mutableListOf<Pair<Double, Double>>())
    val screenPoints: MutableState<MutableList<Pair<Double, Double>>> = _screenPoints

    /**
     * Sets the height and width of the screen.
     *
     * @param h The height of the screen in pixels.
     * @param w The width of the screen in pixels.
     */
    fun setHW(h: Float, w: Float) {
        this.h = h
        this.w = w
    }

    /**
     * Converts geographic coordinates (latitude and longitude) to screen coordinates.
     *
     * @param latitude The latitude value (-90 to 90).
     * @param longitude The longitude value (-180 to 180).
     * @return A pair of screen coordinates (x, y) based on the current screen dimensions.
     */
    fun getScreenXY(latitude: Double, longitude: Double): Pair<Double, Double> {
        val x = ((longitude + 180) / 360) * w
        val y = ((90 - latitude) / 180) * h
        return Pair(x, y)
    }

    /**
     * Populates the `screenPoints` list by converting all `randomPoints` to screen coordinates.
     */
    fun generateScreenPoints() {
        _screenPoints.value = mutableListOf()
        randomPoints.forEach {
            _screenPoints.value.add(getScreenXY(it.first, it.second))
        }
    }

    /**
     * Refreshes the list of random geographic points and updates their screen coordinates.
     *
     * This method generates a new list of random latitude and longitude values and
     * maps them to screen coordinates.
     */
    fun refreshRandomPoints() {
        randomPoints = List(10) {
            Pair(
                (-90..90).random().toDouble(), (-180..180).random().toDouble()
            )
        }
        generateScreenPoints()
    }
}


/**
 * Utility class for performing operations related to points in a 2D space.
 */
class PointsUtil {

    /**
     * Checks whether a given point is within a specified radius of a target point.
     *
     * The distance is calculated using the Euclidean distance formula.
     *
     * @param point The point to check.
     * @param target The target point to compare against.
     * @param radius The radius within which the point is considered "near" the target.
     * @return `true` if the point is within the specified radius of the target, otherwise `false`.
     */
    fun isPointNear(point: Offset, target: Offset, radius: Float): Boolean {
        val dx = point.x - target.x
        val dy = point.y - target.y
        return dx * dx + dy * dy <= radius * radius
    }
}
