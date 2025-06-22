package org.example.project

import android.content.Context
import android.widget.Toast

internal lateinit var applicationContext: Context

// Platformインターフェースの定義 (もしなければ追加、または正しいものを参照)
// 例:
// interface Platform {
//     val name: String
//     fun showGreeting(name: String)
// }

class AndroidPlatform : Platform { // クラス名のスペルを修正
    override val name : String = "Android"

    override fun showGreeting(name : String){ // メソッド名のスペースを削除
        Toast.makeText(
            applicationContext,
            "Hello, $name from Android!",
            Toast.LENGTH_LONG
        ).show()
    }
}

actual fun getPlatform(): Platform = AndroidPlatform() // クラス名のスペルを修正