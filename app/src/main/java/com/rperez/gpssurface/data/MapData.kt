package com.rperez.gpssurface.data

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
