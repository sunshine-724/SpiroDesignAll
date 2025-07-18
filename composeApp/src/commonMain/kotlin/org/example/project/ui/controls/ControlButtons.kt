package org.example.project.ui.controls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.example.project.data.models.PathPoint
import org.example.project.data.utils.parseCsv
import org.example.project.platform
import org.example.project.ui.components.CustomButton

/**
 * Control buttons
 *
 * @param isPlaying
 * @param locus
 * @param onPlayingChange
 * @param onDisplayClear
 * @param onDisplayExport
 * @param onLocusAdd
 * @param loadedDataInfo
 * @param onLoadedDataInfoChange
 * @receiver
 * @receiver
 * @receiver
 * @receiver
 * @receiver
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
    val scope = rememberCoroutineScope()

    CustomButton("Start") { onPlayingChange(true) }
    CustomButton("Stop") { onPlayingChange(false) }
    CustomButton("Clear") { onDisplayClear() }

    CustomButton("Save") {
        if (locus.isNotEmpty()) {
            onPlayingChange(false)
            val csvHeader = "x,y,thickness,color_hex\n"
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
            val fileContent = platform.openFileAndReadText(listOf(".csv"))

            if (fileContent != null) {
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