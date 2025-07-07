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
import org.example.project.data.models.DraggingMode
import org.example.project.data.models.DraggingMode.*
import org.example.project.data.models.PathPoint
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DrawingCanvas(
    color: Color,
    speed: Float,
    locus: List<PathPoint>,
    penSize: Stroke,
    isPlaying: Boolean,
    isExporting: Boolean,
    onAddPoint: (PathPoint) -> Unit,
) {
    var spurGearRadius by remember { mutableStateOf(300f) }  // 固定円（大きい円）の基本半径
    var pinionGearRadius by remember { mutableStateOf(50f) } // 回転する円（ピニオンギア）の基本半径

    val spurGearStroke = Stroke(20f) // スパーギアのストローク

    // 画面に描画するときに必要なパラメーター
    var spurCenterOffset by remember { mutableStateOf(Offset.Zero) }
    var pinionCenterOffset by remember { mutableStateOf(Offset.Zero) } //ピニオンギアの中心座標
    var penOffset by remember { mutableStateOf(Offset.Zero) } //ペンの中心座標
    var canvasSize by remember { mutableStateOf(Size.Zero) } //キャンバスサイズ(2次元)

    val latestSpeed by rememberUpdatedState(speed) //毎フレームspeedを監視し変更する
    val latestIsPlaying by rememberUpdatedState(isPlaying) // 現在のstartとstopのフラグ
    val latestColor by rememberUpdatedState(color) //現在の色
    val latestPenSize by rememberUpdatedState(penSize) //現在のペンのサイズ(ピニオンギアのストロークと常に一致)

    // 入力された情報を管理するパラメーター
    /**
     * Dragging mode
     */
    var draggingMode by remember { mutableStateOf<DraggingMode>(NONE) }

    /**
     * Clicked position
     */
    var clickedStartPosition by remember { mutableStateOf(Offset.Zero) }

    var isInner: Boolean = true


    /**
     * クリックした座標(絶対座標)からどのようなドラッグモードかを判別します
     * なお、判別する際はCanvasの中心座標かあの相対座標に変換して判別します
     * 変数の更新タイミングはrememberの引数のどれかが更新されると更新します
     * @param Offset クリックした座標(Canvasの中心座標からの絶対座標)
     */
    val determineDraggingMode: (
        Offset, // absolutePosition
    ) -> DraggingMode = remember(
        spurCenterOffset, pinionCenterOffset, penOffset, spurGearRadius, canvasSize
    ) {
        { absolutePosition ->
            val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val relativePosition = absolutePosition - canvasCenter // 相対座標に変換

            // 各オブジェクトの中心からの距離を計算し、モードを決定
            // 許容誤差を考慮して判定する
            val tolerance = 10.0f // 判定の許容範囲

            // デバッグ出力：絶対座標と相対座標
            println("DEBUG: Tap Absolute Position: $absolutePosition")
            println("DEBUG: Tap Relative Position (from canvas center): $relativePosition")

            println("DEBUG: Tolerance: $tolerance")

            val spurCenterDistance = (relativePosition - spurCenterOffset).getDistance()
            val spurRadiusDiff = abs((relativePosition - spurCenterOffset).getDistance() - spurGearRadius)
            val pinionCenterDistance = (relativePosition - pinionCenterOffset).getDistance()
            val penDistance = (relativePosition - penOffset).getDistance()

            // デバッグ出力：各オブジェクトの中心からの距離
            println("DEBUG: Spur Center Offset: $spurCenterOffset, Distance to tap: $spurCenterDistance")
            println("DEBUG: Spur Radius: $spurGearRadius, Difference to tap distance: $spurRadiusDiff")
            println("DEBUG: Pinion Center Offset: $pinionCenterOffset, Distance to tap: $pinionCenterDistance")
            println("DEBUG: Pen Offset: $penOffset, Distance to tap: $penDistance")


            // スパーギアの中心を移動
            if ((relativePosition - spurCenterOffset).getDistance() <= tolerance) {
                MOVE_SPUR_CENTER
            }
            // スパーギアの半径を変更
            // 円周付近をクリックした場合
            else if (abs((relativePosition - spurCenterOffset).getDistance() - spurGearRadius) <= tolerance) {
                RESIZE_SPUR_RADIUS
            }
            // ピニオンギアの中心を移動
            else if ((relativePosition - pinionCenterOffset).getDistance() <= tolerance) {
                RESIZE_PINION_RADIUS_AND_MOVE_CENTER
            }
            // ペンを移動
            else if ((relativePosition - penOffset).getDistance() <= tolerance) {
                MOVE_PEN // ペンを直接動かすモードを追加
            }
            // 何もヒットしない場合はパン（全体移動）
            else {
                PAN
            }
        }
    }

    /**
     * Execute dragging
     */
    val executeDragging: (DraggingMode, Offset, Offset) -> Unit = remember(
        spurGearRadius, spurCenterOffset, pinionGearRadius, pinionCenterOffset, penOffset
    ) {
        { draggingMode, draggingAmount, absolutePosition ->
            val canvasCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val relativePosition = absolutePosition - canvasCenter // 相対座標に変換

            when (draggingMode) {
                NONE -> Unit
                MOVE_SPUR_CENTER -> {
                    spurCenterOffset += draggingAmount
                    pinionCenterOffset += draggingAmount
                    penOffset += draggingAmount
                }

                RESIZE_SPUR_RADIUS -> {
                    val oldSpurGearRadius = spurGearRadius
                    val oldPinionRadius = pinionGearRadius

                    val newSpurRadius = (relativePosition - spurCenterOffset).getDistance()
                    spurGearRadius = newSpurRadius // スパーギアの半径を更新

                    val scaleRatio = if (oldSpurGearRadius != 0f) newSpurRadius / oldSpurGearRadius else 1f // 拡大、縮小倍率

                    val dist = (pinionCenterOffset - spurCenterOffset).getDistance()
                    val unitVec =
                        if (dist > 1e-6f) ((pinionCenterOffset - spurCenterOffset) / dist) else return@remember // ゼロ除算回避

                    // 内接円か外接円かで中心座標との距離が決まる
                    val centerDistance = if (isInner) {
                        newSpurRadius - pinionGearRadius
                    } else {
                        newSpurRadius + pinionGearRadius
                    }

                    val newPinionCenter = spurCenterOffset + unitVec * (centerDistance - (spurGearStroke.width / 2f)) // スパーギアのストロークも計算に反映させる

                    // データを更新
                    penOffset = penOffset * scaleRatio
                    pinionGearRadius = oldPinionRadius * scaleRatio
                    pinionCenterOffset = newPinionCenter
                }

                RESIZE_PINION_RADIUS_AND_MOVE_CENTER -> {
                    val dist = (relativePosition - spurCenterOffset).getDistance() //クリックした座標とスパーギアの中心座標との距離
                    if (dist < 5.0f) {
                        return@remember
                    }

                    val unitVec = (relativePosition - spurCenterOffset) / dist //クリックした点からスパーギアの中心点に向けた単位ベクトル
                    var newPinionRadius = abs(spurGearRadius - dist) // スパーギアの半径とクリックした距離の差分を取ることでピニオンギアの座標が決まる(太さは一旦無視)

                    val minRadius = 100.0f // ピニオンギアの最小半径
                    if (newPinionRadius < minRadius) {
                        newPinionRadius = minRadius
                    }

                    val hysteresis = 2.0f;
                    val innerLimit = spurGearRadius + (-1) * (newPinionRadius - hysteresis)
                    val outerLimit = spurGearRadius + (+1) * (newPinionRadius - hysteresis)

                    if (isInner) {
                        if (dist >= innerLimit) isInner = false;
                    } else {
                        if (dist <= outerLimit) isInner = true;
                    }

                    val distanceFromSpurCenter = if (isInner) {
                        spurGearRadius - (newPinionRadius + (spurGearStroke.width / 2f)) // スパーギアのストロークも計算に反映させる
                    } else {
                        spurGearRadius + (newPinionRadius + (spurGearStroke.width / 2f)) // スパーギアのストロークも計算に反映させる
                    }

                    val newCenter = spurCenterOffset + unitVec * distanceFromSpurCenter

                    pinionGearRadius = newPinionRadius
                    pinionCenterOffset = newCenter
                    penOffset += draggingAmount
                }

                MOVE_PEN -> {
                    val newPenOffset = relativePosition
                    penOffset = newPenOffset
                }

                PAN -> {
                    spurCenterOffset += draggingAmount
                    pinionCenterOffset += draggingAmount
                    penOffset += draggingAmount
                }
            }
        }
    }



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

        /**
         * time=0 の時の座標を計算
         */
        // スパーギア
        spurCenterOffset = Offset(0f, 0f)

        // ピニオンギア
        val centerInitDistance = effectiveSpurGearRadius - effectivePinionGearRadius
        val initialPinionCenterX = centerInitDistance * cos(time)
        val initialPinionCenterY = centerInitDistance * sin(time)
        pinionCenterOffset = Offset(initialPinionCenterX, initialPinionCenterY)

        // ペン
        val initialPenRotationAngle =
            (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * time
        val initialPenRelativeX = effectivePenRadius * cos(initialPenRotationAngle)
        val initialPenRelativeY = -effectivePenRadius * sin(initialPenRotationAngle)
        penOffset = pinionCenterOffset + Offset(initialPenRelativeX, initialPenRelativeY)

        while (true) {
            if (latestIsPlaying) {
                //  ピニオンギア（小さい円）の中心座標を計算
                //  中心間の距離は「大きい円の実効半径 - 小さい円の実効半径」
                val centerDistance = effectiveSpurGearRadius - effectivePinionGearRadius
                val pinionCenterX = centerDistance * cos(time)
                val pinionCenterY = centerDistance * sin(time)
                pinionCenterOffset = Offset(pinionCenterX, pinionCenterY)

                // ピニオンギアの中心から見た「ペン先」の相対座標を計算
                val penRotationAngle =
                    (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * time
                val penRelativeX = effectivePenRadius * cos(penRotationAngle)
                val penRelativeY = -effectivePenRadius * sin(penRotationAngle) // Yの符号をマイナスに

                // 最終的なペン先の絶対座標を計算
                penOffset = pinionCenterOffset + Offset(penRelativeX, penRelativeY)

                val newPathPoint = PathPoint(position = penOffset, color = latestColor, thickness = latestPenSize.width)
                onAddPoint(newPathPoint) //軌跡を追加

                time += 0.02f * latestSpeed
            } else {
            }
            delay(16L)
        }
    }

    // --- 描画処理 ---
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                canvasSize = intSize.toSize()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        clickedStartPosition = offset
                        draggingMode = determineDraggingMode(clickedStartPosition)
                        println("Drag Start. Mode: $draggingMode, Clicked: $clickedStartPosition")
                    },
                    onDrag = { change, dragAmount ->
                        if (draggingMode != NONE) {
                            executeDragging(draggingMode, dragAmount, change.position)
                            println("Drag. Mode: $draggingMode, Clicked: $clickedStartPosition")
                        }
                        change.consume() // イベントを消費
                    },
                    onDragEnd = {
                        println("Drag End. Mode reset to NONE.")
                        draggingMode = NONE
                    }
                )
            }
    ) {
        // --- 軌跡の描画 (より連続的に) ---
        if (locus.size > 1) {
            for (i in 1 until locus.size) {
                val prevPoint = locus[i - 1]
                val currentPoint = locus[i]

                // 前の点から現在の点まで、前の点の色で短い線を描画する
                drawLine(
                    color = prevPoint.color,
                    start = prevPoint.position,
                    end = currentPoint.position,
                    strokeWidth = currentPoint.thickness,
                    cap = StrokeCap.Round
                )
            }
        }
        if (!isExporting) {
            // スパーギアの描画
            drawCircle(
                color = Color.Blue,
                radius = spurGearRadius,
                center = spurCenterOffset + center,
                style = spurGearStroke
            )

            // ピニオンギアの描画
            drawCircle(
                color = Color.Red,
                radius = pinionGearRadius,
                center = pinionCenterOffset + center,
                style = latestPenSize
            )

            // ペン先の描画
            drawCircle(
                color = latestColor,
                radius = latestPenSize.width,
                center = penOffset + center,
            )
        }
    }
}