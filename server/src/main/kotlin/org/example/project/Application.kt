package org.example.project

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * サーバーアプリケーションのエントリーポイント
 * Nettyエンジンを使用してKtorサーバーを起動する
 */
fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Ktorアプリケーションモジュールの設定
 * ルーティング設定とエンドポイントの定義を行う
 */
fun Application.module() {
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}