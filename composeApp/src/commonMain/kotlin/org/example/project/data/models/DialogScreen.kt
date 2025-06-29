package org.example.project.data.models

/** 描画情報を保持するデータクラス */
// ダイアログの画面状態を管理
sealed class DialogScreen {
    object Main : DialogScreen()
    object PenSize : DialogScreen()
}