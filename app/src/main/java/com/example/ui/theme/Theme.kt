package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EditorialLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaSoft,
    onPrimaryContainer = TerracottaDark,
    secondary = RoyalBlueAccent,
    onSecondary = Color.White,
    secondaryContainer = RoyalBlueSoft,
    onSecondaryContainer = RoyalBlueAccent,
    background = EditorialPaper,
    onBackground = EditorialInk,
    surface = EditorialCardWhite,
    onSurface = EditorialInk,
    surfaceVariant = EditorialWarmStone,
    onSurfaceVariant = EditorialMutedInk,
    outline = EditorialHairline,
    outlineVariant = EditorialHairline
)

private val EditorialDarkColorScheme = darkColorScheme(
    primary = TerracottaLightAccent,
    onPrimary = DarkEditorialBg,
    primaryContainer = Color(0xFF4B2113),
    onPrimaryContainer = TerracottaSoft,
    secondary = RoyalBlueLightAccent,
    onSecondary = DarkEditorialBg,
    secondaryContainer = Color(0xFF172754),
    onSecondaryContainer = RoyalBlueSoft,
    background = DarkEditorialBg,
    onBackground = DarkEditorialInk,
    surface = DarkEditorialSurface,
    onSurface = DarkEditorialInk,
    surfaceVariant = DarkEditorialSurfaceElevated,
    onSurfaceVariant = DarkEditorialMutedInk,
    outline = DarkEditorialHairline,
    outlineVariant = DarkEditorialHairline
)

val CanvasShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun Canvas27Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CanvasTypography,
        shapes = CanvasShapes,
        content = content
    )
}
