
package com.mdve.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MDVEColorScheme = lightColorScheme(
    primary = OctoRed,
    onPrimary = SurfaceWhite,

    secondary = OctoRedDark,
    onSecondary = SurfaceWhite,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceGray,
    onSurfaceVariant = TextSecondary,

    outline = BorderLight,

    error = ErrorRed,
    onError = SurfaceWhite
)

@Composable
fun MDVETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MDVEColorScheme,
        typography = AppTypography,
        content = content
    )
}
