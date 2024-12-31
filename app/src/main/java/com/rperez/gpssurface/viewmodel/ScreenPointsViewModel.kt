package com.rperez.gpssurface.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

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
        _screenPoints.value.clear()
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

    fun initPoints(h: Float, w: Float) {
        setHW(h, w)
        generateScreenPoints()
    }
}
