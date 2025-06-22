package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

/**
 * 描画情報を保持するデータクラス
 */

data class DrawInfo(
    val position: Offset,
    val color: Color,
    val radius: Float
)

@Composable
fun App() {
    val drawnPoints = remember { mutableStateListOf<DrawInfo>() }

    // 現在選択されている色と太さ
    // val:再代入不可 var:再代入可能
    // ただし、valで宣言されたオブジェクトの中身は変更可能(ex. val list = mutableListOf(1, 2, 3))
    var currentColor by remember { mutableStateOf(Color.Blue) }
    var currentRadius by remember { mutableStateOf(20f) }

    //利用可能な色の種類
    val availableColors = listOf(Color.Black, Color.Blue, Color.Red, Color.Green)

    AppTheme {
        Column(modifier = Modifier.fillMaxSize()) {

            // コントロールパネル
            // sp:文字サイズ dp:レイアウトのサイズ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp)
            ){
                Text("Color",fontSize = 16.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    availableColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(color)
                                .clickable{ currentColor = color } //クリックで色を更新
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Radius: ${currentRadius.toInt()}",fontSize = 16.sp)
            Slider(
                value = currentRadius,
                onValueChange = { newRadius ->
                    currentRadius = newRadius
                },
                valueRange = 5f..50f //スライダーの範囲
            )

            // 描画キャンバス
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f) //残りの領域全て使用する
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val newPoint: DrawInfo = DrawInfo(
                                position = change.position,
                                color = currentColor,
                                radius = currentRadius
                            )
                            drawnPoints.add(newPoint)
                        }
                    }
            ) {
                drawnPoints.forEach { info ->
                    drawCircle(
                        color = info.color,
                        radius = info.radius,
                        center = info.position,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Composable
fun DragInfo(position: Offset, color: Color, radius: Float) {
    TODO("Not yet implemented")
}