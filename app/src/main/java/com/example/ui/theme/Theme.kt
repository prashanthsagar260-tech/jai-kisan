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

private val DarkColorScheme =
  darkColorScheme(
    primary = AgriGreenLight,
    onPrimary = Color.Black,
    primaryContainer = AgriGreen,
    onPrimaryContainer = Color.White,
    secondary = HarvestGold,
    onSecondary = Color.Black,
    secondaryContainer = HarvestAmber,
    onSecondaryContainer = Color.White,
    tertiary = EarthBrownContainer,
    background = DarkAgriBackground,
    surface = DarkAgriSurface,
    onBackground = DarkAgriText,
    onSurface = DarkAgriText,
    surfaceVariant = DarkAgriSurface,
    onSurfaceVariant = DarkAgriTextSecondary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AgriGreen,
    onPrimary = Color.White,
    primaryContainer = AgriGreenContainer,
    onPrimaryContainer = OnAgriGreenContainer,
    secondary = HarvestGold,
    onSecondary = Color.Black,
    secondaryContainer = HarvestGoldContainer,
    onSecondaryContainer = OnHarvestGoldContainer,
    tertiary = EarthBrown,
    background = WarmCream,
    surface = SurfaceCard,
    onBackground = HighContrastText,
    onSurface = HighContrastText,
    surfaceVariant = WarmCream,
    onSurfaceVariant = HighContrastTextSecondary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Keep custom high-contrast earthy palette default
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
