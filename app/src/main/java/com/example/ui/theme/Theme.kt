package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeType

fun getThemeColorScheme(themeType: ThemeType): ColorScheme {
    return when (themeType) {
        ThemeType.MIDNIGHT_DARK -> darkColorScheme(
            primary = MidnightDarkPrimary,
            secondary = MidnightDarkSecondary,
            background = MidnightDarkBackground,
            surface = MidnightDarkSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = MidnightDarkOnSurface,
            onSurface = MidnightDarkOnSurface
        )
        ThemeType.IRON_HUD -> darkColorScheme(
            primary = IronHudPrimary,
            secondary = IronHudSecondary,
            background = IronHudBackground,
            surface = IronHudSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = IronHudOnSurface,
            onSurface = IronHudOnSurface
        )
        ThemeType.CYBERPUNK_NEON -> darkColorScheme(
            primary = CyberpunkPrimary,
            secondary = CyberpunkSecondary,
            background = CyberpunkBackground,
            surface = CyberpunkSurface,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = CyberpunkOnSurface,
            onSurface = CyberpunkOnSurface
        )
        ThemeType.EMERALD_OBSIDIAN -> darkColorScheme(
            primary = EmeraldPrimary,
            secondary = EmeraldSecondary,
            background = EmeraldBackground,
            surface = EmeraldSurface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = EmeraldOnSurface,
            onSurface = EmeraldOnSurface
        )
        ThemeType.SUNSET_PLASMA -> darkColorScheme(
            primary = SunsetPrimary,
            secondary = SunsetSecondary,
            background = SunsetBackground,
            surface = SunsetSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = SunsetOnSurface,
            onSurface = SunsetOnSurface
        )
        ThemeType.MIDNIGHT_ONYX -> darkColorScheme(
            primary = OnyxPrimary,
            secondary = OnyxSecondary,
            background = OnyxBackground,
            surface = OnyxSurface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = OnyxOnSurface,
            onSurface = OnyxOnSurface
        )
        ThemeType.ROSE_GOLD -> darkColorScheme(
            primary = RoseGoldPrimary,
            secondary = RoseGoldSecondary,
            background = RoseGoldBackground,
            surface = RoseGoldSurface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = RoseGoldOnSurface,
            onSurface = RoseGoldOnSurface
        )
        ThemeType.PURE_WHITE -> lightColorScheme(
            primary = PureWhitePrimary,
            secondary = PureWhiteSecondary,
            background = PureWhiteBackground,
            surface = PureWhiteSurface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = PureWhiteOnSurface,
            onSurface = PureWhiteOnSurface
        )
        ThemeType.RED_REACTOR -> darkColorScheme(
            primary = RedReactorPrimary,
            secondary = RedReactorSecondary,
            background = RedReactorBackground,
            surface = RedReactorSurface,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = RedReactorOnSurface,
            onSurface = RedReactorOnSurface
        )
        ThemeType.QUANTUM_PURPLE -> darkColorScheme(
            primary = QuantumPurplePrimary,
            secondary = QuantumPurpleSecondary,
            background = QuantumPurpleBackground,
            surface = QuantumPurpleSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = QuantumPurpleOnSurface,
            onSurface = QuantumPurpleOnSurface
        )
    }
}

@Composable
fun MyApplicationTheme(
    selectedTheme: ThemeType = ThemeType.MIDNIGHT_DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = getThemeColorScheme(selectedTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
