package com.rperez.gpssurface.util

import androidx.compose.ui.geometry.Offset

/**
 * Utility class for performing operations related to points in a 2D space.
 */
class PointsUtil {

    /**
     * Checks whether a given point is within a specified radius of a target point.
     *
     * The distance is calculated using the Euclidean distance formula.
     *
     * @param point The point to check.
     * @param target The target point to compare against.
     * @param radius The radius within which the point is considered "near" the target.
     * @return `true` if the point is within the specified radius of the target, otherwise `false`.
     */
    fun isPointNear(point: Offset, target: Offset, radius: Float): Boolean {
        val dx = point.x - target.x
        val dy = point.y - target.y
        return dx * dx + dy * dy <= radius * radius
    }
}
