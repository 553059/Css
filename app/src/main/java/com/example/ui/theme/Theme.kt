package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberGreen,
    onPrimary = AbyssBlack,
    secondary = CyberCyan,
    onSecondary = AbyssBlack,
    tertiary = CyberAmber,
    background = AbyssBlack,
    onBackground = TextWhite,
    surface = SlateGray,
    onSurface = TextWhite,
    surfaceVariant = LightSlate,
    onSurfaceVariant = TextGray
  )

private val LightColorScheme =
  darkColorScheme( // We want a persistent high-fidelity dark theme look for the analysis, but support fallback nicely
    primary = CyberGreen,
    onPrimary = AbyssBlack,
    secondary = CyberCyan,
    onSecondary = AbyssBlack,
    tertiary = CyberAmber,
    background = AbyssBlack,
    onBackground = TextWhite,
    surface = SlateGray,
    onSurface = TextWhite,
    surfaceVariant = LightSlate,
    onSurfaceVariant = TextGray
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force modern elite dark dashboard
  dynamicColor: Boolean = false, // Disable default light dynamic accents to protect the custom stealth vibe
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
