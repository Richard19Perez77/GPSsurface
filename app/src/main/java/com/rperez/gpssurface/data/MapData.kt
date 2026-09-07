package com.rperez.gpssurface.data

data class Edge(val from: Int, val to: Int, val weight: Int)

object MapData {
    val labels = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")

    val edges = listOf(
        Edge(0, 1, 1),
        Edge(0, 3, 3),
        Edge(1, 2, 2),
        Edge(2, 3, 3),
        Edge(2, 5, 4),
        Edge(3, 4, 4),
        Edge(4, 5, 5),
        Edge(4, 7, 5),
        Edge(5, 6, 6),
        Edge(6, 7, 7),
        Edge(6, 8, 6),
        Edge(7, 8, 8),
        Edge(8, 9, 9),
    )
}
