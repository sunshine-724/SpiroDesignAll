package org.example.project.data.models

import androidx.compose.ui.graphics.Color

// アプリケーション状態を管理するデータクラス
data class AppState(
    val currentScreen: DialogScreen = DialogScreen.Main,
    val penRadius: Float = 10f,
    val spurSpeed: Float = 1f,
    val currentColor: Color = Color.Black,
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    val locus: List<PathPoint> = emptyList()
)