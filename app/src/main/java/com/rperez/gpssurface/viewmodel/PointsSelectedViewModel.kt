package com.rperez.gpssurface.viewmodel

// Import necessary Compose runtime and ViewModel classes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

/**
 * ViewModel for managing the state of selected points.
 * This ViewModel holds a MutableState that tracks a list of integers,
 * representing the currently selected points.
 */
class PointsSelectedViewModel : ViewModel() {

    // Backing field for pointsSelected; a mutable state holding a mutable list of integers.
    private val _pointsSelected = mutableStateListOf<Int>()

    /**
     * Publicly accessible state of the selected points.
     * Using a getter ensures that the internal backing field is not directly modified outside the ViewModel.
     */
    val pointsSelected
        get() = _pointsSelected
}
