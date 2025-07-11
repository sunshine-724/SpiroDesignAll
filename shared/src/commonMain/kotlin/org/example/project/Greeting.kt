package org.example.project

/**
 * プラットフォーム固有の挨拶メッセージを生成するクラス
 * 各プラットフォーム（Android、iOS、Desktop、Web）の名前を含む挨拶文を提供する
 */
class Greeting {
    /**
     * 現在のプラットフォームのインスタンス
     */
    private val platform = getPlatform()

    /**
     * プラットフォーム名を含む挨拶メッセージを生成する
     * 
     * @return "Hello, {プラットフォーム名}!" 形式の挨拶文字列
     */
    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}