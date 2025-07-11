package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay
import org.example.project.Platform
import org.example.project.data.models.DeviceType
import org.example.project.data.models.DraggingMode
import org.example.project.data.models.DraggingMode.*
import org.example.project.data.models.PathPoint
import kotlin.collections.forEach
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DrawingCanvas(
    color: Color,
    speed: Float,
    locus: List<PathPoint>,
    penSize: Stroke,
    cumulativeScale: Float,
    onScaleChange: (Float) -> Unit,
    isPlaying: Boolean,
    isExporting: Boolean,
    onAddPoint: (PathPoint) -> Unit,
    platform: Platform,
    onOpenMenu: () -> Unit,
) {
    var spurGearRadius by remember { mutableStateOf(300f) }  // 固定円（大きい円）の基本半径
    var pinionGearRadius by remember { mutableStateOf(50f) } // 回転する円（ピニオンギア）の基本半径

    val spurGearStroke = Stroke(20f) // スパーギアのストローク

    // 画面に描画するときに必要なパラメーター
    // 中心座標は相対座標で管理する
    var spurCenterOffset by remember { mutableStateOf(Offset.Zero) } // スパーギアの中心座標,基準はcanvasの中心座標
    var pinionCenterOffset by remember { mutableStateOf(Offset.Zero) } // ピニオンギアの中心座標,基準はスパーギアの中心座標
    var penOffset by remember { mutableStateOf(Offset.Zero) } // ペンの中心座標,基準はピニオンギアの中心座標

    var canvasSize by remember { mutableStateOf(Size.Zero) } //キャンバスサイズ(2次元)
    val canvasCenter by remember {
        derivedStateOf {
            Offset(
                canvasSize.width / 2f,
                canvasSize.height / 2f
            )
        }
    } //キャンバスの中心座標

    /**
     * Canvas全体の累計の拡大・縮小率を管理します
     * 1.0fが等倍(100%)
     */
    val latestCumulativeScale by rememberUpdatedState(cumulativeScale)
    val latestSpeed by rememberUpdatedState(speed) //毎フレームspeedを監視し変更する
    val latestIsPlaying by rememberUpdatedState(isPlaying) // 現在のstartとstopのフラグ
    val latestColor by rememberUpdatedState(color) //現在の色
    val latestPenSize by rememberUpdatedState(penSize) //現在のペンのサイズ(ピニオンギアのストロークと常に一致)

    // 入力の判定時,許容誤差を考慮して判定する
    val tolerance = if (platform.getDeviceType() == DeviceType.DESKTOP || platform.getDeviceType() == DeviceType.WEB) {
        10.0f
    } else {
        45.0f
    }

    // 入力された情報を管理するパラメーター
    /**
     * 今どのドラッグ状態かを管理します
     */
    var draggingMode by remember { mutableStateOf<DraggingMode>(NONE) }

    /**
     * ドラッグされた時の最初の座標を格納します
     */
    var clickedStartPosition by remember { mutableStateOf(Offset.Zero) }

    /**
     * ピニオンギアが内接しているか、外接しているかを判定します
     * trueの時ピニオンギアは内接しています
     */
    var isInner by remember { mutableStateOf(true) }

    /**
     * アニメーションの経過時間
     */
    var animationTime by remember { mutableStateOf(0f) }

    /**
     * タッチした時の処理が記述されています
     * @param currentDraggingMode 現在のモード
     * @param absolutePosition タッチした時の絶対座標
     */
    val determineTapMode: (
        DraggingMode,
        Offset,
    ) -> DraggingMode = remember(
        spurCenterOffset, pinionCenterOffset, penOffset, spurGearRadius,pinionGearRadius,canvasSize
    ) {
        { currentDraggingMode,absolutePosition ->
            println("absolutePosition - (spurCenterOffset + pinionCenterOffset + canvasCenter): ${absolutePosition - spurCenterOffset + pinionCenterOffset + canvasCenter}, pinionCenterOffset: $pinionCenterOffset")
            println("distance: ${(absolutePosition - (spurCenterOffset + pinionCenterOffset) - pinionCenterOffset).getDistance()}, radius: $pinionGearRadius")
            if ((absolutePosition - (spurCenterOffset + pinionCenterOffset + canvasCenter) - pinionCenterOffset).getDistance() <= pinionGearRadius + latestPenSize.width / 2f) {
                MOVE_PEN // ペンを直接動かすモードを追加
            }else{
                currentDraggingMode
            }
        }
    }

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
            val relativePosition = absolutePosition - canvasCenter // 相対座標に変換

            // 各オブジェクトの中心からの距離を計算し、モードを決定

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
            else if (abs((relativePosition - spurCenterOffset).getDistance() - spurGearRadius) <= tolerance) {
                RESIZE_SPUR_RADIUS
            }
            // ピニオンギアの中心を移動
            else if ((relativePosition - (pinionCenterOffset + spurCenterOffset)).getDistance() <= tolerance) {
                RESIZE_PINION_RADIUS_AND_MOVE_CENTER
            }
            // ペンを移動
            else if ((relativePosition - (penOffset + pinionCenterOffset + spurCenterOffset)).getDistance() <= tolerance) {
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
        spurGearRadius, spurCenterOffset, pinionGearRadius, pinionCenterOffset, penOffset, locus, latestPenSize
    ) {
        { draggingMode, draggingAmount, absolutePosition ->
            val relativePosition = absolutePosition - canvasCenter // 相対座標に変換(基準はcanvasの中心座標)

            when (draggingMode) {
                NONE -> Unit
                MOVE_SPUR_CENTER, PAN -> {
                    spurCenterOffset += draggingAmount

                    locus.forEach { pathPoint ->
                        pathPoint.position += draggingAmount
                    }
                }

                RESIZE_SPUR_RADIUS -> {
                    val oldSpurGearRadius = spurGearRadius
                    // 新しい半径をカーソル位置から計算
                    val newSpurRadius = (relativePosition - spurCenterOffset).getDistance()

                    // 半径の変化がごくわずかなら処理を中断
                    if (abs(newSpurRadius - oldSpurGearRadius) < 0.1f) return@remember

                    // スケーリング比率を計算
                    val scaleRatio = newSpurRadius / oldSpurGearRadius

                    // すべての関連する長さを、この比率でシンプルにスケールする
                    spurGearRadius = newSpurRadius
                    pinionGearRadius *= scaleRatio
                    pinionCenterOffset *= scaleRatio
                    penOffset *= scaleRatio
                }

                RESIZE_PINION_RADIUS_AND_MOVE_CENTER -> {
                    // ピニオンギアの半径(更新前)
                    val oldPinionRadius = pinionGearRadius

                    // スパーギアの中心から見たカーソルの相対位置
                    val pointerFromSpurCenter = relativePosition - spurCenterOffset
                    val distFromSpurCenter = pointerFromSpurCenter.getDistance()

                    // 新しいピニオンギアの半径を計算
                    // 内側なら (Spur半径 - 距離)、外側なら (距離 - Spur半径)
                    val newPinionRadius = if (isInner) {
                        spurGearRadius - distFromSpurCenter
                    } else {
                        distFromSpurCenter - spurGearRadius
                    }.coerceAtLeast(40f) // 最小半径を40fに制限

                    // 内外判定を更新
                    isInner = distFromSpurCenter < spurGearRadius

                    // 新しいピニオンギアの中心までの距離を計算
                    val newDistanceFromSpurCenter = if (isInner) {
                        spurGearRadius - (newPinionRadius + (spurGearStroke.width / 2f))
                    } else {
                        spurGearRadius + (newPinionRadius + (spurGearStroke.width / 2f))
                    }

                    // 単位ベクトルを計算して、新しい中心位置を決定
                    val unitVec =
                        if (distFromSpurCenter > 1e-6f) pointerFromSpurCenter / distFromSpurCenter else Offset(1f, 0f)
                    val newPinionCenter = unitVec * (newDistanceFromSpurCenter)

                    val scaleRatio = if (oldPinionRadius > 1e-6f) (newPinionRadius / oldPinionRadius) else 1f
                    penOffset *= scaleRatio

                    // データを更新
                    pinionGearRadius = newPinionRadius
                    pinionCenterOffset = newPinionCenter
                }

                MOVE_PEN -> {
                    val newPenOffset =
                        relativePosition - (spurCenterOffset + pinionCenterOffset) // 絶対座標からピニオンギアの中心からの相対座標に変換
                    if (abs((newPenOffset).getDistance()) <= (pinionGearRadius + (latestPenSize.width / 2f))) {
                        penOffset = newPenOffset
                    }
                }
            }
        }
    }

    // --- 座標の初期化用のLaunchedEffect ---
    LaunchedEffect(canvasSize) {
        // canvasSize が確定したときに一度だけ実行される
        if (canvasSize == Size.Zero) return@LaunchedEffect

        // ピニオンギアの初期相対座標
        val effectiveSpurGearRadius = spurGearRadius - spurGearStroke.width / 2f
        val effectivePinionGearRadius = pinionGearRadius + latestPenSize.width / 2f
        val effectivePenRadius = pinionGearRadius

        val initialTime = 0f // 初期化時の時間

        val centerInitDistance = effectiveSpurGearRadius - effectivePinionGearRadius
        val initialPinionCenterX = centerInitDistance * cos(initialTime)
        val initialPinionCenterY = centerInitDistance * sin(initialTime)
        pinionCenterOffset = Offset(initialPinionCenterX, initialPinionCenterY) // 相対座標で設定

        val initialPenRotationAngle =
            (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius * initialTime
        val initialPenRelativeX = effectivePenRadius * cos(initialPenRotationAngle)
        val initialPenRelativeY = -effectivePenRadius * sin(initialPenRotationAngle)
        penOffset = Offset(initialPenRelativeX, initialPenRelativeY) // 相対座標で設定
    }

    // --- アニメーションループ用のLaunchedEffect ---
    LaunchedEffect(latestIsPlaying) {
        if (latestIsPlaying) {
            // 手動で設定した位置がアニメーションの開始点になる
            // ピニオンギアの「スパーギア中心からの相対座標」を使って角度を計算する
            val startAngle = atan2(pinionCenterOffset.y, pinionCenterOffset.x)
            animationTime = startAngle

            // アニメーション開始時の「ピニオンギア中心からのペン先の距離と角度」を記憶しておく
            val userSetPenDistance = penOffset.getDistance()
            val userSetPenAngle = atan2(-penOffset.y, penOffset.x)

            // ストロークを考慮した実行半径を求める
            val effectiveSpurGearRadius = spurGearRadius - spurGearStroke.width / 2f
            val effectivePinionGearRadius = pinionGearRadius + latestPenSize.width / 2f

            // 実際にピニオンギアが回る軸の半径を求める
            val rotationRatio = if (isInner) {
                (effectiveSpurGearRadius - effectivePinionGearRadius) / effectivePinionGearRadius
            } else {
                (effectiveSpurGearRadius + effectivePinionGearRadius) / effectivePinionGearRadius
            }

            val calculatedStartPenAngle = rotationRatio * startAngle

            // 実際の角度と計算上の角度の差（オフセット）を計算する
            val angleOffset = userSetPenAngle - calculatedStartPenAngle

            animationTime = startAngle

            while (true) {
                val effectiveSpurGearRadius = spurGearRadius - spurGearStroke.width / 2f
                val effectivePinionGearRadius = pinionGearRadius + latestPenSize.width / 2f

                val centerDistance = if (isInner) {
                    effectiveSpurGearRadius - effectivePinionGearRadius
                } else {
                    effectiveSpurGearRadius + effectivePinionGearRadius
                }

                val pinionCenterX = centerDistance * cos(animationTime)
                val pinionCenterY = centerDistance * sin(animationTime)
                pinionCenterOffset = Offset(pinionCenterX, pinionCenterY)

                val currentPenRotationAngle = (rotationRatio * animationTime) + angleOffset

                // 記憶しておいた「ユーザー設定の距離」を使ってペン先の位置を計算する
                val penRelativeX = userSetPenDistance * cos(currentPenRotationAngle)
                val penRelativeY = -userSetPenDistance * sin(currentPenRotationAngle)
                penOffset = Offset(penRelativeX, penRelativeY)

                // 軌跡の追加 (描画時にはすべてのオフセットを足して絶対座標に変換)
                val currentPenAbsolutePosition =
                    penOffset + pinionCenterOffset + spurCenterOffset + canvasCenter
                val newPathPoint = PathPoint(
                    position = currentPenAbsolutePosition,
                    color = latestColor,
                    thickness = latestPenSize.width
                )
                onAddPoint(newPathPoint)

                animationTime += 0.02f * latestSpeed
                delay(16L)
            }
        }
    }

    // --- 描画処理 ---
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                canvasSize = intSize.toSize()
            }
            .pointerInput(platform) {
                detectTapGestures(
                    onTap = { offset ->
                        // シングルタップは、再生中でない時だけ処理する
                        if (!latestIsPlaying) {
                            println("Tap. Offset: $offset")
                            if (determineTapMode(draggingMode, offset) == MOVE_PEN) {
                                val relativePosition = offset - canvasCenter
                                val newPenOffset =
                                    relativePosition - (spurCenterOffset + pinionCenterOffset)
                                if (abs((newPenOffset).getDistance()) <= (pinionGearRadius + (latestPenSize.width / 2f))) {
                                    penOffset = newPenOffset
                                }
                            }
                        }
                    },
                    onDoubleTap = {
                        // ダブルタップは、再生中も常に処理する (iOS/Android)
                        if (platform.getDeviceType() == DeviceType.IOS || platform.getDeviceType() == DeviceType.ANDROID) {
                            onOpenMenu()
                        }
                    },
                    onLongPress = {
                        // 長押しは、再生中も常に処理する (Desktop/Web)
                        if (platform.getDeviceType() == DeviceType.DESKTOP || platform.getDeviceType() == DeviceType.WEB) {
                            onOpenMenu()
                        }
                    }
                )
            }
            .pointerInput(latestIsPlaying) {
                if(!latestIsPlaying){
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
            }
            .pointerInput(latestIsPlaying) {
                if(!latestIsPlaying){
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val scrollDelta = event.changes.first().scrollDelta.y
                                val zoomFactor = 1.0f - scrollDelta * 0.001f

                                val cursorAbsolutePosition = event.changes.first().position

                                // カーソルの位置を取得（Canvas中心からの相対座標）
                                val cursorPosition = cursorAbsolutePosition - canvasCenter

                                // 以前の各種値を保持
                                val oldSpurGearRadius = spurGearRadius
                                val oldSpurCenterOffset = spurCenterOffset

                                // 新しい半径を計算し、範囲を制限
                                val newSpurGearRadius = (oldSpurGearRadius * zoomFactor)
                                val actualZoomFactor = newSpurGearRadius / oldSpurGearRadius
                                pinionCenterOffset *= actualZoomFactor // ピニオンギアの中心位置もスケールする

                                if (latestCumulativeScale * actualZoomFactor in 0.8f..1.2f) {
                                    onScaleChange(latestCumulativeScale * actualZoomFactor) //拡大率の更新
                                }

                                // 軌跡の各点もスケーリングする
                                locus.forEach { point ->
                                    // カーソル位置を基準に、点の絶対座標をスケーリング
                                    point.position =
                                        cursorAbsolutePosition + (point.position - cursorAbsolutePosition) * actualZoomFactor
                                }

                                // 他のサイズ関連の値もスケール
                                pinionGearRadius *= actualZoomFactor
                                penOffset *= actualZoomFactor

                                // ★カーソル位置がズレないように、スパーギアの中心座標も更新する★
                                spurCenterOffset =
                                    (cursorPosition * (1 - actualZoomFactor)) + (oldSpurCenterOffset * actualZoomFactor)

                                // 半径を更新
                                spurGearRadius = newSpurGearRadius
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
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

            // 中心点の描画
            drawCircle(
                color = Color.Blue,
                radius = 5.0f,
                center = spurCenterOffset + center,
            )

            // ピニオンギアの描画
            drawCircle(
                color = Color.Red,
                radius = pinionGearRadius,
                center = pinionCenterOffset + spurCenterOffset + center,
                style = latestPenSize
            )

            // 中心点の描画
            drawCircle(
                color = Color.Red,
                radius = 5.0f,
                center = pinionCenterOffset + spurCenterOffset + center,
            )

            // ペン先の描画
            drawCircle(
                color = latestColor,
                radius = latestPenSize.width,
                center = penOffset + spurCenterOffset + pinionCenterOffset + center,
            )
        }
    }

    println("scale: $latestCumulativeScale")
}