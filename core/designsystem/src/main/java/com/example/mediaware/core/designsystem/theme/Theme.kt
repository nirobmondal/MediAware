package com.example.mediaware.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryTeal,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondarySlate,
    onSecondary = OnSecondarySlate,
    secondaryContainer = SecondaryContainerSlate,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = TertiaryVoice,
    onTertiary = OnTertiaryVoice,
    tertiaryContainer = TertiaryContainerVoice,
    onTertiaryContainer = TextPrimaryDark,
    background = BackgroundLight,
    onBackground = TextPrimaryDark,
    surface = SurfaceLight,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryGrey,
    outline = OutlineGrey,
    outlineVariant = OutlineVariantGrey
)

@Composable
fun MediAwareTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
