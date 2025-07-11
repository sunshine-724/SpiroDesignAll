package org.example.project

import android.content.Context
import android.widget.Toast

/**
 * Androidアプリケーションのコンテキスト
 * グローバルにアクセス可能なアプリケーションコンテキスト
 */
internal lateinit var applicationContext: Context

// Platformインターフェースの定義 (もしなければ追加、または正しいものを参照)
// 例:
// interface Platform {
//     val name: String
//     fun showGreeting(name: String)
// }

/**
 * Android プラットフォーム固有の実装クラス
 * Platformインターフェースの Android 版実装を提供する
 */
class AndroidPlatform : Platform {
    /**
     * プラットフォーム名
     */
    override val name : String = "Android"

    /**
     * Androidトーストによる挨拶表示
     * 
     * @param name 挨拶に表示する名前
     */
    override fun showGreeting(name : String){
        Toast.makeText(
            applicationContext,
            "Hello, $name from Android!",
            Toast.LENGTH_LONG
        ).show()
    }
}

/**
 * Android プラットフォーム用のPlatformインスタンスを返す
 * 
 * @return AndroidPlatformのインスタンス
 */
actual fun getPlatform(): Platform = AndroidPlatform()