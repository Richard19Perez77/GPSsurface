package com.rperez.gpssurface.data

class MapData {
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
}
