package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import org.example.project.AppTheme

// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

@Composable
fun App() {
    //remember修飾子:再描画した際、かかっているデータ構造が初期化されない(逆に言うとつけてないと初期化される)
    //mutableStateListOf<Offset>:リストに何かしらの操作があると、@Composableアノテーションが発火する
    val circles = remember { mutableStateListOf<Offset>() }

    AppTheme {
        //pointerInput:(マウスや指)の入力を受け取るためのModifier
        //detectDragGestures:ドラッグ操作を検知する
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        circles.add(change.position)
                    }
                }
        ) {
            //Canvasの再描画が起きるたび、発火する
            circles.forEach { position ->
                drawCircle(color = Color.Blue, radius = 20f, center = position)
            }
        }
    }
}