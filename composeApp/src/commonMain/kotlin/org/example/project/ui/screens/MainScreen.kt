package org.example.project.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import org.example.project.data.models.AppState
import org.example.project.data.models.DialogScreen
import org.example.project.data.models.PathPoint
import org.example.project.ui.components.ColorControl
import org.example.project.ui.components.CustomButton
import org.example.project.ui.controls.ControlButtons
import org.example.project.ui.controls.SpeedControl

/**
 * メイン画面のコンテンツを表示するComposable関数
 * スピログラフアプリの設定UI（コントロールボタン、スピード調整、色選択等）を提供する
 *
 * @param appState アプリケーション状態
 * @param locus 軌跡データリスト
 * @param onStateChange アプリケーション状態変更コールバック
 * @param onLocusAdd 軌跡データ追加コールバック
 * @param onDisplayClear 画面クリアコールバック
 * @param onDisplayExport 画像エクスポートコールバック
 */
@Composable
fun MainScreenContent(
    appState: AppState,
    locus: List<PathPoint>,
    onStateChange: (AppState) -> Unit,
    onLocusAdd: (PathPoint) -> Unit,
    onDisplayClear: () -> Unit,
    onDisplayExport: () -> Unit
) {
    /**
     * カラーピッカーのコントローラー
     */
    val controller = rememberColorPickerController()
    
    /**
     * ロードされたデータの情報を表示する文字列
     */
    var loadedDataInfo by remember { mutableStateOf("CSVファイルがロードされていません。") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp).fillMaxHeight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 制御ボタン群
        ControlButtons(
            isPlaying = appState.isPlaying,
            locus = locus,
            onPlayingChange = { isPlaying ->
                onStateChange(appState.copy(isPlaying = isPlaying))
            },
            onDisplayClear = onDisplayClear,
            onDisplayExport = onDisplayExport,
            onLocusAdd = onLocusAdd,
            loadedDataInfo = loadedDataInfo,
            onLoadedDataInfoChange = { info -> loadedDataInfo = info }
        )

        CustomButton("pensize") {
            onStateChange(appState.copy(currentScreen = DialogScreen.PenSize))
        }

        // スピード設定
        SpeedControl(
            currentSpeed = appState.spurSpeed,
            onSpeedChange = { newSpeed ->
                onStateChange(appState.copy(spurSpeed = newSpeed))
            }
        )

        // カラー設定
        ColorControl(
            currentColor = appState.currentColor,
            controller = controller,
            onColorChange = { newColor ->
                onStateChange(appState.copy(currentColor = newColor))
            }
        )
    }
}