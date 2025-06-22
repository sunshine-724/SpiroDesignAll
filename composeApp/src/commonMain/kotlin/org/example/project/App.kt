package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.example.project.AppTheme


// アプリ起動時に一度だけプラットフォーム固有の実装を取得する
private val platform = getPlatform()

@Composable
fun App() {
    AppTheme {
        // TextFieldの入力値を保持するためのState
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color.Red,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                strokeWidth = 5f
            )
        }
    }
}