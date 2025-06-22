package org.example.project

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    // commonMainで定義したAppThemeで囲む
    AppTheme {
        App()
    }
}
