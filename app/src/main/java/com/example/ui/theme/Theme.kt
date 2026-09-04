package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.model.ThemeMode

private val FrostedGlassDarkColorScheme = darkColorScheme(
    primary = NebulaIndigo,
    onPrimary = TextWhitePrimary,
    primaryContainer = FrostedGlassSurfaceVariant,
    onPrimaryContainer = NebulaIndigo,
    secondary = NebulaFuchsia,
    onSecondary = TextWhitePrimary,
    tertiary = NebulaBlue,
    onTertiary = TextWhitePrimary,
    background = NebulaBackground,
    onBackground = TextWhitePrimary,
    surface = FrostedGlassSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = FrostedGlassSurfaceVariant,
    onSurfaceVariant = TextMutedSecondary,
    outline = FrostedGlassBorder,
    error = SecurityRed,
    onError = TextWhitePrimary
)

private val AmoledColorScheme = darkColorScheme(
    primary = NebulaIndigo,
    onPrimary = TextWhitePrimary,
    primaryContainer = AmoledSurfaceVariant,
    onPrimaryContainer = NebulaIndigo,
    secondary = NebulaFuchsia,
    onSecondary = TextWhitePrimary,
    tertiary = NebulaBlue,
    onTertiary = TextWhitePrimary,
    background = AmoledBackground,
    onBackground = TextWhitePrimary,
    surface = AmoledSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = TextMutedSecondary,
    outline = AmoledBorder,
    error = SecurityRed,
    onError = TextWhitePrimary
)

private val LightThemeColorScheme = lightColorScheme(
    primary = NebulaIndigo,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = NebulaIndigo,
    secondary = NebulaFuchsia,
    onSecondary = LightSurface,
    tertiary = NebulaBlue,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = SecurityRed,
    onError = LightSurface
)

@Composable
fun AhmadGuardTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> FrostedGlassDarkColorScheme
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.LIGHT -> LightThemeColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
