package org.example.project

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.example.project.data.models.DeviceType

/**
 * プラットフォーム固有の機能を提供するインターフェース
 * 各プラットフォーム（Android、iOS、Desktop、Web）で異なる実装を持つ
 */
interface Platform{
    /**
     * プラットフォーム名
     */
    val name: String
    
    /**
     * 挨拶メッセージを表示する
     * 
     * @param name 挨拶に使用する名前
     */
    fun showGreeting(name: String)
    
    /**
     * テキストをファイルに保存する
     * 
     * @param content 保存するテキスト内容
     * @param defaultFileName デフォルトのファイル名
     */
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
     * 現在動かしているプラットフォームのデバイスタイプを返す
     *
     * @return デバイスタイプ（ANDROID、IOS、DESKTOP、WEB）
     */
    fun getDeviceType(): DeviceType

    /*debug*/
//    /**
//     * キャンバスの状態をデバッグする
//     */
//    suspend fun debugCanvas()
}

/**
 * プラットフォーム固有のPlatformインスタンスを取得する
 * 
 * @return 現在実行中のプラットフォームに対応するPlatformインスタンス
 */
expect fun getPlatform(): Platform