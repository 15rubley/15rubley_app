package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BoltEmerald,
    onPrimary = Color(0xFF042110),
    primaryContainer = BoltEmeraldDarkContainer,
    onPrimaryContainer = BoltMintLight,
    secondary = SberGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF173323),
    onSecondaryContainer = BoltMintLight,
    tertiary = AmberAlert,
    onTertiary = Color(0xFF2B1B00),
    background = AsphaltDark,
    onBackground = TextOnDark,
    surface = AsphaltSurface,
    onSurface = TextOnDark,
    surfaceVariant = AsphaltSurfaceHigh,
    onSurfaceVariant = TextSecondaryOnDark,
    surfaceContainerLowest = Color(0xFF080E0B),
    surfaceContainerLow = AsphaltSurface,
    surfaceContainer = AsphaltSurfaceHigh,
    surfaceContainerHigh = AsphaltSurfaceHighest,
    outline = Color(0xFF2E4739),
    outlineVariant = Color(0xFF1F3127),
    error = CoralAlert,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = BoltEmeraldDeep,
    onPrimary = Color.White,
    primaryContainer = BoltMintLight,
    onPrimaryContainer = Color(0xFF052E16),
    secondary = SberGreen,
    onSecondary = Color.White,
    secondaryContainer = SberGreenLight,
    onSecondaryContainer = Color(0xFF0A3615),
    tertiary = AmberAlert,
    onTertiary = Color(0xFF2B1B00),
    background = BoltMintSurface,
    onBackground = TextOnLight,
    surface = Color.White,
    onSurface = TextOnLight,
    surfaceVariant = Color(0xFFE5F2EA),
    onSurfaceVariant = TextSecondaryOnLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7FCF9),
    surfaceContainer = Color(0xFFEDF7F1),
    surfaceContainerHigh = Color(0xFFE1EFE7),
    outline = Color(0xFFBBD3C4),
    outlineVariant = Color(0xFFD7E8DD),
    error = CoralAlert,
    onError = Color.White,
)

@Composable
fun CourierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
