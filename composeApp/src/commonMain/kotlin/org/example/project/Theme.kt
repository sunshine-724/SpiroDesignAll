package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import spirodesign.composeapp.generated.resources.Res
import spirodesign.composeapp.generated.resources.notosansjp_regular

/**
 * Noto Sans JPフォントファミリーを定義するComposable関数
 * アプリケーション全体で日本語テキストを美しく表示するためのフォント設定
 * 
 * @return Noto Sans JPフォントファミリー
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun NotoSansJPFontFamily(): FontFamily = FontFamily(
    Font(Res.font.notosansjp_regular)
)

/**
 * アプリケーションのタイポグラフィ設定を定義するComposable関数
 * Material 3の新しいタイポグラフィスケールを使用してNoto Sans JPフォントを適用
 * 
 * @return アプリケーション用にカスタマイズされたTypographyオブジェクト
 */
@Composable
fun AppTypography(): Typography {
    /**
     * Noto Sans JPフォントファミリーのインスタンス
     */
    val notoSansJP = NotoSansJPFontFamily()

    // 各テキストスタイルにデフォルトとして新しいフォントを適用します。
    // TextStyleの他のプロパティ（fontSizeなど）はMaterial3のデフォルト値が使われます。
    return Typography(
        displayLarge = TextStyle(fontFamily = notoSansJP),
        displayMedium = TextStyle(fontFamily = notoSansJP),
        displaySmall = TextStyle(fontFamily = notoSansJP),
        headlineLarge = TextStyle(fontFamily = notoSansJP),
        headlineMedium = TextStyle(fontFamily = notoSansJP),
        headlineSmall = TextStyle(fontFamily = notoSansJP),
        titleLarge = TextStyle(fontFamily = notoSansJP),
        titleMedium = TextStyle(fontFamily = notoSansJP),
        titleSmall = TextStyle(fontFamily = notoSansJP),
        bodyLarge = TextStyle(fontFamily = notoSansJP),
        bodyMedium = TextStyle(fontFamily = notoSansJP),
        bodySmall = TextStyle(fontFamily = notoSansJP),
        labelLarge = TextStyle(fontFamily = notoSansJP),
        labelMedium = TextStyle(fontFamily = notoSansJP),
        labelSmall = TextStyle(fontFamily = notoSansJP)
    )
}

/**
 * アプリケーション全体に適用するカスタムテーマを定義するComposable関数
 * Material 3のテーマシステムを使用してカスタムタイポグラフィを適用
 * 
 * @param content テーマを適用するComposableコンテンツ
 */
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    // MaterialThemeもmaterial3のものを使用します
    MaterialTheme(
        typography = AppTypography(),
        content = content
    )
}