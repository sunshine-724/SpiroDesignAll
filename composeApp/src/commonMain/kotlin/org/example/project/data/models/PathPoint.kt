package org.example.project.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PathPoint(
    var position: Offset,
    val thickness: Float,
    val color: Color
)