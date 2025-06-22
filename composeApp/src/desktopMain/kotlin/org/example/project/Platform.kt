package org.example.project

class DesktopPlatform : Platform {
    override val name: String = "Desktop"

    override fun showGreeting(name: String) {
        println("Greeting from $name on Desktop")
    }
}

actual fun getPlatform(): Platform = DesktopPlatform()