package org.example.project

// --- ▼▼▼ ワイルドカードで整理したリスト ▼▼▼ ---
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.github.skydoves.colorpicker.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.data.models.AppState
import org.example.project.data.models.DraggingMode
import org.example.project.data.models.DraggingMode.*
import org.example.project.data.models.PathPoint
import org.example.project.data.utils.handleExportAction
import org.example.project.ui.components.DrawingCanvas
import org.example.project.ui.screens.DrawerContent
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.*

// --- ▲▲▲ ここまで ▲▲▲ ---

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
val platform = getPlatform()


/**
 * 全てのアプリケーションにおけるエントリーポイント
 */
@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    // メニュー画面が開いているか閉じているか
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 一番の親スコープを管理
    val scope = rememberCoroutineScope()

    // アプリケーション状態を管理
    var appState by remember {
        mutableStateOf(AppState())
    }

    // DrawingCanvasに直接locusを渡すのではなく、AppStateから取得するように変更
    // val locus = remember { mutableStateListOf<PathPoint>() } // これは不要になるか、AppState.locusを監視する形になる

    /* 入力 */
    // ドラッグしているときの状態を保存する
    var draggingMode by remember { mutableStateOf<DraggingMode>(NONE) }

    // ドラッグ開始の座標を管理する

    // ドラッグの総移動量を管理する
    var totalDragAmount by remember { mutableStateOf(Offset.Zero) }

    // タッチかスロープかを判断する閾値を設定
    val viewConfiguration = LocalViewConfiguration.current
    val touchSlop = viewConfiguration.touchSlop

    // AppStateの変更を処理するコールバック
    val onAppStateChanged: (AppState) -> Unit = { newState ->
        appState = newState
    }

    // 軌跡に点を追加するためのメソッド
    val onLocusAdd: (PathPoint) -> Unit = { newPoint ->
        appState = appState.copy(locus = appState.locus + newPoint)
    }

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        appState = appState,
                        locus = appState.locus.toMutableList(), // AppStateからlocusを渡す
                        onStateChange = onAppStateChanged, // AppState全体の変更を通知
                        onLocusAdd = onLocusAdd, // 軌跡の追加を通知
                        onDisplayClear = {
                            appState = appState.copy(isPlaying = false, locus = emptyList()) // locusもクリア
                        },
                        onDisplayExport = {
                            handleExportAction(
                                scope = scope,
                                drawerState = drawerState,
                                onStateChange = onAppStateChanged, // AppState全体の変更を通知
                                platform = platform
                            )
                        }
                    )
                }
            ) {
                MainContent(
                    appState = appState,
                    onAppStateChanged = onAppStateChanged, // AppState全体の変更を通知
                    onLocusAdd = onLocusAdd, // 軌跡の追加を通知
                    scope = scope,
                    drawerState = drawerState,
                    onDragStart = { clickedPosition ->
                        // clickedPositionはキャンバスの中心からの相対座標として渡されることを想定
                        // PenのmanualPositionを更新
                        // appState.pen.manualPosition = clickedPosition は直接変更できないため、
                        // appStateのコピーを作成し、その中のpenインスタンスを更新
                        if ((clickedPosition - appState.pinionGear.position).getDistance() < appState.pinionGear.radius * 0.9) {
                            onAppStateChanged(appState.copy(
                                pen = appState.pen.copy().apply { manualPosition = clickedPosition }
                            ))
                        }

                        if((clickedPosition - appState.spurGear.position).getDistance() <= 10.0f) {
                            draggingMode = MOVE_SPUR_CENTER
                        }else if(((clickedPosition - appState.spurGear.position).getDistance() - appState.spurGear.radius) <= 10.0f) {
                            draggingMode = RESIZE_SPUR_RADIUS
                        }else if((clickedPosition - appState.pinionGear.position).getDistance() <= 10.0f) {
                            draggingMode = RESIZE_PINION_RADIUS_AND_MOVE_CENTER
                        }else if((clickedPosition - appState.currentPenDrawingPosition).getDistance() <= 10.0f) { // Penの位置はcurrentPenDrawingPositionを参照
                            // PenのドラッグはDrawingCanvas内で処理されるため、ここではNONEに設定
                            draggingMode = NONE
                        }else{
                            draggingMode = PAN
                        }
                    },
                    onDrag = { currentPosition ->
                        val previousTotalDragAmount = totalDragAmount.copy()
                        totalDragAmount += currentPosition

                        val variationAmount = totalDragAmount - previousTotalDragAmount

                        println("変化量は ${variationAmount}です")

                        when (draggingMode) {
                            NONE -> {
                                println("モードなし")
                            }
                            MOVE_SPUR_CENTER -> {
                                println("モード${MOVE_SPUR_CENTER}")
                                // TODO: SpurGearのpositionを更新するロジック
                                // appState.copy(spurGear = appState.spurGear.copy(position = appState.spurGear.position + variationAmount))
                            }
                            RESIZE_SPUR_RADIUS -> {
                                println("<UNK>${RESIZE_SPUR_RADIUS}")
                                // TODO: SpurGearのradiusを更新するロジック
                            }
                            RESIZE_PINION_RADIUS_AND_MOVE_CENTER -> {
                                println("<${RESIZE_PINION_RADIUS_AND_MOVE_CENTER}")
                                // TODO: PinionGearのradiusとpositionを更新するロジック
                            }
                            PAN -> {
                                println("<UNK>${PAN}")
                                // TODO: 全体のCanvasオフセットを更新するロジック
                            }

                            MOVE_PEN -> {
                                // これはタップで処理するので何もしない (DrawingCanvas内のdetectDragGesturesで処理)
                                println("<UNK>${MOVE_PEN}")
                            }
                        }
                    },
                    onDragEnd = {
                        // 閾値より小さければタップとみなす
                        if(totalDragAmount.getDistance() < touchSlop){
                            println("on Tap")
                            // タップ時のペンの位置リセットなど、追加のロジックが必要な場合はここに記述
                            // appState.pen.resetManualPosition() の呼び出しなど
                        }
                        draggingMode = NONE
                        totalDragAmount = Offset.Zero // ドラッグ量をリセット
                        println("drag end")
                    },
                    onDragCancel = {
                        draggingMode = NONE
                        totalDragAmount = Offset.Zero // ドラッグ量をリセット
                        println("drag cancel")
                    }
                )
            }
        }
    }
}

/**
 * メイン画面を統括します
 * Canvasは常時表示され、UIに関してはAppStateに応じて変化します
 *
 * @param appState アプリケーション状態と画面の状態を管理します
 * @param onAppStateChanged AppStateの変更を通知するためのコールバックです
 * @param onLocusAdd 軌跡に点を追加するためのメソッドです
 * @param scope 親のスコープを引き継ぐことで、メモリの効率化を図ります
 * @param drawerState 左側に表示させるドロワーの状態を管理します
 * @param onDragStart ドラッグがスタートした時に呼び出されるコールバックメソッドです
 * @param onDrag ドラッグ中に呼び出されます
 * @param onDragEnd ドラッグが終了した時に呼び出されるコールバックメソッドです
 * @param onDragCancel ドラッグがキャンセルした時に呼び出されるコールバックメソッドです
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainContent(
    appState: AppState,
    onAppStateChanged: (AppState) -> Unit, // AppStateの変更を通知
    onLocusAdd: (PathPoint) -> Unit, // 軌跡の追加を通知
    scope: CoroutineScope,
    drawerState: DrawerState,
    onDragStart: (Offset) -> Unit = { /* no-op */ },
    onDrag: (Offset) -> Unit = { /* no-op */ },
    onDragEnd: () -> Unit = { /* no-op */ },
    onDragCancel: () -> Unit = { /* no-op */ }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> onDragStart(offset) },
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume() //ここでイベントを消費することによって子要素にイベントを伝えないようにする
                        onDrag(dragAmount)
                    },
                    onDragCancel = { onDragCancel() }
                )
            }
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Secondary),
                onClick = {
                    // 右クリックでドロワーを開く
                    scope.launch { drawerState.open() }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        DrawingCanvas(
            appState = appState, // AppState全体を渡す
            onAppStateChanged = onAppStateChanged, // AppStateの変更を通知するコールバックを渡す
            onAddPoint = onLocusAdd // 軌跡の追加を通知するコールバックを渡す
        )
    }
}

@Composable
fun DragInfo(position: Offset, color: Color, radius: Float) {
    // TODO: Not yet implemented
}
