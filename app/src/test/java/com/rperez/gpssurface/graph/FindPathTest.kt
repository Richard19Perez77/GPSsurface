package com.rperez.gpssurface.graph

import com.rperez.gpssurface.data.Edge
import com.rperez.gpssurface.data.MapData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindPathTest {

    @Test
    fun sameNodeHasZeroCost() {
        val path = findPath(MapData.edges, start = 0, end = 0)
        assertEquals(GraphPath(listOf(0), 0), path)
    }

    @Test
    fun directEdge() {
        val path = findPath(MapData.edges, start = 0, end = 1)
        assertEquals(GraphPath(listOf(0, 1), 1), path)
    }

    @Test
    fun cheaperDirectEdgeBeatsLongerWalk() {
        val path = findPath(MapData.edges, start = 0, end = 3)
        assertEquals(GraphPath(listOf(0, 3), 3), path)
    }

    @Test
    fun cheapestPathAcrossTheLabGraph() {
        val path = findPath(MapData.edges, start = 0, end = 9)
        assertEquals(GraphPath(listOf(0, 1, 2, 5, 6, 8, 9), 28), path)
    }

    @Test
    fun reverseTapOrderUsesTheSameEdges() {
        val path = findPath(MapData.edges, start = 9, end = 0)
        assertEquals(GraphPath(listOf(9, 8, 6, 5, 2, 1, 0), 28), path)
    }

    @Test
    fun noPathWhenDisconnected() {
        val edges = listOf(Edge(0, 1, 1), Edge(2, 3, 1))
        assertNull(findPath(edges, start = 0, end = 3))
    }

    @Test
    fun tinyFixtureGraph() {
        val edges = listOf(
            Edge(0, 1, 10),
            Edge(0, 2, 1),
            Edge(2, 1, 1),
        )
        val path = findPath(edges, start = 0, end = 1)
        assertEquals(GraphPath(listOf(0, 2, 1), 2), path)
    }
}
