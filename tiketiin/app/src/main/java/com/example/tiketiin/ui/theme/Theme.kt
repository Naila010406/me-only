package com.example.tiketiin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    secondary = RedBadge,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    error = RedDark
)

@Composable
fun TiketiinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
