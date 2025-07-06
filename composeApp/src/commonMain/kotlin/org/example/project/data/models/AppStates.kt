package org.example.project.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import org.example.project.data.models.objects.Pen
import org.example.project.data.models.objects.PinionGear
import org.example.project.data.models.objects.SpurGear
import kotlin.math.cos
import kotlin.math.sin

// AppStateの初期化に必要な仮定義 (実際のプロジェクトに合わせてください)
enum class DialogScreen { Main, Settings }
data class PathPoint(val position: Offset, val color: Color, val thickness: Float)

// アプリケーション状態を管理するデータクラス
data class AppState(
    val currentScreen: DialogScreen = DialogScreen.Main, // 現在の画面
    val penSize: Float = 10f, // 現在のペンのサイズ (PinionGearのストローク幅にも影響)
    val pinionGearSpeed: Float = 1f, // ピニオンギアのスピード
    val currentPenColor: Color = Color.Black, // 現在のペンの色
    val isPlaying: Boolean = false, // 現在動いているかどうか
    val isExporting: Boolean = false, // 現在Canvasから画像を出力中かどうか
    val locus: List<PathPoint> = emptyList(), // 軌跡

    // アニメーションの時間状態
    val animationTime: Float = 0f, // AppStateで時間を管理する

    // ギアとペンの設定パラメータ (これらはアニメーション中も固定)
    val spurGear: SpurGear = SpurGear(), // SpurGearの半径、ストローク幅など

    // PinionGearのインスタンスを初期化 (パラメータのみを保持)
    val pinionGear: PinionGear = PinionGear(
        radius = 50f, // デフォルト値
        penSize = penSize, // AppStateのpenSizeをPinionGearに渡す
        spurGearRadius = spurGear.radius, // spurGearインスタンスのradiusをPinionGearに渡す
        spurGearStrokeWidth = spurGear.strokeWidth // spurGearインスタンスのstrokeWidthをPinionGearに渡す
    ),

    // Penのインスタンスを初期化 (パラメータのみを保持し、manualPositionはnullで初期化)
    val pen: Pen = Pen()
) {
    // 計算プロパティ: アニメーション時間に基づいて計算されたピニオンギアの中心位置
    val animatedPinionCenterOffset: Offset
        get() {
            // 固定円が転がりに影響する「内側の半径」
            val effectiveSpurGearRadius = spurGear.radius - spurGear.stroke.width / 2f
            // ピニオンが転がる「外側の半径」
            val effectivePinionGearRadius = pinionGear.radius + penSize / 2f // AppStateのpenSizeを使用
            // 中心間の距離
            val centerDistance = effectiveSpurGearRadius - effectivePinionGearRadius
            val pinionCenterX = centerDistance * cos(animationTime)
            val pinionCenterY = centerDistance * sin(animationTime)
            return Offset(pinionCenterX, pinionCenterY)
        }

    // 計算プロパティ: アニメーション時間に基づいて計算されたペンの位置
    val animatedPenOffset: Offset
        get() {
            // ピニオンギアの中心から見た「ペン先」の相対座標を計算
            val effectiveSpurGearRadius = spurGear.radius - spurGear.stroke.width / 2f
            val effectivePinionGearRadius = pinionGear.radius + penSize / 2f
            val effectivePenRadius = pinionGear.radius // ピニオンの内周に設定

            val penRotationAngle = (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * animationTime
            val penRelativeX = effectivePenRadius * cos(penRotationAngle)
            val penRelativeY = -effectivePenRadius * sin(penRotationAngle) // Yの符号をマイナスに

            return animatedPinionCenterOffset + Offset(penRelativeX, penRelativeY)
        }

    // DrawingCanvasに渡す最終的なペンの描画位置
    // manualPenPositionが設定されていればそれを優先し、そうでなければアニメーション位置を返す
    val currentPenDrawingPosition: Offset
        get() = pen.manualPosition ?: animatedPenOffset
}
