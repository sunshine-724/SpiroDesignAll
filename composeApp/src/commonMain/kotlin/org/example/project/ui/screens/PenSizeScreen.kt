package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.components.CustomButton

@Composable
fun PenSizeScreenContent(
    currentRadius: Float,
    onPenSizeChange: (Float) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("ペンの太さを調整", fontSize = 20.sp)
        Text("現在の太さ: ${currentRadius.toInt()}", fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CustomButton("Small") { onPenSizeChange(5f) }
            CustomButton("Medium") { onPenSizeChange(10f) }
            CustomButton("Large") { onPenSizeChange(20f) }
        }
        CustomButton("戻る") { onNavigateBack() }
    }
}