package org.example.project.data.models.objects

import androidx.compose.ui.geometry.Offset

// Penクラスは、主にそのパラメータと手動設定された位置を管理します。
// アニメーションによる位置計算はAppStateで行われます。
data class Pen(val penRadius: Float = 10f) {
    // Penのインスタンス自体は、手動で設定された位置のみを保持します。
    // アニメーションによる位置はAppStateで計算され、DrawingCanvasに渡されます。
    // このプロパティは、ユーザーが手動でペンを動かした場合にのみ設定されます。
    var manualPosition: Offset? = null // nullの場合はアニメーションに追従

    // 手動設定を解除し、アニメーションに追従させるためのメソッド
    fun resetManualPosition() {
        manualPosition = null
    }
}
