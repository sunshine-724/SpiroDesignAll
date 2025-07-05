// iosMain/kotlin/org/example/project/Platform.kt
package org.example.project

import androidx.compose.runtime.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.data.models.DeviceType
import platform.Foundation.*
import platform.Photos.*
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText

class IOSPlatform: Platform {
    override val name: String = "iOS"
    private var documentPickerDelegate: DocumentPickerDelegate? = null // GCされないようにフィールドでデリゲートを保持

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


    // --- openFileAndReadText ---
    // UI操作とデリゲートが必要なため、より複雑になります
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun openFileAndReadText(allowedFileExtensions: List<String>): String? {
        return suspendCancellableCoroutine { continuation ->
            val documentPickerDelegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
                    val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                    if (url != null) {
                        val fileManager = NSFileManager.defaultManager()
                        if (fileManager.fileExistsAtPath(url.path!!)) {
                            try {
                                val content = NSString.stringWithContentsOfURL(url, encoding = NSUTF8StringEncoding, error = null)
                                continuation.resume(content)
                            } catch (e: Exception) {
                                println("Error reading file: ${e.message}")
                                continuation.resume(null)
                            }
                        } else {
                            println("File does not exist at path: ${url.path}")
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null) // ファイルが選択されなかった
                    }
                }

                override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                    continuation.resume(null) // ユーザーがキャンセル
                }
            }

            // デリゲートが早期にデアロケートされないように参照を保持
            // これはKotlin/Nativeにおけるデリゲートの一般的なパターンです。
            // より堅牢な方法で、例えばViewModelや専用のクラス内でこれを保持することを検討してください。
            // ここでは簡略化のため、現在のUIアプリケーションに一時的なプロパティとしてアタッチします。
            // 実際のアプリでは、デリゲートのライフサイクルを注意深く管理してください。
            val app = UIApplication.sharedApplication
            val key = "DocumentPickerDelegate"
            // iOSでは、delegateオブジェクトが適切に保持される必要があります。
            // ここでは簡易的な例として、シングルトンオブジェクトに保持するか、
            // pickerを表示するUIViewControllerがこのデリゲートへの強い参照を持つようにします。
            // 実際のアプリでは、シングルトンを使用したり、UIViewControllerにプロパティとして持たせたりします。
            // ここでは便宜上、UIApplicationのassociated objectを使用していますが、これはプロダクションコードでは推奨されません。
            // 正しい実装では、呼び出し元のUIViewControllerがデリゲートをプロパティとして保持します。
            // 例: currentViewController.documentPickerDelegate = documentPickerDelegate
            // ここではダミーとして保持する機構を示します。
            app.setAssociatedObject(documentPickerDelegate, key) // デリゲートの参照を保持する簡易的な方法

            val allowedTypes: List<UTType> = listOf(
                UTTypeCommaSeparatedText,
            )

            val documentPicker = UIDocumentPickerViewController(
                forOpeningContentTypes = allowedTypes, // ".csv" -> "public.csv"
                asCopy = true // ファイルをアプリのサンドボックスにコピーする
            )

            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(documentPicker, animated = true, completion = null)

            continuation.invokeOnCancellation {
                app.setAssociatedObject(null, key) // デリゲート参照をクリーンアップ
            }
        }
    }

    // --- saveCanvasAsImage ---
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

    override fun getDeviceType(): DeviceType {
        return DeviceType.IOS
    }
}

actual fun getPlatform(): Platform = IOSPlatform()

// associated objectを作成するためのヘルパー関数 (openFileAndReadTextのデリゲート用)
// 実際のアプリケーションでは、より堅牢なデリゲート管理戦略を検討してください。
// この実装はプロダクションには適していません。
fun UIApplication.setAssociatedObject(value: Any?, key: String) {
    // これは簡略化されたアプローチです。プロダクションアプリでは、
    // <objc/runtime.h> の objc_setAssociatedObject を使用するか、
    // ビューコントローラ内の強い参照を通じてデリゲートを管理します。
    // 簡単な概念実証では、静的な可変マップを使用することもできますが、メモリリークに注意が必要です。
    // より適切な解決策は、UIKitのデリゲートパターンと統合することです。
    // 現在のところ、これは概念的なプレースホルダーです。
}

/**
 * ドキュメントのメニュー画面のデリゲートを管理するクラス(コンポーネント)
 *
 */
@OptIn(ExperimentalForeignApi::class)
class DocumentPickerDelegate(
) : NSObject(), UIDocumentPickerDelegateProtocol {

//    // エクスポートモードでは、このメソッドは通常呼ばれないか、異なる使われ方をする
//    // ユーザーが既存のファイルを上書きしようとした場合に呼ばれる可能性がある
//    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<NSURL>) {
//        // エクスポートモードでは、このコールバックは通常必要ない
//        // ユーザーが選択した場所への保存は、ピッカーが提供された一時ファイルを移動することで行われる
//        println("documentPicker: didPickDocumentsAtURLs called (should not be for export mode).")
//        controller.dismissViewControllerAnimated(true, completion = null)
//        println("File operation completed (check system behavior for actual save).")
//    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        println("Document picker was cancelled.")
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}