package com.sana.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SanaColorScheme = darkColorScheme(
    primary = SanaPrimary,
    onPrimary = SanaOnPrimary,
    secondary = SanaSecondary,
    onSecondary = SanaOnSecondary,
    tertiary = SanaTertiary,
    onTertiary = SanaOnTertiary,
    background = SanaBackground,
    onBackground = SanaOnBackground,
    surface = SanaSurface,
    onSurface = SanaOnSurface,
    surfaceVariant = SanaSurfaceVariant,
    onSurfaceVariant = SanaOnSurfaceVariant,
    surfaceContainer = SanaSurface,
    surfaceContainerHigh = SanaSurfaceVariant,
    surfaceContainerHighest = SanaSurfaceVariant,
    surfaceContainerLow = SanaSurface,
    surfaceContainerLowest = SanaBackground,
    error = SanaError,
    onError = SanaOnError,
    outline = SanaOutline,
    outlineVariant = SanaOutline,
)

/**
 * Sana is dark-mode only (per the wireframes). Wrap the whole app (and every @Preview)
 * in this so colors and typography stay consistent.
 */
@Composable
fun SanaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SanaColorScheme,
        typography = Typography,
        content = content
    )
}
