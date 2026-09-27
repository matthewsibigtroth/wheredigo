package com.wheredigo.hikingtracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppColorScheme = lightColorScheme(
    primary = VibrantYellowButton,
    onPrimary = DeepForestText,
    primaryContainer = PastelSpringGreen,
    onPrimaryContainer = DeepForestText,
    secondary = SoftMintContainer,
    onSecondary = DeepForestText,
    secondaryContainer = SoftMintContainer,
    onSecondaryContainer = DeepForestText,
    tertiary = SoftPeachAccent,
    onTertiary = DeepForestText,
    background = SoftSageBackground,
    onBackground = DeepForestText,
    surface = CrispWhiteSurface,
    onSurface = DeepForestText,
    surfaceVariant = SoftSageBackground,
    onSurfaceVariant = MutedForestText
)

@Composable
fun WhereDiGoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
