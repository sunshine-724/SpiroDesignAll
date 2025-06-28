package org.example.project
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@JsFun("createCsvBlob")
external fun createCsvBlob(content: String): Blob

@JsFun("stringFormatForJavaScript")
external fun stringFormatForJavaScript(format: String, vararg args: Int): String

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

    override fun stringFormat(format: String, vararg args: Int): String {
        if (args.isEmpty()) return format

        return stringFormatForJavaScript(format, *args)
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
}

actual fun getPlatform(): Platform = WasmPlatform()