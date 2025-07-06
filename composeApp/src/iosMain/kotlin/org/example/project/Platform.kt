// iosMain/kotlin/org/example/project/Platform.kt
package org.example.project

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.data.models.DeviceType
import platform.Foundation.*
import platform.Photos.*
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.UIKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText

class IOSPlatform: Platform {
    override val name: String = "iOS"

    /**
     * iPhoneでUIを操作する時のデリゲートを保持します
     * iOSのUIは基本的にViewControllerをデリゲートで保持します
     */
    private var documentPickerDelegate: DocumentPickerDelegate? = null // GCされないようにフィールドでデリゲートを保持
    private var currentOpenDocumentPickerDelegate: OpenFilePickerDelegate? = null // GCされないようにフィールドでデリゲートを保持

    /**
     * 各プラットフォームでのUIを用いて挨拶(デバイス名)を返します
     *
     * @param name 名前
     */
    override fun showGreeting(name: String) {
        // 簡単なアラート表示には UIAlertController を使用
        val alert = UIAlertController.alertControllerWithTitle(
            "Hello",
            message = "Hello, $name from iOS",
            preferredStyle = UIAlertControllerStyleAlert
        )
        alert.addAction(UIAlertAction.actionWithTitle("OK", style = UIAlertActionStyleDefault, handler = null))

        // 最前面のビューコントローラにアラートを表示
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            alert,
            animated = true,
            completion = null
        )
    }

    /**
     * 軌跡のデータ(csvファイル)を書き出します
     *
     * @param content csv形式の文字列です
     * @param defaultFileName ファイルの名前です
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun saveTextToFile(content: String, defaultFileName: String) {
        // メインスレッドでUI操作を行う
        Dispatchers.Main.immediate.run {
            val temporaryDirectory = NSTemporaryDirectory()
            val temporaryFileName = NSUUID().UUIDString() + ".tmp.csv" // 一時ファイル名
            val temporaryFilePath = temporaryDirectory + "/" + temporaryFileName
            val temporaryFileURL = NSURL.fileURLWithPath(temporaryFilePath)

            val nsContent = content as NSString
            var error: CPointer<ObjCObjectVar<NSError?>>? = null

            // 一時ファイルにコンテンツを書き込む
            if (!nsContent.writeToURL(
                    temporaryFileURL,
                    atomically = true,
                    encoding = NSUTF8StringEncoding,
                    error = error // errorポインタを渡してエラー情報を受け取る
                )
            ) {
                // エラーが発生した場合
                val errorMessage = error ?: "Unknown error creating temporary file."
                println("Error creating temporary file: $errorMessage")
                return // ここで処理を終了
            }

            // 一時ファイルのURLをUIDocumentPickerViewControllerに渡す
            val picker = UIDocumentPickerViewController(
                forExportingURLs = listOf(temporaryFileURL)
            )

            // デリゲートを設定
            documentPickerDelegate = DocumentPickerDelegate()
            picker.delegate = documentPickerDelegate

            // ユーザーに提示
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                picker,
                animated = true,
                completion = null
            )
        }
    }

    /**
     * csvファイルをiPhoneプラットフォームのファイルピッカーを用いて開いて、書かれている文字列を返します
     *
     * @param allowedFileExtensions 開くファイルの拡張子の指定
     * @return 選択されたファイルに書かれているデータの文字列
     */
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun openFileAndReadText(allowedFileExtensions: List<String>): String? {
        // UI操作はメインスレッドで実行
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                currentOpenDocumentPickerDelegate = OpenFilePickerDelegate(continuation)

                // 許可するファイルタイプをUTTypeに変換
                val allowedTypes: List<UTType> = allowedFileExtensions.mapNotNull { ext ->
                    when (ext.lowercase()) {
                        ".csv" -> UTTypeCommaSeparatedText
                        else -> null // 未知の拡張子は無視
                    }
                }

                if (allowedTypes.isEmpty()) {
                    continuation.resume(null)
                    currentOpenDocumentPickerDelegate = null // デリゲートを解放
                    return@suspendCancellableCoroutine
                }

                val documentPicker = UIDocumentPickerViewController(
                    forOpeningContentTypes = allowedTypes,
                    asCopy = false
                )

                documentPicker.delegate = currentOpenDocumentPickerDelegate

                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(documentPicker, animated = true, completion = null)

                // コルーチンがキャンセルされた場合のクリーンアップ
                continuation.invokeOnCancellation {
                    // ピッカーがまだ表示されている場合、閉じる
                    documentPicker.dismissViewControllerAnimated(true, completion = null)
                    currentOpenDocumentPickerDelegate = null // デリゲートを解放
                }
            }
        }
    }

    /**
     * 現在写っている画面のスクリーンショットを撮る
     * スクリーンショットの保存先はiPhoneのデフォルトの写真アプリ
     *
     * @param defaultFileName ファイルの名前
     */
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveCanvasAsImage(defaultFileName: String) {
        // 現在のウィンドウのスクリーンショットを撮る
        val window = UIApplication.sharedApplication.keyWindow ?: return
        val scale = UIScreen.mainScreen.scale
        val bounds = window.bounds

        UIGraphicsBeginImageContextWithOptions(bounds.useContents { size.readValue() }, false, scale)
        window.drawViewHierarchyInRect(bounds, afterScreenUpdates = true)
        val image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        suspendCancellableCoroutine<Unit> { continuation ->
            PHPhotoLibrary.requestAuthorization { status ->
                when (status) {
                    PHAuthorizationStatusAuthorized -> {
                        sharedPhotoLibrary().performChanges({
                            image?.let { PHAssetChangeRequest.creationRequestForAssetFromImage(it) }
                        }) { success, error ->
                            if (success) {
                                println("Image saved to Photos.")
                                continuation.resume(Unit)
                            } else {
                                println("Error saving image to Photos: ${error?.localizedDescription}")
                                continuation.resumeWithException(RuntimeException(error?.localizedDescription))
                            }
                        }
                    }

                    PHAuthorizationStatusNotDetermined -> {
                        // requestAuthorizationで処理されているはずですが、念のため
                        continuation.resumeWithException(IllegalStateException("Photo Library authorization not determined."))
                    }

                    PHAuthorizationStatusDenied, PHAuthorizationStatusRestricted -> {
                        println("Photo Library access denied or restricted.")
                        continuation.resumeWithException(IllegalStateException("Photo Library access denied."))
                    }

                    else -> {
                        continuation.resumeWithException(IllegalStateException("Unknown Photo Library authorization status."))
                    }
                }
            }
        }
    }

    /**
     * 現在実行されているデバイスを返します
     *
     * @return デバイスの種類
     */
    override fun getDeviceType(): DeviceType {
        return DeviceType.IOS
    }
}

/**
 * IOSプラットフォーム専用の処理が記述されたインスタンスを取得できるゲッターです
 *
 * @return
 */
actual fun getPlatform(): Platform = IOSPlatform()

/**
 * ドキュメントのメニュー画面のデリゲートを管理するクラス(コンポーネント)
 *
 */
@OptIn(ExperimentalForeignApi::class)
class DocumentPickerDelegate(
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        println("Document picker was cancelled.")
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}

/**
 * ドキュメントを開く操作を行うクラス(コンポーネント)
 * 今回は単一ファイルを開くようにしています
 * 複数ファイルを開く場合はdidPickDocumentAtURLを複数形にして、NSURLをジェネリックにしたListを宣言してください
 *
 * @property continuation
 */

private class OpenFilePickerDelegate(
    private val continuation: kotlin.coroutines.Continuation<String?>
) : NSObject(), UIDocumentPickerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL) {
        // ピッカーが閉じたら実行
        controller.dismissViewControllerAnimated(true){
            val url = didPickDocumentAtURL
            if (url != null) {
                val startedAccessing = url.startAccessingSecurityScopedResource() // iCloudなど、外部から取得したURLにアクセスする場合に必要(ただし一時的)
                if (!startedAccessing) {
                    println("Failed to start accessing security-scoped resource for URL: ${url.path}")
                    continuation.resume(null)
                    return@dismissViewControllerAnimated
                }

                val fileManager = NSFileManager.defaultManager() // UIスレッドをブロックしないように,バックグラウンドスレッドでファイル読み込み
                if (fileManager.fileExistsAtPath(url.path!!)) {
                    try {
                        val content = NSString.stringWithContentsOfURL(url, encoding = NSUTF8StringEncoding, error = null)
                        continuation.resume(content)
                    } catch (e: Exception) {
                        println("Error reading file: ${e.message}")
                        continuation.resume(null)
                    } finally {
                        // セキュリティスコープの終了
                        url.stopAccessingSecurityScopedResource()
                    }
                } else {
                    println("File does not exist at path: ${url.path}")
                    continuation.resume(null)
                }
            } else {
                continuation.resume(null) // ファイルが選択されなかった
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}