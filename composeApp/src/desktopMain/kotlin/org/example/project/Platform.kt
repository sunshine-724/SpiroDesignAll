package org.example.project

/**
 * Desktop プラットフォーム固有の実装クラス
 * Platformインターフェースの Desktop 版実装を提供する
 */
class DesktopPlatform : Platform {
    /**
     * プラットフォーム名
     */
    override val name: String = "Desktop"

    /**
     * デスクトップコンソール出力による挨拶表示
     * 
     * @param name 挨拶に表示する名前
     */
    override fun showGreeting(name: String) {
        println("Greeting from $name on Desktop")
    }
}

/**
 * Desktop プラットフォーム用のPlatformインスタンスを返す
 * 
 * @return DesktopPlatformのインスタンス
 */
actual fun getPlatform(): Platform = DesktopPlatform()