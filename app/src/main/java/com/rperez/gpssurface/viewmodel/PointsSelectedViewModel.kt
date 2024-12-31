package com.rperez.gpssurface.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class PointsSelectedViewModel : ViewModel() {
    private val _pointsSelected = mutableStateOf(mutableListOf<Int>())
    val pointsSelected: MutableState<MutableList<Int>> get() = _pointsSelected
}
