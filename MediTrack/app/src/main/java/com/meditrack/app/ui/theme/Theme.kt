package com.meditrack.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val ForestColors = darkColorScheme(
    primary = Primary,
    onPrimary = PrimaryOn,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Primary,
    background = Background,
    onBackground = Ink,
    surface = CardSurface,
    onSurface = Ink,
    surfaceVariant = CardSurface,
    onSurfaceVariant = Muted,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = CardSurface,
    surfaceContainerHighest = CardSurface,
    error = Danger,
    outline = Outline
)

@Composable
fun MediTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ForestColors,
        typography = AppTypography
    ) {
        // Makes every Text use the Lexend font unless it sets its own.
        ProvideTextStyle(TextStyle(fontFamily = AppFontFamily)) {
            content()
        }
    }
}
