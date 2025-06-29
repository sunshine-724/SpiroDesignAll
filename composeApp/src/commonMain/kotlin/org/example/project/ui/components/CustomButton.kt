package org.example.project.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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