package com.example.videodownloader

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Font ខ្មែរ "Kantumruy Pro" ដាក់ភ្ជាប់ក្នុងកម្មវិធីផ្ទាល់ (license: OFL)
 * ដូច្នេះអក្សរខ្មែរបង្ហាញដូចគ្នាលើគ្រប់ម៉ាកទូរស័ព្ទ មិនពឹងលើ font ប្រព័ន្ធ។
 */
val KhmerFont = FontFamily(
    Font(R.font.kantumruypro_regular, FontWeight.Normal),
    Font(R.font.kantumruypro_medium, FontWeight.Medium),
    Font(R.font.kantumruypro_semibold, FontWeight.SemiBold),
    Font(R.font.kantumruypro_bold, FontWeight.Bold),
)

// អក្សរខ្មែរមានស្រៈខ្ពស់/ទាប ត្រូវការកម្ពស់បន្ទាត់ធំជាងធម្មតា
private fun TextStyle.khmer() = copy(fontFamily = KhmerFont, lineHeight = fontSize * 1.65f)

private fun appTypography(): Typography {
    val b = Typography()
    return Typography(
        displayLarge = b.displayLarge.khmer(),
        displayMedium = b.displayMedium.khmer(),
        displaySmall = b.displaySmall.khmer(),
        headlineLarge = b.headlineLarge.khmer(),
        headlineMedium = b.headlineMedium.khmer(),
        headlineSmall = b.headlineSmall.khmer(),
        titleLarge = b.titleLarge.khmer(),
        titleMedium = b.titleMedium.khmer(),
        titleSmall = b.titleSmall.khmer(),
        bodyLarge = b.bodyLarge.khmer(),
        bodyMedium = b.bodyMedium.khmer(),
        bodySmall = b.bodySmall.khmer(),
        labelLarge = b.labelLarge.khmer(),
        labelMedium = b.labelMedium.khmer(),
        labelSmall = b.labelSmall.khmer(),
    )
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B4BDB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E1FF),
    onPrimaryContainer = Color(0xFF1B0F70),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD2F4EE),
    onSecondaryContainer = Color(0xFF00352F),
    tertiary = Color(0xFF1F7AE0),
    background = Color(0xFFF5F4FA),
    onBackground = Color(0xFF1B1B25),
    surface = Color.White,
    onSurface = Color(0xFF1B1B25),
    surfaceVariant = Color(0xFFECEAF5),
    onSurfaceVariant = Color(0xFF5E5C70),
    outline = Color(0xFFB7B4CC),
    outlineVariant = Color(0xFFDAD8E8),
    error = Color(0xFFD93A4A),
    onError = Color.White,
    errorContainer = Color(0xFFFFE1E4),
    onErrorContainer = Color(0xFF5C0A14),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9B0FF),
    onPrimary = Color(0xFF2B1D8F),
    primaryContainer = Color(0xFF42359F),
    onPrimaryContainer = Color(0xFFE5E1FF),
    secondary = Color(0xFF6FD8C8),
    onSecondary = Color(0xFF00382F),
    secondaryContainer = Color(0xFF1B4F48),
    onSecondaryContainer = Color(0xFFD2F4EE),
    tertiary = Color(0xFF8DB8FF),
    background = Color(0xFF12111A),
    onBackground = Color(0xFFE6E3F2),
    surface = Color(0xFF1C1B27),
    onSurface = Color(0xFFE6E3F2),
    surfaceVariant = Color(0xFF2A2938),
    onSurfaceVariant = Color(0xFFC3C0D6),
    outline = Color(0xFF8A879E),
    outlineVariant = Color(0xFF3A384C),
    error = Color(0xFFFF8A94),
    onError = Color(0xFF5C0A14),
    errorContainer = Color(0xFF6B1A24),
    onErrorContainer = Color(0xFFFFE1E4),
)

@Composable
fun successColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF6FE3A8) else Color(0xFF14935C)

@Composable
fun successContainer(): Color =
    if (isSystemInDarkTheme()) Color(0xFF14442F) else Color(0xFFD9F5E8)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = appTypography(),
        content = content
    )
}
