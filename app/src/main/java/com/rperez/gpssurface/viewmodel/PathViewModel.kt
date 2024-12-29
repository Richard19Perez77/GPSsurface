package com.rperez.gpssurface.viewmodel

import androidx.lifecycle.ViewModel

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