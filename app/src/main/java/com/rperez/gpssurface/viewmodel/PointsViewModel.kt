package com.rperez.gpssurface.viewmodel

import androidx.lifecycle.ViewModel

class PointsViewModel : ViewModel() {

    var h = 0.0f
    var w = 0.0f

    var randomPoints = List(10) {
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

    fun refreshRandomPoints() {
        randomPoints = List(10) {
            Pair(
                (-90..90).random().toDouble(), (-180..180).random().toDouble()
            )
        }
        generateScreenPoints()
    }
}