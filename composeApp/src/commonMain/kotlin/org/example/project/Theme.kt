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
 * 1. Noto Sans JPフォントファミリーを定義します。
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun NotoSansJPFontFamily(): FontFamily = FontFamily(
    Font(Res.font.notosansjp_regular)
)

/**
 * 2. アプリのタイポグラフィ設定を定義します。
 * ★★★ ここがMaterial 3の新しい方法です ★★★
 * Material 3のTypographyは、新しいタイポグラフィスケール（displayLargeなど）を使用します。
 */
@Composable
fun AppTypography(): Typography {
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
 * 3. アプリ全体に適用するカスタムテーマを定義します。
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