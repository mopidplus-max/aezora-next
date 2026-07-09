package com.aezora.next.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

enum class AezoraTheme { DARK_WHITE, YELLOW_GREEN, BLUE_VIOLET }

data class AezoraColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val error: Color,
    val playerBg: Color
)

val LocalAezoraColors = staticCompositionLocalOf {
    AezoraColorScheme(
        background      = BlueVioletColors.Background,
        surface         = BlueVioletColors.Surface,
        surfaceVariant  = BlueVioletColors.SurfaceVariant,
        primary         = BlueVioletColors.Primary,
        primaryVariant  = BlueVioletColors.PrimaryVariant,
        secondary       = BlueVioletColors.Secondary,
        accent          = BlueVioletColors.Accent,
        accentSecondary = BlueVioletColors.AccentSecondary,
        onPrimary       = BlueVioletColors.OnPrimary,
        onBackground    = BlueVioletColors.OnBackground,
        onSurface       = BlueVioletColors.OnSurface,
        error           = BlueVioletColors.Error,
        playerBg        = BlueVioletColors.PlayerBg
    )
}

fun aezoraColorScheme(theme: AezoraTheme): AezoraColorScheme = when (theme) {
    AezoraTheme.DARK_WHITE -> AezoraColorScheme(
        background      = DarkWhiteColors.Background,
        surface         = DarkWhiteColors.Surface,
        surfaceVariant  = DarkWhiteColors.SurfaceVariant,
        primary         = DarkWhiteColors.Primary,
        primaryVariant  = DarkWhiteColors.PrimaryVariant,
        secondary       = DarkWhiteColors.Secondary,
        accent          = DarkWhiteColors.Accent,
        accentSecondary = DarkWhiteColors.AccentSecondary,
        onPrimary       = DarkWhiteColors.OnPrimary,
        onBackground    = DarkWhiteColors.OnBackground,
        onSurface       = DarkWhiteColors.OnSurface,
        error           = DarkWhiteColors.Error,
        playerBg        = DarkWhiteColors.PlayerBg
    )
    AezoraTheme.YELLOW_GREEN -> AezoraColorScheme(
        background      = YellowGreenColors.Background,
        surface         = YellowGreenColors.Surface,
        surfaceVariant  = YellowGreenColors.SurfaceVariant,
        primary         = YellowGreenColors.Primary,
        primaryVariant  = YellowGreenColors.PrimaryVariant,
        secondary       = YellowGreenColors.Secondary,
        accent          = YellowGreenColors.Accent,
        accentSecondary = YellowGreenColors.AccentSecondary,
        onPrimary       = YellowGreenColors.OnPrimary,
        onBackground    = YellowGreenColors.OnBackground,
        onSurface       = YellowGreenColors.OnSurface,
        error           = YellowGreenColors.Error,
        playerBg        = YellowGreenColors.PlayerBg
    )
    AezoraTheme.BLUE_VIOLET -> AezoraColorScheme(
        background      = BlueVioletColors.Background,
        surface         = BlueVioletColors.Surface,
        surfaceVariant  = BlueVioletColors.SurfaceVariant,
        primary         = BlueVioletColors.Primary,
        primaryVariant  = BlueVioletColors.PrimaryVariant,
        secondary       = BlueVioletColors.Secondary,
        accent          = BlueVioletColors.Accent,
        accentSecondary = BlueVioletColors.AccentSecondary,
        onPrimary       = BlueVioletColors.OnPrimary,
        onBackground    = BlueVioletColors.OnBackground,
        onSurface       = BlueVioletColors.OnSurface,
        error           = BlueVioletColors.Error,
        playerBg        = BlueVioletColors.PlayerBg
    )
}

@Composable
fun AezoraNextTheme(
    theme: AezoraTheme = AezoraTheme.BLUE_VIOLET,
    content: @Composable () -> Unit
) {
    val colors = aezoraColorScheme(theme)
    val m3Colors = darkColorScheme(
        primary         = colors.primary,
        secondary       = colors.secondary,
        background      = colors.background,
        surface         = colors.surface,
        surfaceVariant  = colors.surfaceVariant,
        onPrimary       = colors.onPrimary,
        onBackground    = colors.onBackground,
        onSurface       = colors.onSurface,
        error           = colors.error
    )
    CompositionLocalProvider(LocalAezoraColors provides colors) {
        MaterialTheme(
            colorScheme = m3Colors,
            typography  = AezoraTypography,
            content     = content
        )
    }
}

val MaterialTheme.aezoraColors: AezoraColorScheme
    @Composable get() = LocalAezoraColors.current
