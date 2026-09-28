package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReveDarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color(0xFF04180A),
    primaryContainer = Color(0xFF0D3B1C),
    onPrimaryContainer = Color(0xFF86FFAB),
    secondary = AccentBlue,
    onSecondary = Color(0xFF001E42),
    secondaryContainer = Color(0xFF0D2847),
    onSecondaryContainer = Color(0xFFB8D5FF),
    tertiary = AccentPurple,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = CardSecondaryBg,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    outlineVariant = Color(0xFF162536),
    error = LiveRed,
    onError = Color.White
)

@Composable
fun ReveSportsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ReveDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun REVELiveSportsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    ReveSportsTheme(darkTheme = true, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ReveSportsTheme(darkTheme = true, content = content)
}
