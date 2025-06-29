package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import org.example.project.data.models.PathPoint
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DrawingCanvas(
    color: Color,
    speed : Float,
    locus : List<PathPoint>,
    penSize : Stroke,
    isPlaying : Boolean,
    isExporting : Boolean,
    onAddPoint: (PathPoint) -> Unit,
) {
    val spurGearRadius = 300f            // 固定円（大きい円）の基本半径
    val pinionGearRadius = 50f           // 回転する円（ピニオンギア）の基本半径

    val spurGearStroke = Stroke(20f) // スパーギアのストローク

    // --- State定義 ---
    var pinionCenterOffset by remember { mutableStateOf(Offset.Zero) } //ピニオンギアの中心座標
    var penOffset by remember { mutableStateOf(Offset.Zero) } //ペンの中心座標
    var canvasSize by remember { mutableStateOf(Size.Zero) } //キャンバスサイズ(2次元)

    val latestSpeed by rememberUpdatedState(speed) //毎フレームspeedを監視し変更する
    val latestIsPlaying by rememberUpdatedState(isPlaying) // 現在のstartとstopのフラグ
    val latestColor by rememberUpdatedState(color) //現在の色
    val latestPenSize by rememberUpdatedState(penSize) //現在のペンのサイズ

    LaunchedEffect(canvasSize) {
        if (canvasSize == Size.Zero) return@LaunchedEffect

        // --- ストロークを考慮した「実効半径」を定義 ---
        // 1. 固定円が転がりに影響する「内側の半径」
        val effectiveSpurGearRadius = spurGearRadius - spurGearStroke.width / 2f
        // 2. ピニオンが転がる「外側の半径」
        val effectivePinionGearRadius = pinionGearRadius + latestPenSize.width / 2f
        // 3. ピニオンの中心からペン先までの距離（今回はピニオンの内周に設定）
        val effectivePenRadius = pinionGearRadius

        var time = 0f

        // time=0 の時の座標を計算
        val centerInitDistance = effectiveSpurGearRadius - effectivePinionGearRadius
        val initialPinionCenterX = centerInitDistance * cos(time)
        val initialPinionCenterY = centerInitDistance * sin(time)
        pinionCenterOffset = Offset(initialPinionCenterX, initialPinionCenterY)

        val initialPenRotationAngle = (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * time
        val initialPenRelativeX = effectivePenRadius * cos(initialPenRotationAngle)
        val initialPenRelativeY = -effectivePenRadius * sin(initialPenRotationAngle)
        penOffset = pinionCenterOffset + Offset(initialPenRelativeX, initialPenRelativeY)

        while (true) {
            if(latestIsPlaying){
                //  ピニオンギア（小さい円）の中心座標を計算
                //  中心間の距離は「大きい円の実効半径 - 小さい円の実効半径」
                val centerDistance = effectiveSpurGearRadius - effectivePinionGearRadius
                val pinionCenterX = centerDistance * cos(time)
                val pinionCenterY = centerDistance * sin(time)
                pinionCenterOffset = Offset(pinionCenterX, pinionCenterY)

                // ピニオンギアの中心から見た「ペン先」の相対座標を計算
                val penRotationAngle = (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * time
                val penRelativeX = effectivePenRadius * cos(penRotationAngle)
                val penRelativeY = -effectivePenRadius * sin(penRotationAngle) // Yの符号をマイナスに

                // 最終的なペン先の絶対座標を計算
                penOffset = pinionCenterOffset + Offset(penRelativeX, penRelativeY)

                val newPathPoint = PathPoint(position = penOffset, color = latestColor,thickness = latestPenSize.width)
                onAddPoint(newPathPoint) //軌跡を追加

                time += 0.02f * latestSpeed
            }else{
            }
            delay(16L)
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
//                    println("描画された座標はlocus[$i] = ${locus[i].position}で描画された色は${locus[i].color}です")
                val prevPoint = locus[i - 1]
                val currentPoint = locus[i]

                // 前の点から現在の点まで、前の点の色で短い線を描画する
                drawLine(
                    color = prevPoint.color,
                    start = canvasCenter + prevPoint.position,
                    end = canvasCenter + currentPoint.position,
                    strokeWidth = currentPoint.thickness,
                    cap = StrokeCap.Round
                )
            }
        }
        if(!isExporting){
            // 固定円の描画
            drawCircle(
                color = Color.Blue,
                radius = spurGearRadius,
                center = canvasCenter,
                style = spurGearStroke
            )

            // 回転する円（ピニオンギア）の描画
            drawCircle(
                color = Color.Red,
                radius = pinionGearRadius,
                center = canvasCenter + pinionCenterOffset,
                style = latestPenSize
            )

            // ペン先の描画
            drawCircle(
                color = latestColor,
                radius = latestPenSize.width,
                center = canvasCenter + penOffset
            )
        }
    }
}