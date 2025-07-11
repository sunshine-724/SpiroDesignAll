package org.example.project

import platform.UIKit.UIDevice

/**
 * 共有モジュール用 iOS プラットフォーム実装クラス
 * Platformインターフェースの iOS 版実装（共有モジュール用）
 */
class IOSPlatform: Platform {
    /**
     * iOSシステム名とバージョンを含むプラットフォーム名
     */
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

/**
 * 共有モジュール用 iOS プラットフォームのPlatformインスタンスを返す
 * 
 * @return IOSPlatformのインスタンス
 */
actual fun getPlatform(): Platform = IOSPlatform()