package com.rperez.gpssurface.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

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
    private var _pathList = mutableStateListOf<Int>()
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

        _pathList.clear()
        _pathResult.value = searchDepthListing(graph, start, end)

        if (_pathResult.value.first.isNotEmpty()) {
            val resList = _pathResult.value.first.toCharArray()
            resList.forEach {
                _pathList.add(labels.indexOf(it.toString()))
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
