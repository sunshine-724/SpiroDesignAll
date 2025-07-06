package org.example.project.data.models.objects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

data class SpurGear(
    val radius: Float = 300f, // スパーギアの半径のデフォルト値
    val position: Offset = Offset.Zero, // スパーギアの中心座標のデフォルト値
    val strokeWidth: Float = 20f // スパーギアのストローク幅のデフォルト値
) {
    // 計算プロパティ: strokeWidthからStrokeオブジェクトを生成
    val stroke: Stroke
        get() = Stroke(width = strokeWidth)
}
