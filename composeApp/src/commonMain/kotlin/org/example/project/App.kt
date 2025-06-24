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

    var currentScreen by remember { mutableStateOf<DialogScreen>(DialogScreen.Main) }
    var penRadius by remember { mutableStateOf(20f) }
    var spurSpeed by remember { mutableStateOf(1f) }
    var currentColor by remember { mutableStateOf(Color.Black) }

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
                                    // 以下は仮のパラメータ
                                    currentSpeed = spurSpeed,
                                    currentColor = currentColor,
                                    onSpeedChange = { newSpeed ->
                                        spurSpeed = newSpeed
                                    },
                                    onColorChange = { newColor ->
                                        currentColor = newColor
                                    }
                                )
                            }
                            is DialogScreen.PenSize -> {
                                PenSizeScreenContent(
                                    currentRadius = penRadius,
                                    onRadiusChange = { newRadius ->
                                        penRadius = newRadius
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
                DrawingCanvas(color = currentColor)
            }
        }
    }
}

@Composable
fun DrawingCanvas(
    color: Color
) {
    // 軌跡のリスト
    val points = remember { mutableStateListOf<Offset>() }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(canvasSize) {
        if (canvasSize == Size.Zero) return@LaunchedEffect

        var time = 0f
        val radius = 200f

        // キャンバスのサイズから中心座標を取得する
        val centerX = canvasSize.width / 2f
        val centerY = canvasSize.height / 2f

        // Update
        while (true) {
            val x = centerX + radius * cos(time)
            val y = centerY + radius * sin(time)

            val newPoint = Offset(x, y)

            points.add(newPoint)

            time += 0.05f

            delay(16L) // 遅延を挟むことで、無限ループをUpdateに見せかける
        }
    }

    // 描画
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { intSize ->
                canvasSize = intSize.toSize()
            }
    ) { 
        points.forEach { point -> 
            drawCircle(color = color, radius = 5f, center = point) 
        } 
    }
}

// メイン画面のコンテンツ
@Composable
private fun MainScreenContent(
    onNavigateToPenSize: () -> Unit,
    currentSpeed: Float,
    currentColor: Color,
    onSpeedChange: (Float) -> Unit,
    onColorChange: (Color) -> Unit
) {
    val controller = rememberColorPickerController()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxHeight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomButton("Start") {}
        CustomButton("Stop") {}
        CustomButton("Clear") {}
        CustomButton("Save") {}
        CustomButton("Load") {}
        CustomButton("pensize") {
            // クリックされたら、渡された関数を呼び出して画面遷移を依頼する
            onNavigateToPenSize()
        }

        // 小数第2位まで表示
        Text(text = "Current Speed: ${(currentSpeed * 100).roundToInt() / 100.0}", fontSize = 16.sp)

        Slider(value = currentSpeed, onValueChange = onSpeedChange, valueRange = 0f..10f)

        // カラー設定
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope: ColorEnvelope ->
                        // do something
                        onColorChange(colorEnvelope.color)
                    }
                )
            }
        }
    }
}

@Composable
private fun PenSizeScreenContent(
    currentRadius: Float,
    onRadiusChange: (Float) -> Unit,
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
            CustomButton("Small") { onRadiusChange(5f) }
            CustomButton("Medium") { onRadiusChange(10f) }
            CustomButton("Large") { onRadiusChange(20f) }
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
