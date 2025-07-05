// iosMain/kotlin/org/example/project/Platform.kt
package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.draw
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.readValue
import kotlinx.cinterop.readValues
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.data.models.DeviceType
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.*
import platform.Photos.*
import platform.Photos.PHPhotoLibrary.Companion.sharedPhotoLibrary
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class IOSPlatform: Platform {
    override val name: String = "iOS"

    override fun showGreeting(name: String) {
        // 簡単なアラート表示には UIAlertController を使用
        val alert = UIAlertController.alertControllerWithTitle(
            "Hello",
            message = "Hello, $name from iOS",
            preferredStyle = UIAlertControllerStyleAlert
        )
        alert.addAction(UIAlertAction.actionWithTitle("OK", style = UIAlertActionStyleDefault, handler = null))

        // 最前面のビューコントローラにアラートを表示
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(alert, animated = true, completion = null)
    }

    // --- saveTextToFile ---
    @OptIn(ExperimentalForeignApi::class)
    override fun saveTextToFile(content: String, defaultFileName: String) {
        val fileManager = NSFileManager.defaultManager()
        val docsDir = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )

        val fileURL = docsDir?.URLByAppendingPathComponent(defaultFileName)

        if (fileURL != null) {
            val nsContent = content as NSString
            try {
                nsContent.writeToURL(fileURL, atomically = true, encoding = NSUTF8StringEncoding, error = null)
                println("Text saved to: ${fileURL.path}")
            } catch (e: Exception) {
                println("Error saving text: ${e.message}")
            }
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
        // 現在のウィンドウのスクリーンショットを撮る（一般的な方法の例）
        val window = UIApplication.sharedApplication.keyWindow ?: return
        val scale = UIScreen.mainScreen.scale
        val bounds = window.bounds

        UIGraphicsBeginImageContextWithOptions(bounds.useContents{size.readValue()}, false, scale)
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

//    @Composable
//    @OptIn(ExperimentalForeignApi::class)
//    fun ComponentToImageBitmap(
//        modifier: Modifier = Modifier,
//        content: @Composable () -> Unit,
//    ): ImageBitmap? {
//        var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
//
//        // Wait until drawing is complete. Can be removed if content doesn't contain LaunchedEffect etc.
//        var waited by remember { mutableStateOf(false) }
//        LaunchedEffect(Unit) {
//            delay(500)
//            waited = true
//        }
//
//        Column(
//            modifier =
//                modifier
//                    .drawWithCache {
//                        val width = this.size.width.toInt()
//                        val height = this.size.height.toInt()
//
//                        val newImageBitmap = ImageBitmap(width, height)
//                        val canvas = Canvas(newImageBitmap)
//
//                        if (waited) {
//                            onDrawWithContent {
//                                imageBitmap = newImageBitmap
//                                draw(this, this.layoutDirection, canvas, this.size) {
//                                    this@onDrawWithContent.drawContent()
//                                }
//                            }
//                        } else {
//                            onDrawWithContent {
//                                // Wait for content to be ready
//                            }
//                        }
//                    },
//        ) {
//            content()
//        }
//        return imageBitmap
//    }
}

actual fun getPlatform(): Platform = IOSPlatform()


@OptIn(ExperimentalForeignApi::class)
fun convertImageBitmapToUIImage(imageBitmap: ImageBitmap) : UIImage {
    val skiaImage = Image.makeFromBitmap(imageBitmap.asSkiaBitmap())
    val pngData: Data? = skiaImage.encodeToData(EncodedImageFormat.PNG)
    val pngBytes = pngData?.bytes ?: throw Exception("Failed to encode ImageBitmap to PNG")

    val nsData =
        pngBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), pngBytes.size.toULong())
        }
    return UIImage(data = nsData)
}

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