package com.pethelp.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas (Shapes) oficial para la aplicación PetHelp.
 *
 * **Responsabilidad:**
 * Define la estrategia de redondeo de esquinas para todos los componentes de la interfaz.
 * Centraliza las dimensiones de los bordes para asegurar una estética coherente, moderna
 * y amigable en toda la aplicación.
 *
 * **Propósito y Diseño:**
 * PetHelp utiliza una guía de diseño basada en "bordes muy redondeados" (smooth roundings).
 * Esto ayuda a transmitir una sensación de cercanía, suavidad y cuidado, valores centrales
 * en una aplicación orientada al bienestar animal.
 *
 * **Categorías de Formas:**
 * El sistema se basa en 5 niveles de redondeo:
 * 1. **extraSmall (8dp)**: Para componentes muy pequeños como etiquetas o badges.
 * 2. **small (12dp)**: Para campos de texto (TextFields) y botones pequeños.
 * 3. **medium (16dp)**: El estándar para tarjetas (Cards) y la mayoría de contenedores.
 * 4. **large (24dp)**: Para diálogos, hojas inferiores (Bottom Sheets) y secciones destacadas.
 * 5. **extraLarge (32dp)**: Para elementos visuales de gran tamaño o banners promocionales.
 *
 * **Ejemplo de Uso:**
 * ```kotlin
 * Card(
 *     shape = MaterialTheme.shapes.medium, // Aplica el redondeo de 16dp
 *     // ...
 * ) {
 *     // Contenido
 * }
 * ```
 *
 * **Notas para Junior Developers:**
 * - No definas `RoundedCornerShape` con valores fijos en tus componentes. Usa siempre
 *   `MaterialTheme.shapes` para que la app se adapte automáticamente si el diseño cambia.
 * - Si necesitas un círculo perfecto para una imagen de perfil, usa `CircleShape` en lugar de estos valores.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 * @see RoundedCornerShape para entender cómo se aplican los bordes en Compose.
 */
val PetHelpShapes = Shapes(
    /** Redondeo extra pequeño de 8dp. Ideal para chips y mini-botones. */
    extraSmall = RoundedCornerShape(8.dp),
    /** Redondeo pequeño de 12dp. Usado frecuentemente en inputs de formularios. */
    small      = RoundedCornerShape(12.dp),
    /** Redondeo medio de 16dp. Es la forma estándar para las tarjetas de la app. */
    medium     = RoundedCornerShape(16.dp),
    /** Redondeo grande de 24dp. Aplicado en diálogos y componentes de navegación. */
    large      = RoundedCornerShape(24.dp),
    /** Redondeo extra grande de 32dp. Reservado para contenedores principales y banners. */
    extraLarge = RoundedCornerShape(32.dp)
)
