package org.example.project.data.utils

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.example.project.Platform
import org.example.project.data.models.AppState

/**
 * 画像エクスポート処理を行うユーティリティ関数
 * 設定メニューを一度閉じて、キャンバスを保存する
 * キャンバス保存ロジックは各プラットフォームのsaveCanvasAsImageを参照
 *
 * @param scope コルーチンスコープ
 * @param drawerState ドロワーの状態
 * @param onStateChange アプリケーション状態変更コールバック
 * @param platform プラットフォーム情報
 */
fun handleExportAction(
    scope: CoroutineScope,
    drawerState: DrawerState,
    onStateChange: (AppState) -> Unit,
    platform: Platform
) {
    scope.launch {
        onStateChange(AppState(isPlaying = false, isExporting = true))
        drawerState.close()

        snapshotFlow { drawerState.isClosed }
            .filter { isClosed -> isClosed }
            .first()

        yield()

        platform.saveCanvasAsImage("spiroDesign.png")
        onStateChange(AppState(isExporting = false))
        drawerState.open()
    }
}