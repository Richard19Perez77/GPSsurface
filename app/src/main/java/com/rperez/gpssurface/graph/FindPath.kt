package com.rperez.gpssurface.graph

import com.rperez.gpssurface.data.Edge

data class GraphPath(val nodes: List<Int>, val cost: Int)

fun findPath(edges: List<Edge>, start: Int, end: Int): GraphPath? {
    if (start == end) return GraphPath(listOf(start), 0)

    val adj = mutableMapOf<Int, MutableList<Edge>>()
    for (edge in edges) {
        adj.getOrPut(edge.from) { mutableListOf() }.add(edge)
        adj.getOrPut(edge.to) { mutableListOf() }.add(Edge(edge.to, edge.from, edge.weight))
    }
    var best: GraphPath? = null

    fun walk(node: Int, path: List<Int>, cost: Int) {
        if (node == end) {
            if (best == null || cost < best!!.cost) {
                best = GraphPath(path, cost)
            }
            return
        }
        for (edge in adj[node].orEmpty()) {
            if (edge.to in path) continue
            walk(edge.to, path + edge.to, cost + edge.weight)
        }
    }

    walk(start, listOf(start), 0)
    return best
}
