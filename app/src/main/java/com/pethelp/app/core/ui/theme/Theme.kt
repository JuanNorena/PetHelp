/**
 * Configuración central del tema visual de PetHelp.
 *
 * Orquesta Material Design 3 con la paleta de colores, tipografía y formas
 * definidas en el design system. Gestiona automáticamente modo claro/oscuro.
 */
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Archivo de configuración central del tema visual para la aplicación PetHelp.
 *
 * **Responsabilidad:**
 * Orquestar el sistema de diseño de Material Design 3, integrando colores, tipografías y formas.
 * Gestiona automáticamente el cambio entre el modo claro y oscuro, además de configurar
 * elementos del sistema como la barra de estado.
 *
 * **Propósito:**
 * Asegurar que toda la aplicación respete la identidad visual definida en Figma, proporcionando
 * un punto de entrada único (`PetHelpTheme`) que envuelve la jerarquía de componentes.
 *
 * **Lógica de Funcionamiento:**
 * 1. Determina el esquema de colores a usar basado en la configuración del sistema (modo oscuro)
 *    y la disponibilidad de Dynamic Color (Android 12+).
 * 2. Aplica efectos secundarios (`SideEffect`) para sincronizar la barra de estado del dispositivo
 *    con el color primario del tema.
 * 3. Provee los valores de `Color`, `Typography` y `Shape` a través de los Local providers de Compose.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */

// ── Esquema de colores claro ──────────────────────────────────────────────────
/**
 * Define el mapeo de colores para el tema claro (Light Mode).
 *
 * Utiliza la paleta definida en [Color.kt], asignando cada color a un rol semántico
 * de Material 3 (primary, secondary, background, etc.).
 */
private val LightColorScheme = lightColorScheme(
    primary             = PetHelpPrimary,
    onPrimary           = White,
    primaryContainer    = PrimaryLightContainer,
    onPrimaryContainer  = OnPrimaryContainer,
    secondary           = PetHelpSecondary,
    onSecondary         = White,
    secondaryContainer  = SecondaryLightContainer,
    onSecondaryContainer= DarkSurface,
    tertiary            = PetHelpTertiary,
    onTertiary          = White,
    tertiaryContainer   = TertiaryLightContainer,
    background          = BackgroundLight,
    surface             = SurfaceLight,
    error               = PetHelpDestructive,
    onBackground        = TextPrimary,
    onSurface           = TextPrimary,
    outlineVariant      = PetHelpOutline,
    surfaceVariant      = SurfaceVariantLight,
)

// TODO: IMPLEMENTAR EL CAMBIO DE IDIOMA EN TODO EL PROYECTO

// ── Esquema de colores oscuro ─────────────────────────────────────────────────
/**
 * Define el mapeo de colores para el tema oscuro (Dark Mode).
 *
 * Ajusta los contrastes y tonos para asegurar la legibilidad y reducir la fatiga
 * visual en entornos de poca luz, siguiendo las variantes `Dark` de [Color.kt].
 */
private val DarkColorScheme = darkColorScheme(
    primary             = PetHelpPrimaryDark,
    onPrimary           = DarkSurface,
    primaryContainer    = PrimaryDarkContainer,
    onPrimaryContainer  = PrimaryLightContainer,
    secondary           = PetHelpSecondaryDark,
    onSecondary         = DarkSurface,
    secondaryContainer  = SecondaryDarkContainer,
    onSecondaryContainer= White,
    tertiary            = PetHelpTertiaryDark,
    onTertiary          = DarkSurface,
    tertiaryContainer   = TertiaryDarkContainer,
    background          = BackgroundDark,
    surface             = SurfaceDark,
    error               = PetHelpDestructiveDark,
    onBackground        = White,
    onSurface           = White,
    outlineVariant      = PetHelpOutlineDark,
    surfaceVariant      = SurfaceVariantDark,
)

/**
 * Componente Composable raíz que aplica el tema de PetHelp a su contenido.
 *
 * **Funcionalidad:**
 * Configura el entorno visual de la aplicación. Es el encargado de decidir si se usa
 * el modo oscuro, si se activan los colores dinámicos de Android 12 y de inyectar
 * las definiciones de [PetHelpTypography] y [PetHelpShapes].
 *
 * **Parámetros:**
 * @param darkTheme Determina si se aplica el esquema de colores oscuro. Por defecto
 * toma el valor del sistema usando [isSystemInDarkTheme()].
 * @param dynamicColor Si es `true` y el dispositivo corre Android 12+, utiliza los colores
 * del fondo de pantalla del usuario. Se mantiene en `false` por defecto para preservar
 * la identidad visual de la marca.
 * @param content El bloque de contenido (otros Composables) que se renderizará con este tema.
 *
 * **Ejemplo de Uso en MainActivity:**
 * ```kotlin
 * setContent {
 *     PetHelpTheme {
 *         MainScreen()
 *     }
 * }
 * ```
 *
 * **Notas para Junior Developers:**
 * - Envuelve siempre tu `Scaffold` o pantalla principal con este tema.
 * - Dentro del contenido de `PetHelpTheme`, puedes acceder a los colores usando
 *   `MaterialTheme.colorScheme.primary`, etc.
 * - El código dentro de `SideEffect` se encarga de que la barra de notificaciones del celular
 *   combine perfectamente con el color de tu app.
 *
 * @see LightColorScheme para el detalle de colores en modo claro.
 * @see DarkColorScheme para el detalle de colores en modo oscuro.
 * @see PetHelpShapes para las formas de los componentes.
 * @see PetHelpTypography para los estilos de texto.
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
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
