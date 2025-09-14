package com.roocode.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RooCodeBlue = Color(0xFF2563EB)
private val RooCodeBlueDark = Color(0xFF1D4ED8)
private val RooCodeGray = Color(0xFF6B7280)
private val RooCodeGrayLight = Color(0xFFF3F4F6)

private val DarkColorScheme = darkColorScheme(
    primary = RooCodeBlue,
    secondary = RooCodeGray,
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFF111827),
    surface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFF374151),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFFD1D5DB)
)

private val LightColorScheme = lightColorScheme(
    primary = RooCodeBlue,
    secondary = RooCodeGray,
    tertiary = Color(0xFF7C3AED),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = RooCodeGrayLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF374151)
)

@Composable
fun RooCodeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}