package org.example.project
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import kotlin.Any

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
        if(args.isEmpty()) return format

        return stringFormatForJavaScript(format, *args)
    }
}

actual fun getPlatform(): Platform = WasmPlatform()