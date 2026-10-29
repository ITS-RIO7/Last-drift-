package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DriftColorScheme = darkColorScheme(
    primary = NeonOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A1A00),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004D5A),
    onSecondaryContainer = Color(0xFFB2F5EA),
    tertiary = NeonPurple,
    onTertiary = Color.White,
    background = RacingDark,
    onBackground = TextPrimary,
    surface = RacingSurface,
    onSurface = TextPrimary,
    surfaceVariant = RacingSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AsphaltBorder,
    error = NeonRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DriftColorScheme,
        typography = Typography,
        content = content
    )
}
