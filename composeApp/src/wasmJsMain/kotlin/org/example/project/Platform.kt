package org.example.project
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@JsFun("createCsvBlob")
external fun createCsvBlob(content: String): Blob


class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"

    override fun showGreeting(name: String) {
        window.alert("Hello, $name from Wasm")
    }

    /**
     * `saveTextToFile`のWebAssembly(Wasm)向け実装。
     * 文字列からBlobを作成し、ダウンロードリンクを生成してクリックすることで、
     * ブラウザにファイルをダウンロードさせる。
     */
    override fun saveTextToFile(content: String, defaultFileName: String) {
        // 1. 保存する文字列(content)から、ファイルのようなオブジェクト(Blob)を作成する
        //    MIMEタイプに 'text/csv' を指定
        println("Blobを作成します")
        val blob = createCsvBlob(content)

        // 2. 作成したBlobにアクセスするための、一時的なURLを生成する
        println("URLを作成します")
        val url = URL.createObjectURL(blob)

        // 3. ダウンロード用の<a>タグ（アンカーリンク）をメモリ上に作成する
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = defaultFileName

        // 4. 作成した<a>タグをドキュメントに一瞬だけ追加し、プログラム的にクリックして、すぐに削除する
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)

        // 5. 使い終わった一時的なURLをメモリから解放する
        URL.revokeObjectURL(url)
    }

    /**
     * `expect fun openFileAndReadText`のWASMブラウザ向け実装。
     * DOM APIを使用してファイル入力要素を動的に作成し、ファイル読み込み処理を行います。
     * @param allowedFileExtensions 開くのを許可しているファイルの拡張子
     * @return csvファイルから読み取ったものを一つのString変数として返します
     */
    override suspend fun openFileAndReadText(allowedFileExtensions: List<String>): String? {
        return suspendCoroutine{ continuation ->
            // 1. 動的に<input type="file">要素を作成
            val fileInput = (document.createElement("input") as HTMLInputElement).apply {
                type = "file"
                style.display = "none" // ユーザーには見えないようにする
                accept = allowedFileExtensions.joinToString(",") // 例: ".csv,.txt"
            }

            // 2. ファイルが選択されたときの処理を設定
            fileInput.onchange = {
                val file = fileInput.files?.get(0)
                if (file != null) {
                    val reader = FileReader()
                    // 読み込み完了時の処理
                    reader.onload = {
                        // JsAny型からString型にキャストするには一度JsString型に変換する必要がある
                        val result = reader.result as? JsString
                        val stringResult = result?.toString()
                        continuation.resume(stringResult)
                        document.body?.removeChild(fileInput)
                    }
                    // 読み込み失敗時の処理
                    reader.onerror = {
                        println("Error reading file: ${reader.error}")
                        continuation.resume(null) // 失敗した場合はnullを返す
                        document.body?.removeChild(fileInput)
                    }
                    reader.readAsText(file)
                } else {
                    // ファイルが選択されなかった（キャンセルされた）場合
                    continuation.resume(null)
                    document.body?.removeChild(fileInput)
                }
            }

            // 3. 要素をDOMに追加し、クリックイベントを発火させてファイル選択ダイアログを開く
            document.body?.appendChild(fileInput)
            fileInput.click()
        }
    }

    override suspend fun saveCanvasAsImage(defaultFileName: String) {
        // requestAnimationFrameを使って、次の描画フレームを待つ
        suspendCancellableCoroutine<Unit> { continuation ->
            window.requestAnimationFrame {
                // 一番上にあるcanvas要素を習得する
                val canvas = document.querySelector("canvas") as? HTMLCanvasElement
                if (canvas == null) {
                    println("Error: Canvas with ID 'ComposeTarget' not found.")
                    continuation.resume(Unit)
                    return@requestAnimationFrame
                }

                val dataUrl = canvas.toDataURL("image/png")

                // --- ここからがダウンロード処理の最終版 ---
                val anchor = document.createElement("a") as HTMLAnchorElement
                anchor.href = dataUrl
                anchor.download = defaultFileName
                document.body?.appendChild(anchor)

                // リンクをプログラムがクリックして、ダウンロードを開始
                anchor.click()

                // ダウンロード開始と同時に要素を削除すると、ダウンロードの準備が完了する前にdataURL要素を消してしまうので、少量の遅延を入れる
                window.setTimeout({
                    document.body?.removeChild(anchor)
                }, 100)
                // 後片付けが終わったら、コルーチンを再開させる
                continuation.resume(Unit)
            }
        }
    }


    suspend fun debugCanvas() {
        // requestAnimationFrameを使って、次の描画フレームのタイミングを待つ
        suspendCancellableCoroutine<Unit> { continuation ->
            window.requestAnimationFrame {
                println("--- Canvas Debug Start ---")

                val canvas = document.querySelector("canvas") as? HTMLCanvasElement
                if (canvas == null) {
                    println("DEBUG_ERROR: Canvas with ID 'ComposeTarget' NOT FOUND.")
                    continuation.resume(Unit)
                    return@requestAnimationFrame
                }

                println("Canvas Found: YES")
                println("Canvas Dimensions: width=${canvas.width}, height=${canvas.height}")

                try {
                    // Canvasの内容を文字列データ(DataURL)に変換
                    val dataUrl = canvas.toDataURL("image/png")

                    println("Generated dataURL length: ${dataUrl.length}")

                    if (dataUrl.length < 200) {
                        println("DEBUG_WARNING: The generated dataURL is very short. This often means the canvas is empty, black, or tainted.")
                    }

                    println("--- COPY THE LINE BELOW AND PASTE IT INTO YOUR BROWSER'S ADDRESS BAR ---")
                    // この行をコンソールに出力する
                    println(dataUrl)
                    println("-------------------------------------------------------------------------")

                } catch (e: Error) {
                    // セキュリティエラーなど、dataURLの取得に失敗した場合
                    println("DEBUG_ERROR: Failed to get dataURL. This might be a security (tainted canvas) issue.")
                    println("Error message: ${e.message}")
                }

                println("--- Canvas Debug End ---")
                continuation.resume(Unit)
            }
        }
    }
}

actual fun getPlatform(): Platform = WasmPlatform()