package com.pethelp.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Definición central del sistema tipográfico para la aplicación PetHelp.
 *
 * **Responsabilidad Principal:**
 * Establecer y centralizar la jerarquía visual de los textos en toda la aplicación,
 * siguiendo la escala tipográfica de Material Design 3. Su objetivo es garantizar
 * una lectura clara, jerarquizada y profesional en todas las pantallas.
 *
 * **Propósito y Estructura:**
 * La tipografía se divide en cinco categorías semánticas que ayudan a los desarrolladores
 * a elegir el estilo adecuado según el contexto:
 * 1. **Display**: Textos de gran tamaño para pantallas de bienvenida o énfasis extremo.
 * 2. **Headline**: Encabezados de sección importantes.
 * 3. **Title**: Títulos de componentes (tarjetas, diálogos, barras superiores).
 * 4. **Body**: Texto de lectura principal (descripciones, posts, mensajes).
 * 5. **Label**: Etiquetas pequeñas para botones, pies de foto e información auxiliar.
 *
 * **Lógica de Implementación:**
 * - Se utiliza la fuente predeterminada del sistema (`FontFamily.Default`) para asegurar
 *   un rendimiento óptimo y una integración nativa con el sistema operativo.
 * - Cada estilo define cuidadosamente el `fontSize`, `fontWeight`, `lineHeight` y `letterSpacing`.
 *
 * **Ejemplo de Uso Práctico:**
 * ```kotlin
 * Text(
 *     text = "Bienvenido a PetHelp",
 *     style = MaterialTheme.typography.headlineMedium
 * )
 * ```
 *
 * **Notas para Junior Developers:**
 * - No definas tamaños de fuente fijos (ej. `fontSize = 18.sp`) directamente en tus Composables.
 * - Usa siempre `MaterialTheme.typography.X` para que la app respete las preferencias de
 *   accesibilidad del usuario (como el escalado de texto del sistema).
 * - Si el diseño pide un texto más grueso, prefiere cambiar la categoría (ej. de `body` a `title`)
 *   en lugar de forzar un `fontWeight` manualmente.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see Typography para la clase base de Material 3.
 * @see TextStyle para entender las propiedades de cada estilo.
 */
val PetHelpTypography = Typography(
    // ── Títulos grandes (Display) ──────────────────────────────────────
    // Se usan para números grandes o palabras de gran impacto visual.
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 45.sp,
        lineHeight = 52.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 36.sp,
        lineHeight = 44.sp,
    ),
    // ── Encabezados (Headline) ──────────────────────────────────────────
    // Ideales para títulos de secciones o encabezados de páginas principales.
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
    ),
    // ── Títulos de componentes (Title) ────────────────────────────────
    // Para títulos dentro de tarjetas, diálogos o la barra superior (TopAppBar).
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // ── Cuerpo (Body) ───────────────────────────────────────────────
    // El estándar para todo el texto de lectura de la aplicación.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // ── Etiquetas (Label) ───────────────────────────────────────────────
    // Para botones, insignias (badges) o pies de imagen pequeños.
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)
