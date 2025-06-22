package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults.color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var showDialog by remember { mutableStateOf(false) }
    
    AppTheme { 
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Button(onClick = { 
                showDialog = true 
            }) {
                Text("Show Dialog")
            }
        }
        
        if(showDialog){
            BasicAlertDialog(
                onDismissRequest = { showDialog = false }
            ){
                Surface(
                    modifier = Modifier
                        .width(300.dp) //幅を決める
                        .wrapContentHeight(), //高さは自動調整
                        // または
                        // .size(width = 300.dp, height = 200.dp)  // 幅と高さを固定
                        // または
                        // .fillMaxWidth(0.8f)  // 画面幅の80%
                        // または
                        // .sizeIn(minWidth = 200.dp, maxWidth = 300.dp)  // 最小・最大サイズを指定
                    color = Color.LightGray,
                    shape = RoundedCornerShape(16.dp) //丸みをつける
                ){
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, //これ以下の要素を水平中央寄せ
                    ){
                        Text("これはCompose Multiplatformで表示されたダイアログです。")
                        Button(
                            onClick = { showDialog = false } ,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),  // カスタムカラー(背景色)
                                contentColor = Color(0xFFFFFFFF)     // カスタムカラー(文字色)
                            )
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DragInfo(position: Offset, color: Color, radius: Float) {
    TODO("Not yet implemented")
}