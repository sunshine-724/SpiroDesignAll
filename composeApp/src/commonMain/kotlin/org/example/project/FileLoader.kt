package org.example.project

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * 特殊な形式のカラーコード文字列（例: "#00-2001E"）をComposeのColorオブジェクトに変換する。
 * @param colorString CSVから読み込んだカラーコード文字列
 * @return パースされたColorオブジェクト
 */
fun parseCustomColorString(colorString: String): Color {
    // 1. '#'や'-'など、16進数でない文字をすべて除去する
    val cleanHex = colorString.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }

    // 2. 8桁（AARRGGBB）になるように、先頭を'0'で埋める
    val paddedHex = cleanHex.padStart(8, '0')

    // 3. 16進数文字列をLongとしてパースし、Intに変換してColorオブジェクトを作成
    // Alpha（透明度）も含まれるARGB形式として扱われる
    return Color(paddedHex.toULong(16).toLong())
}

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
        if (columns.size == 3) {
            try {
                val point = PathPoint(
                    position = Offset(columns[0].trim().toFloat(), columns[1].trim().toFloat()),
                    color = parseCustomColorString(columns[2].trim())
                )
                points.add(point)
            } catch (e: NumberFormatException) {
                println("Warning: Skipping line due to number format error: $line")
            }
        } else {
            println("Warning: Skipping line with incorrect column count: $line")
        }
    }
    return points
}
