package org.example.project.data.models

/**
 * ダイアログの画面状態を管理するSealed Class
 * アプリケーション内で表示可能な画面の種類を定義する
 */
sealed class DialogScreen {
    /**
     * メイン画面
     */
    object Main : DialogScreen()
    
    /**
     * ペンサイズ設定画面
     */
    object PenSize : DialogScreen()
}