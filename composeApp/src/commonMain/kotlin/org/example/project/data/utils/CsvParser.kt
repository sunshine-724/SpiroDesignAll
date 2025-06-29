package org.example.project.data.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.example.project.data.models.PathPoint

/**
 * CSV文字列をパースして、PathPointのリストに変換する共有ロジック。
 * @param csvContent 読み込んだCSVファイルの全内容
 * @return パースされたPathPointオブジェクトのリスト
 */
fun parseCsv(csvContent: String): List<PathPoint> {
    val points = mutableListOf<PathPoint>()
    // ヘッダー行を除外し、空行も無視する
    val lines = csvContent.lines().drop(1).filter { it.isNotBlank() }

    for (line in lines) {
        val columns = line.split(',')
        // 列の数が5 (x, y, r, g, b) であることを確認
        if (columns.size == 6) {
            try {
                val point = PathPoint(
                    position = Offset(columns[0].trim().toFloat(), columns[1].trim().toFloat()),
                    thickness = columns[2].trim().toFloat(),
                    // 3つの列からR,G,Bを読み取り、Colorオブジェクトを直接生成
                    // alpha（透明度）は自動的に1.0f（不透明）になります
                    color = Color(
                        red = columns[3].trim().toFloat(),
                        green = columns[4].trim().toFloat(),
                        blue = columns[5].trim().toFloat()
                    ),
                )
                points.add(point)
            } catch (e: NumberFormatException) {
                println("Warning: Skipping line due to number format error: $line")
            }
        } else {
            println("Warning: Skipping line with incorrect column count: $line (expected 5 columns)")
        }
    }
    return points
}