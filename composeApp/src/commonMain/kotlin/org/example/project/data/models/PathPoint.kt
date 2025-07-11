package org.example.project.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * スピログラフの軌跡上の一点を表すデータクラス
 *
 * @param position 2D座標上の位置
 * @param thickness 線の太さ
 * @param color 描画色
 * @param isBlank この点が空白点（描画しない点）かどうか
 */
data class PathPoint(
    var position: Offset,
    var thickness: Float,
    val color: Color,
    val isBlank: Boolean = false
)