package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Compose App の共通テストクラス
 * プラットフォームに依存しない共通ロジックのテストを行う
 */
class ComposeAppCommonTest {

    /**
     * 基本的な算術演算のテスト例
     * テストフレームワークが正常に動作することを確認する
     */
    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }
}