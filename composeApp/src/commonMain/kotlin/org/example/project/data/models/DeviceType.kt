package org.example.project.data.models

/**
 * デバイスタイプを表すEnum
 * アプリケーションが実行されているプラットフォームの種類を識別する
 */
enum class DeviceType {
    /** デスクトップ（PC）プラットフォーム */
    DESKTOP,
    
    /** iOS プラットフォーム */
    IOS,
    
    /** Android プラットフォーム */
    ANDROID,
    
    /** Webブラウザ（WebAssembly）プラットフォーム */
    WEB
}