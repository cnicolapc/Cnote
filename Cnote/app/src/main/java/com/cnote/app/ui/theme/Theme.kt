package com.cnote.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CnoteYellowAccent,
    onPrimary = CnoteTextPrimary,
    background = CnoteBackground,
    surface = CnoteBackground,
    surfaceVariant = CnoteSurfaceGray,
    onBackground = CnoteTextPrimary,
    onSurface = CnoteTextPrimary,
    secondary = CnoteTextSecondary
)

private val DarkColors = darkColorScheme(
    primary = CnoteYellowAccent,
    onPrimary = Color(0xFF202124),
    background = Color(0xFF202124),
    surface = Color(0xFF202124),
    surfaceVariant = Color(0xFF303134),
    onBackground = Color(0xFFE8EAED),
    onSurface = Color(0xFFE8EAED),
    secondary = Color(0xFF9AA0A6)
)

@Composable
fun CnoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = CnoteTypography,
        content = content
    )
}
