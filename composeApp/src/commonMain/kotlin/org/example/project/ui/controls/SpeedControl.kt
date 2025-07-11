package org.example.project.ui.controls

import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
public fun SpeedControl(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Text(text = "Current Speed: ${(currentSpeed * 100).roundToInt() / 100.0}", fontSize = 16.sp)
    Slider(
        value = currentSpeed,
        onValueChange = onSpeedChange,
        valueRange = 0.1f..5f
    )
}