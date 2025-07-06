package org.example.project.data.models.objects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

data class PinionGear(
    val radius: Float = 50f, // ピニオンギアの半径のデフォルト値
    val penSize: Float, // ペンのサイズ（PinionGearのストローク幅に影響）
    val spurGearRadius: Float, // スパーギアの半径（PinionGearの位置計算に必要）
    val spurGearStrokeWidth: Float // スパーギアのストローク幅（PinionGearの位置計算に必要）
) {
    // 計算プロパティ: ピニオンギアの中心座標 (これは固定オフセット計算であり、時間には依存しない)
    val position: Offset
        get() = Offset(
            ((spurGearRadius - spurGearStrokeWidth / 2f) - (radius + penSize / 2f)).toLong()
        )

    // 計算プロパティ: ピニオンギアのストローク（常にpenSizeと等しい）
    val pinionGearStroke: Stroke
        get() = Stroke(penSize)
}
