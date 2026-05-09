package app.jammes.thepro.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = TextOnDark,
    primaryContainer = AccentDim,
    onPrimaryContainer = TextOnDark,

    secondary = Navy,
    onSecondary = TextOnDark,
    secondaryContainer = NavyDeep,
    onSecondaryContainer = TextMuted,

    tertiary = NavyDeep,
    onTertiary = TextOnDark,
    tertiaryContainer = NavyDeep,
    onTertiaryContainer = TextMuted,

    background = Black,
    onBackground = TextOnDark,

    surface = NavyDeep,
    onSurface = TextOnDark,
    surfaceVariant = Navy,
    onSurfaceVariant = TextMuted,

    surfaceContainerLowest = Black,
    surfaceContainerLow = Black,
    surfaceContainer = NavyDeep,
    surfaceContainerHigh = NavyDeep,
    surfaceContainerHighest = Navy,

    surfaceTint = Accent,

    outline = OutlineDim,
    outlineVariant = OutlineFaint,

    error = DangerRed,
    onError = TextOnDark,
    errorContainer = DangerRed,
    onErrorContainer = TextOnDark
)

@Composable
fun ThePROTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
