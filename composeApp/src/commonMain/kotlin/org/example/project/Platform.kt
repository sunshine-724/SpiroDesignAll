package org.example.project

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.example.project.data.models.DeviceType

interface Platform{
    val name: String
    fun showGreeting(name: String)
    fun saveTextToFile(content: String, defaultFileName: String)

    /**
     * プラットフォーム固有のファイルピッカーを開き、ユーザーが選択したファイルの内容をテキストとして返すことを期待する関数。
     * この関数は中断（suspend）可能で、ユーザーがファイルを選択するまで待機します。
     *
     * @param allowedFileExtensions ユーザーに選択を許可するファイルの拡張子リスト（例: listOf(".csv", ".txt")）
     * @return 選択されたファイルのテキスト内容。ユーザーがキャンセルした場合はnull。
     */
    suspend fun openFileAndReadText(allowedFileExtensions: List<String> = listOf()): String?

    /**
     * 現在表示されているCanvasの内容を画像として保存する
     * @param defaultFileName 保存する画像のデフォルトファイル名。
     */
    suspend fun saveCanvasAsImage(defaultFileName: String)

    /**
     * 現在動かしているプラットフォームを返します
     *
     * @return
     */
    fun getDeviceType(): DeviceType

    /*debug*/
//    /**
//     * キャンバスの状態をデバッグする
//     */
//    suspend fun debugCanvas()
}

expect fun getPlatform(): Platform