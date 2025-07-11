package org.example.project

import android.os.Build

/**
 * 共有モジュール用 Android プラットフォーム実装クラス
 * Platformインターフェースの Android 版実装（共有モジュール用）
 */
class AndroidPlatform : Platform {
    /**
     * Android SDKバージョンを含むプラットフォーム名
     */
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

/**
 * 共有モジュール用 Android プラットフォームのPlatformインスタンスを返す
 * 
 * @return AndroidPlatformのインスタンス
 */
actual fun getPlatform(): Platform = AndroidPlatform()