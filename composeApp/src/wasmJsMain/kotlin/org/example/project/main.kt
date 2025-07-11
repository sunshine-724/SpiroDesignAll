package org.example.project

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

/**
 * WebAssemblyアプリケーションのメインエントリーポイント
 * Compose Multiplatformを使用してWebブラウザ上でSpiroDesignアプリケーションを起動する
 * ブラウザのbody要素内にComposeのViewportを作成してアプリを表示
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
        ComposeViewport(document.body!!) {
                App()
        }
}