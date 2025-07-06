package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import org.example.project.data.models.PathPoint
import org.example.project.data.models.AppState // AppStateをインポート

@Composable
fun DrawingCanvas(
    // AppState全体を受け取る
    appState: AppState,
    // AppStateの更新を通知するコールバック
    onAppStateChanged: (AppState) -> Unit,
    // 軌跡を追加するコールバック (AppStateの更新を伴う)
    onAddPoint: (PathPoint) -> Unit
) {
    // AppStateから必要なプロパティを直接取得し、rememberUpdatedStateで最新の状態を監視
    val spurGear = appState.spurGear
    val pinionGear = appState.pinionGear
    appState.pen // Penインスタンス自体もAppStateから取得
    val color by rememberUpdatedState(appState.currentPenColor)
    val speed by rememberUpdatedState(appState.pinionGearSpeed)
    val locus = appState.locus
    val isPlaying by rememberUpdatedState(appState.isPlaying)
    val isExporting by rememberUpdatedState(appState.isExporting)
    val currentPenStrokeWidth by rememberUpdatedState(appState.penSize) // AppStateのpenSizeを直接使用

    var canvasSize by remember { mutableStateOf(Size.Zero) } // キャンバスサイズ(2次元)

    // アニメーション時間の更新をAppStatesに委譲
    LaunchedEffect(canvasSize, isPlaying, speed) {
        if (canvasSize == Size.Zero) return@LaunchedEffect

        while (true) {
            if (isPlaying) {
                // AppStateのanimationTimeを更新
                // appStateのコピーを作成し、animationTimeを更新してonAppStateChangedを呼び出す
                onAppStateChanged(appState.copy(animationTime = appState.animationTime + (0.02f * speed)))

                // ペンの描画位置はAppStateのcurrentPenDrawingPositionから取得
                // このプロパティは、手動設定があればそれを優先し、なければアニメーション位置を返す
                val currentDrawingPosition = appState.currentPenDrawingPosition

                // 軌跡を追加
                val newPathPoint = PathPoint(
                    position = currentDrawingPosition,
                    color = color,
                    thickness = currentPenStrokeWidth
                )
                onAddPoint(newPathPoint)
            }
            delay(16L) // 約60fps
        }
    }

    // --- 描画処理 ---
    Canvas(modifier = Modifier
        .fillMaxSize()
        .onSizeChanged { intSize ->
            canvasSize = intSize.toSize()
        }
    ) {
        val canvasCenter = center
        // --- 軌跡の描画 (より連続的に) ---
        if (locus.size > 1) {
            for (i in 1 until locus.size) {
                val prevPoint = locus[i - 1]
                val currentPoint = locus[i]

                drawLine(
                    color = prevPoint.color,
                    start = canvasCenter + prevPoint.position,
                    end = canvasCenter + currentPoint.position,
                    strokeWidth = currentPoint.thickness,
                    cap = StrokeCap.Round
                )
            }
        }
        if (!isExporting) {
            // 固定円の描画 (SpurGearオブジェクトからプロパティを取得)
            drawCircle(
                color = Color.Blue,
                radius = spurGear.radius,
                center = canvasCenter + spurGear.position, // SpurGearのpositionを使用
                style = spurGear.stroke // SpurGearのstrokeを使用
            )

            // 回転する円（ピニオンギア）の描画 (AppStateの計算されたアニメーション位置を使用)
            drawCircle(
                color = Color.Red,
                radius = pinionGear.radius,
                center = canvasCenter + appState.animatedPinionCenterOffset, // AppStateの計算されたアニメーション位置を使用
                style = pinionGear.pinionGearStroke // PinionGearのpinionGearStrokeを使用
            )

            // ペン先の描画 (AppStateのcurrentPenDrawingPositionを使用)
            drawCircle(
                color = color, // AppStateのcurrentPenColorを使用
                radius = currentPenStrokeWidth, // ペン先の半径はAppStateのpenSizeを使用
                center = canvasCenter + appState.currentPenDrawingPosition // AppStateの計算された最終的なペンの描画位置を使用
            )
        }
    }
}
