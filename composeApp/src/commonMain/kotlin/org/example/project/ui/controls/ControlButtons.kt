package org.example.project.ui.controls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.example.project.data.models.PathPoint
import org.example.project.data.utils.parseCsv
import org.example.project.platform
import org.example.project.ui.components.CustomButton

/**
 * コントロールボタンを表示するComposable関数
 * スピログラフアプリの操作に必要なボタン群（Start、Stop、Clear、Save、Load、Export）を提供する
 *
 * @param isPlaying アニメーションが再生中かどうか
 * @param locus スピログラフの軌跡データリスト
 * @param onPlayingChange 再生状態変更のコールバック
 * @param onDisplayClear 画面クリアのコールバック
 * @param onDisplayExport 画像エクスポートのコールバック
 * @param onLocusAdd 軌跡データ追加のコールバック
 * @param loadedDataInfo ロードされたデータの情報文字列
 * @param onLoadedDataInfoChange データ情報変更のコールバック
 */
@Composable
public fun ControlButtons(
    isPlaying: Boolean,
    locus: List<PathPoint>,
    onPlayingChange: (Boolean) -> Unit,
    onDisplayClear: () -> Unit,
    onDisplayExport: () -> Unit,
    onLocusAdd: (PathPoint) -> Unit,
    loadedDataInfo: String,
    onLoadedDataInfoChange: (String) -> Unit
) {
    /**
     * Composableスコープ内でコルーチンを実行するためのスコープ
     */
    val scope = rememberCoroutineScope()

    CustomButton("Start") { onPlayingChange(true) }
    CustomButton("Stop") { onPlayingChange(false) }
    CustomButton("Clear") { onDisplayClear() }

    CustomButton("Save") {
        if (locus.isNotEmpty()) {
            onPlayingChange(false)
            /**
             * CSVファイルのヘッダー行
             */
            val csvHeader = "x,y,thickness,color_hex\n"
            
            /**
             * 軌跡データをCSV形式の行に変換
             */
            val csvRows = locus.joinToString(separator = "\n") { pathPoint ->
                val x = pathPoint.position.x
                val y = pathPoint.position.y
                val thickness = pathPoint.thickness
                val r = pathPoint.color.red
                val g = pathPoint.color.green
                val b = pathPoint.color.blue
                "$x,$y,$thickness,$r,$g,$b"
            }
            platform.saveTextToFile(csvHeader + csvRows, "locus_data.csv")
        }
        println("locus.size: ${locus.size}件のデータをセーブしました")
    }

    CustomButton("Load") {
        onPlayingChange(false)
        scope.launch {
            println("読み込み中...")
            /**
             * ファイル選択ダイアログから読み込んだCSVファイルの内容
             */
            val fileContent = platform.openFileAndReadText(listOf(".csv"))

            if (fileContent != null) {
                /**
                 * CSVファイルをパースして軌跡データのリストに変換
                 */
                val pathPoints = parseCsv(fileContent)
                if(pathPoints.isNotEmpty()){
                    pathPoints.forEach { pathPoint ->
                        onLocusAdd(pathPoint)
                    }
                }
                println("${pathPoints.size} 件のデータをロードしました。")
            } else {
                println("ファイルの選択がキャンセルされました。")
            }
        }
    }

    CustomButton("Export") {
        onDisplayExport()
        println("画像を出力しました")
    }
}