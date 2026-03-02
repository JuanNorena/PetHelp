package com.pethelp.app.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Esquema de colores claro ──────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary             = PetHelpGreen,
    onPrimary           = White,
    primaryContainer    = LightGreenContainer,
    onPrimaryContainer  = DarkGreen,
    secondary           = WarmOrange,
    onSecondary         = White,
    secondaryContainer  = Color(0xFFFFCCBC),
    onSecondaryContainer= DarkSurface,
    tertiary            = SoftBlue,
    onTertiary          = White,
    tertiaryContainer   = Color(0xFFE1BEE7),
    background          = BackgroundLight,
    surface             = SurfaceLight,
    error               = ErrorRed,
    onBackground        = Color(0xFF1C1B1F),
    onSurface           = Color(0xFF1C1B1F),
)

// ── Esquema de colores oscuro ─────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = PetHelpGreenDark,
    onPrimary           = DarkSurface,
    primaryContainer    = DarkGreenContainer,
    onPrimaryContainer  = LightGreenContainer,
    secondary           = WarmOrangeDark,
    onSecondary         = DarkSurface,
    secondaryContainer  = Color(0xFFBF360C),
    onSecondaryContainer= White,
    tertiary            = SoftBlueDark,
    onTertiary          = DarkSurface,
    tertiaryContainer   = Color(0xFF7E57C2),
    background          = BackgroundDark,
    surface             = SurfaceDark,
    error               = ErrorRedDark,
    onBackground        = White,
    onSurface           = White,
)

/**
 * Tema principal de PetHelp.
 *
 * Sigue las guías de Material You:
 * - Soporte de Dynamic Color (Android 12+).
 * - Colores estáticos como fallback en versiones anteriores.
 * - Status bar con color coordinado con el tema.
 * - Formas muy redondeadas según la guía de diseño.
 */
@Composable
fun PetHelpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desactivado por defecto para mantener la identidad de marca de Figma
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PetHelpTypography,
        shapes      = PetHelpShapes,
        content     = content
    )
}
