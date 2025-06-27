package org.example.project

// import androidx.compose.animation.core.Animatable
// import androidx.compose.animation.core.LinearEasing
// import androidx.compose.animation.core.tween
// import androidx.compose.foundation.Canvas
// import androidx.compose.foundation.ExperimentalFoundationApi
// import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.onClick
// import androidx.compose.foundation.PointerMatcher
// import androidx.compose.foundation.background
// import androidx.compose.ui.input.pointer.PointerButton
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material3.BasicAlertDialog
// import androidx.compose.material3.Button
// import androidx.compose.material3.ButtonDefaults
// import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Slider
// import androidx.compose.material3.Surface
// import androidx.compose.material3.Text
// import androidx.compose.material3.TextField
// import androidx.compose.runtime.*
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.unit.dp
// import androidx.compose.runtime.Composable
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.geometry.Offset
// import androidx.compose.ui.geometry.Size
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.StrokeCap
// import androidx.compose.ui.graphics.drawscope.Stroke
// import androidx.compose.ui.graphics.toArgb
// import androidx.compose.ui.input.pointer.pointerInput
// import androidx.compose.ui.layout.onSizeChanged
// import androidx.compose.ui.platform.LocalDensity
// import androidx.compose.ui.unit.sp
// import androidx.compose.ui.unit.toSize
// import com.github.skydoves.colorpicker.compose.ColorEnvelope
// import com.github.skydoves.colorpicker.compose.ColorPickerController
// import com.github.skydoves.colorpicker.compose.HsvColorPicker
// import com.github.skydoves.colorpicker.compose.PaletteContentScale
// import com.github.skydoves.colorpicker.compose.rememberColorPickerController
// import org.jetbrains.compose.ui.tooling.preview.Preview
// import kotlin.math.roundToInt
// import kotlinx.coroutines.delay
// import kotlin.math.cos
// import kotlin.math.sin

// --- ▼▼▼ ワイルドカードで整理したリスト ▼▼▼ ---
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalNavigationDrawer
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
import kotlin.math.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

// --- ▲▲▲ ここまで ▲▲▲ ---

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

/** 描画情報を保持するデータクラス */

// ダイアログの画面状態を管理
sealed class DialogScreen {
    object Main : DialogScreen()
    object PenSize : DialogScreen()
}

// DrawingCanvasの上など、関数の外に定義します
data class PathPoint(
    val position: Offset,
    val color: Color
)

// カスタムボタンのComposable関数を定義
@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White, // カスタムカラー(背景色)
            contentColor = Color.Black // カスタムカラー(文字色)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp, // 通常時の影の高さ
            pressedElevation = 4.dp, // 押下時の影の高さ
            disabledElevation = 0.dp // 無効時の影の高さ
        ),
    ) { Text(text) }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<DialogScreen>(DialogScreen.Main) } //現在の画面
    var penRadius by remember { mutableStateOf(10f) } // ペンの太さ
    var spurSpeed by remember { mutableStateOf(1f) } // スパーギアのスピード
    var currentColor by remember { mutableStateOf(Color.Black) } //現在の軌跡の色
    var isPlaying by remember { mutableStateOf(false) } // startかstopかのフラグ

    val locus = remember { mutableStateListOf<PathPoint>() } //軌跡

    AppTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // --- ▼▼▼ ドロワーの中身（以前のダイアログの中身がここに来る） ▼▼▼ ---
                ModalDrawerSheet {
                    Surface(
                        modifier = Modifier
                            .width(300.dp) // ドロワーの幅を指定
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        when (currentScreen) {
                            is DialogScreen.Main -> {
                                MainScreenContent(
                                    onNavigateToPenSize = {
                                        currentScreen = DialogScreen.PenSize
                                    },
                                    currentSpeed = spurSpeed,
                                    currentColor = currentColor,
                                    onSpeedChange = { newSpeed ->
                                        spurSpeed = newSpeed
                                    },
                                    onColorChange = { newColor ->
                                        currentColor = newColor
                                    },
                                    onPlayingChange = { newIsPlaying ->
                                        isPlaying = newIsPlaying
                                    },
                                    onDisplayClear = {
                                        isPlaying = false
                                        locus.clear()
                                    }
                                )
                            }
                            is DialogScreen.PenSize -> {
                                PenSizeScreenContent(
                                    currentRadius = penRadius,
                                    onPenSizeChange = { newPenSize ->
                                        isPlaying = false
                                        penRadius = newPenSize
                                    },
                                    onNavigateBack = {
                                        currentScreen = DialogScreen.Main
                                    }
                                )
                            }
                            else -> {}
                        }
                    }
                }
                // --- ▲▲▲ ドロワーの中身 ▲▲▲ ---
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    color = currentColor,
                    speed = spurSpeed,
                    isPlaying = isPlaying,
                    locus = locus,
                    penSize = Stroke(penRadius),
                    onAddPoint = { newPoint ->
                        locus.add(newPoint)
                    }
                )
            }
        }
    }
}

@Composable
fun DrawingCanvas(
    color: Color,
    speed : Float,
    locus : List<PathPoint>,
    penSize : Stroke,
    isPlaying : Boolean,
    onAddPoint: (PathPoint) -> Unit
) {
    val spurGearRadius = 300f            // 固定円（大きい円）の基本半径
    val pinionGearRadius = 50f           // 回転する円（ピニオンギア）の基本半径

    val spurGearStroke = Stroke(20f) // スパーギアのストローク
    val pinionGearOrPenStroke = penSize // ピニオンギアのストローク & ペン先の直径

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
        val effectivePinionGearRadius = pinionGearRadius + pinionGearOrPenStroke.width / 2f
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

                val newPathPoint = PathPoint(position = penOffset, color = latestColor)
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
        println("Speed: $speed")

        // 軌跡の描画（Pathを使うことで滑らかに）
//        locus.forEach { pathPoint ->
//            drawCircle(
//                color = pathPoint.color, // ★ リストに保存された色を使う
//                radius = penRadius,
//                center = canvasCenter + pathPoint.position // リストに保存された座標を使う
//            )
//            println(center)
//        }
        // --- 軌跡の描画 (より連続的に) ---
        if (locus.size > 1) {
            for (i in 1 until locus.size) {
                val prevPoint = locus[i - 1]
                val currentPoint = locus[i]

                // 前の点から現在の点まで、前の点の色で短い線を描画する
                drawLine(
                    color = prevPoint.color,
                    start = canvasCenter + prevPoint.position,
                    end = canvasCenter + currentPoint.position,
                    strokeWidth = pinionGearOrPenStroke.width,
                    cap = StrokeCap.Round
                )
            }
        }

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
            style = pinionGearOrPenStroke
        )

        // ペン先の描画
        drawCircle(
            color = latestColor,
            radius = pinionGearOrPenStroke.width,
            center = canvasCenter + penOffset
        )
    }
}

// メイン画面のコンテンツ
@Composable
private fun MainScreenContent(
    onNavigateToPenSize: () -> Unit,
    currentSpeed: Float,
    currentColor: Color,
    onSpeedChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit,
    onPlayingChange: (Boolean) -> Unit,
    onDisplayClear: () -> Unit
) {
    val controller = rememberColorPickerController()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxHeight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomButton("Start") {
            onPlayingChange(true)
        }
        CustomButton("Stop") {
            onPlayingChange(false)
        }
        CustomButton("Clear") {
            onDisplayClear()
        }
        CustomButton("Save") {}
        CustomButton("Load") {}
        CustomButton("Export") {}
        CustomButton("pensize") {
            // クリックされたら、渡された関数を呼び出して画面遷移を依頼する
            onNavigateToPenSize()
        }

        // 小数第2位まで表示
        Text(text = "Current Speed: ${(currentSpeed * 100).roundToInt() / 100.0}", fontSize = 16.sp)

        Slider(value = currentSpeed, onValueChange = { newSpeed -> onSpeedChange(newSpeed) }, valueRange = 0.1f..5f)
        // カラー設定
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Current Color", fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .size(80.dp) // 四角のサイズ
                        .background(currentColor) // ★ ここで現在の色を適用
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Choose Color",
                    fontSize = 18.sp // お好みのフォントサイズに
                )

                HsvColorPicker(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        // do something
                        onColorChange(colorEnvelope.color)
                    },
                    initialColor = currentColor,
                )
            }
        }
    }
}

@Composable
private fun PenSizeScreenContent(
    currentRadius: Float,
    onPenSizeChange: (Float) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // 要素間のスペース
    ) {
        Text("ペンの太さを調整", fontSize = 20.sp)
        Text("現在の太さ: ${currentRadius.toInt()}", fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CustomButton("Small") { onPenSizeChange(5f) }
            CustomButton("Medium") { onPenSizeChange(10f) }
            CustomButton("Large") { onPenSizeChange(20f) }
        }
        CustomButton("戻る") {
            // クリックされたら、渡された関数を呼び出して戻る
            onNavigateBack()
        }
    }
}

@Composable
fun DragInfo(position: Offset, color: Color, radius: Float) {
    TODO("Not yet implemented")
}
