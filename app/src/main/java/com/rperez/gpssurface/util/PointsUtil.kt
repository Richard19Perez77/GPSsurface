package com.rperez.gpssurface.util

import androidx.compose.ui.geometry.Offset

class PointsUtil {
    fun isPointNear(point: Offset, target: Offset, radius: Float): Boolean {
        val dx = point.x - target.x
        val dy = point.y - target.y
        return dx * dx + dy * dy <= radius * radius
    }
}