package org.example.project
import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"

    override fun showGreeting(name: String) {
        window.alert("Hello, $name from Wasm")
    }
}

actual fun getPlatform(): Platform = WasmPlatform()