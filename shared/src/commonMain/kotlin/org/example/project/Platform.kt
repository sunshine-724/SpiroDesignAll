package org.example.project

/**
 * 共有モジュール用のプラットフォーム抽象化インターフェース
 * 各プラットフォーム（Android、iOS）で異なる実装を持つ基本的なプラットフォーム情報を提供する
 */
interface Platform {
    /**
     * プラットフォーム名（バージョン情報を含む）
     */
    val name: String
}

/**
 * 共有モジュール用のプラットフォーム固有のPlatformインスタンスを取得する
 * 
 * @return 現在実行中のプラットフォームに対応するPlatformインスタンス
 */
expect fun getPlatform(): Platform