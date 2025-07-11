package org.example.project.data.models

import androidx.compose.ui.graphics.Color

/**
 * アプリケーション状態を管理するデータクラス
 * スピログラフアプリの描画設定や再生状態を保持する
 *
 * @param currentScreen 現在表示している画面の種類
 * @param penRadius ペンの半径（太さ）
 * @param spurSpeed スパーの回転速度
 * @param currentColor 現在の描画色
 * @param isPlaying アニメーションが再生中かどうか
 * @param isExporting エクスポート処理中かどうか
 * @param locus スピログラフの軌跡データ
 */
data class AppState(
    val currentScreen: DialogScreen = DialogScreen.Main,
    val penRadius: Float = 10f,
    val spurSpeed: Float = 1f,
    val currentColor: Color = Color.Black,
    val isPlaying: Boolean = false,
    val isExporting: Boolean = false,
    val locus: List<PathPoint> = emptyList()
)