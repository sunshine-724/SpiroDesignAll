package org.example.project

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.PointerMatcher
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

/**
 * 描画情報を保持するデータクラス
 */

// ダイアログの画面状態を管理
sealed class DialogScreen {
    object Hidden : DialogScreen()
    object Main : DialogScreen()
    object PenSize : DialogScreen()
}

// カスタムボタンのComposable関数を定義
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,  // カスタムカラー(背景色)
            contentColor = Color.Black     // カスタムカラー(文字色)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,      // 通常時の影の高さ
            pressedElevation = 4.dp,      // 押下時の影の高さ
            disabledElevation = 0.dp      // 無効時の影の高さ
        ),
    ) {
        Text(text)
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<DialogScreen>(DialogScreen.Hidden) }
    var penRadius by remember { mutableStateOf(20f) }
    
    AppTheme { 
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onClick(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onClick = { currentScreen = DialogScreen.Main }
                ),
            contentAlignment = Alignment.Center
        ){
            Text("画面のどこでも右クリックしてメニューを開きます")
        }
        
        if(currentScreen != DialogScreen.Hidden){
            BasicAlertDialog(
                onDismissRequest = { currentScreen = DialogScreen.Hidden }
            ){
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.8f) //幅を決める
                        .fillMaxHeight(0.8f), // 高さを決める
                        // または
                        // .size(width = 300.dp, height = 200.dp)  // 幅と高さを固定
                        // または
                        // .fillMaxWidth(0.8f)  // 画面幅の80%
                        // または
                        // .sizeIn(minWidth = 200.dp, maxWidth = 300.dp)  // 最小・最大サイズを指定
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp) //丸みをつける
                ){
                    when (currentScreen) {
                        is DialogScreen.Main -> {
                            MainScreenContent(
                                onNavigateToPenSize = { currentScreen = DialogScreen.PenSize }
                            )
                        }
                        is DialogScreen.PenSize -> {
                            PenSizeScreenContent(
                                currentRadius = penRadius,
                                onRadiusChange = { newRadius -> penRadius = newRadius},
                                onNavigateBack = { currentScreen = DialogScreen.Main }
                            )
                        }
                        else -> {
                            //Hiddenの場合
                        }
                    }
                }
            }
        }
    }
}



// メイン画面のコンテンツ
@Composable
private fun MainScreenContent(onNavigateToPenSize: () -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CustomButton("Start") { }
        CustomButton("Stop") { }
        CustomButton("Clear") { }
        CustomButton("Save") { }
        CustomButton("Load") { }
        CustomButton("pensize") {
            // クリックされたら、渡された関数を呼び出して画面遷移を依頼する
            onNavigateToPenSize()
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
        verticalArrangement = Arrangement.spacedBy(8.dp) //要素間のスペース
    ) {
        Text("ペンの太さを調整", fontSize = 20.sp)
        Text("現在の太さ: ${currentRadius.toInt()}", fontSize = 16.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ){
            CustomButton("Small") { onRadiusChange(5f) }
            CustomButton("Medium") { onRadiusChange(10f) }
            CustomButton("Large") { onRadiusChange(20f) }
        }
//        Slider(
//            value = currentRadius,
//            onValueChange = onRadiusChange,
//            valueRange = 5f..50f
//        )
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