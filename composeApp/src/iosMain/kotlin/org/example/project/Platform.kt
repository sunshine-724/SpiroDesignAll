// composeApp/src/iosMain/kotlin/Platform.kt

package org.example.project

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override fun showGreeting(name: String) {
        // Xcodeのコンソールにログを出力する
        println("Greeting from $name on iOS!")
    }
}

actual fun getPlatform(): Platform = IOSPlatform()