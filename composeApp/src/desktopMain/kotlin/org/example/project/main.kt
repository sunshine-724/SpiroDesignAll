package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * デスクトップアプリケーションのメインエントリーポイント
 * Compose Multiplatformを使用してデスクトップウィンドウを作成し、
 * SpiroDesignアプリケーションを起動する
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Greeting App (Desktop)"
    ) {
        App()
    }
}