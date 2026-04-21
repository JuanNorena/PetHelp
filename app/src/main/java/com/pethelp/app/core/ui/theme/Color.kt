/**
 * Archivo de definición de la paleta de colores oficial para la aplicación PetHelp.
 *
 * **Responsabilidad:**
 * Este archivo centraliza todos los valores cromáticos utilizados en la interfaz de usuario.
 * Su objetivo es asegurar la consistencia visual en toda la aplicación, facilitando el
 * mantenimiento y la implementación de temas claro (Light) y oscuro (Dark).
 *
 * **Organización:**
 * Los colores están agrupados por su función semántica:
 * 1. **Paleta Principal**: Colores de marca (Turquesa y Naranja).
 * 2. **Superficies**: Colores para fondos, tarjetas y separadores.
 * 3. **Tipografía**: Escala de grises para diferentes niveles de lectura.
 * 4. **Semánticos**: Colores que indican estados (Éxito, Error, Advertencia).
 * 5. **Estilos de Mapa**: Configuraciones JSON para personalizar Google Maps.
 *
 * **Notas para Junior Developers:**
 * - No utilices valores hexadecimales directamente en tus Composables. Usa siempre los nombres
 *   definidos aquí o, preferiblemente, a través de `MaterialTheme.colorScheme`.
 * - Los nombres que terminan en `Dark` están diseñados específicamente para el modo oscuro.
 *
 * @since 1.0.0
 * @author Equipo de Desarrollo PetHelp
 */
package com.pethelp.app.core.ui.theme

import androidx.compose.ui.graphics.Color
// ── Paleta principal — Primario (Turquesa PetHelp)
/** Color de identidad principal. Un turquesa vibrante que transmite frescura y ayuda. */
val PetHelpPrimary       = Color(0xFF00BCB4)   // Turquesa (#00BCB4)
/** Variante más oscura del color primario para estados activos o presionados. */
val PetHelpPrimaryDark   = Color(0xFF009690)   // Active / Dark variant (#009690)

// Contenedores derivados
/** Fondo suave para componentes que usan el color primario (ej. tarjetas de información). */
val PrimaryLightContainer = Color(0xFFE5F8F7)  // Tint 10% simulado
/** Variante oscura para contenedores primarios en el tema Dark. */
val PrimaryDarkContainer  = Color(0xFF009690)  // Variante oscura
/** Color de texto o iconos diseñado para leerse sobre [PrimaryLightContainer]. */
val OnPrimaryContainer    = Color(0xFF00BCB4)  // Texto sobre contenedor claro

/** Contenedor naranja muy suave para resaltar elementos secundarios. */
val SecondaryLightContainer = Color(0xFFFFEDD5) // Naranja muy suave
/** Variante oscura para contenedores secundarios. */
val SecondaryDarkContainer  = Color(0xFFCC9138) // Variante oscura

// ── Acento — Secundario (Naranja Cálido)
/** Color secundario de la marca. Un naranja cálido que evoca cercanía y energía. */
val PetHelpSecondary     = Color(0xFFFFB547)   // Naranja (#FFB547)
/** Variante oscura del naranja para mejor contraste en fondos claros o modo oscuro. */
val PetHelpSecondaryDark = Color(0xFFCC9138)   // Active / Dark variant (#CC9138)

// ── Terciario — Violeta Suave (IA, categorías secundarias)
/** Color terciario (Violeta). Se utiliza para funciones especiales o relacionadas con IA. */
val PetHelpTertiary      = Color(0xFFA78BFA)   // Violeta (#A78BFA)
/** Variante oscura del color terciario. */
val PetHelpTertiaryDark  = Color(0xFF8266D0)   // Variante oscura

// ── Fondos y superficies (ajustados a valores Figma)
/** Color de fondo base para la aplicación en modo claro. */
val BackgroundLight      = Color(0xFFFAFAFA)   // (#FAFAFA)
/** Color de fondo base para la aplicación en modo oscuro. */
val BackgroundDark       = Color(0xFF1A1A1A)   // (#1A1A1A)
/** Superficie blanca pura para tarjetas y diálogos en modo claro. */
val SurfaceLight         = Color(0xFFFFFFFF)   // (#FFFFFF)
/** Superficie gris oscuro para componentes en modo oscuro. */
val SurfaceDark          = Color(0xFF2A2A2A)   // (#2A2A2A)
/** Variante de superficie más clara para diferenciar capas visuales. */
val SurfaceVariantLight  = Color(0xFFF5F5F5)   // (#F5F5F5)
/** Variante de superficie para el modo oscuro. */
val SurfaceVariantDark   = Color(0xFF383838)
/** Alias para superficies oscuras persistentes. */
val DarkSurface          = Color(0xFF2A2A2A)   // Alias para onPrimary/Secondary oscuro

// ── Bordes y Separadores
/** Color estándar para líneas divisorias y bordes de inputs en modo claro. */
val PetHelpOutline       = Color(0xFFE0E0E0)   // Gris claro para bordes (#E0E0E0)
/** Color para bordes y separadores en modo oscuro. */
val PetHelpOutlineDark   = Color(0xFF404040)   // Gris oscuro para bordes

// ── Texto y Contenidos
/** Color para el texto principal. Un gris casi negro para máxima legibilidad. */
val TextPrimary          = Color(0xFF2D2D2D)   // (#2D2D2D)
/** Color para subtítulos o información secundaria de menor jerarquía. */
val TextSecondary        = Color(0xFF666666)   // (#666666)
/** Color para textos de ayuda (hints) o estados deshabilitados. */
val TextHint             = Color(0xFF9E9E9E)   // (#9E9E9E)

// ── Contenedores Terciarios (Específicos para avisos)
/** Fondo violeta muy claro para avisos o notificaciones informativas. */
val TertiaryLightContainer   = Color(0xFFF3E8FF)
/** Texto violeta oscuro optimizado para [TertiaryLightContainer]. */
val OnTertiaryLightContainer = Color(0xFF6B21A8)
/** Contenedor terciario para el tema oscuro. */
val TertiaryDarkContainer    = Color(0xFF7E57C2)

// ── Auxiliares ────────────────────────────────────────────────────────────────
/** Blanco absoluto. */
val White                = Color(0xFFFFFFFF)
/** Rojo vibrante para acciones peligrosas (Eliminar, Cancelar). */
val PetHelpDestructive   = Color(0xFFFF4747)   // Rojo destructivo (#FF4747)
/** Variante oscura del rojo destructivo. */
val PetHelpDestructiveDark = Color(0xFFCC3333) // Rojo oscuro para tema oscuro
/** Color específico para las burbujas de notificación (badges). */
val BadgeRed             = Color(0xFFFB2C36)

// ── Colores de Acento para Iconos y Categorías ────────────────────────────────
/** Azul para categorías generales o información técnica. */
val AccentBlue           = Color(0xFF3B82F6)
/** Verde para salud animal o naturaleza. */
val AccentGreen          = Color(0xFF10B981)
/** Púrpura para eventos o servicios premium. */
val AccentPurple         = Color(0xFF8B5CF6)
/** Rojo claro para alertas de urgencia moderada. */
val AccentRed            = Color(0xFFF87171)
/** Gris azulado para elementos neutrales. */
val AccentGray           = Color(0xFF64748B)
/** Turquesa alternativo para acentos visuales. */
val AccentTeal           = Color(0xFF2DD4BF)
/** Índigo para servicios comunitarios. */
val AccentIndigo         = Color(0xFF6366F1)
/** Ámbar para advertencias de nivel bajo. */
val AccentAmber          = Color(0xFFFBBF24)
/** Cian para información de contacto o soporte. */
val AccentCyan           = Color(0xFF06B6D4)

// ── Colores de UI y Fondos Específicos ────────────────────────────────────────
/** Superficie de elevación baja. */
val SurfaceLow           = Color(0xFFFAFAFA)
/** Superficie de elevación media. */
val SurfaceMedium        = Color(0xFFF3F4F6)
/** Superficie de elevación alta (resalta sobre el fondo). */
val SurfaceHigh          = Color(0xFFE5E7EB)
/** Inicio de gradientes suaves para fondos. */
val GradientStart        = Color(0xFFFAFAFA)
/** Fin de gradientes suaves para fondos (tono verdoso). */
val GradientEnd          = Color(0xFFE8F5E9)

// ── Colores de Texto y Semánticos ─────────────────────────────────────────────
/** Verde oscuro para indicar éxito (completado, guardado). */
val StatusSuccess        = Color(0xFF146C2E)
/** Fondo verde pálido para banners de éxito. */
val StatusSuccessBg      = Color(0xFFE8F5E9)
/** Borde verde suave para contenedores de éxito. */
val StatusSuccessBorder  = Color(0xFFC8E6C9)
/** Texto verde optimizado para legibilidad en fondos claros. */
val StatusSuccessText    = Color(0xFF388E3C)

// Status: Warning (Pending)
/** Marrón oscuro para advertencias (pendientes, en revisión). */
val StatusWarning        = Color(0xFF7A2E0E)
/** Fondo naranja pálido para advertencias. */
val StatusWarningBg      = Color(0xFFFFF4E5)

// Status: Error (Rejected)
/** Rojo oscuro para errores críticos o rechazos. */
val StatusError          = Color(0xFFA12622)
/** Fondo rojo muy claro para banners de error. */
val StatusErrorBg        = Color(0xFFFFECEB)
/** Rojo brillante para mensajes de validación de error. */
val ErrorText            = Color(0xFFB42318)

// Status: Info (Active)
/** Azul oscuro para información general o estados activos. */
val StatusInfo           = Color(0xFF0369A1)
/** Fondo azul claro para banners informativos. */
val StatusInfoBg         = Color(0xFFE0F2FE)

// Status: Indigo (Resolved)
/** Índigo oscuro para estados resueltos o finalizados. */
val StatusIndigo         = Color(0xFF4338CA)
/** Fondo índigo claro para estados resueltos. */
val StatusIndigoBg       = Color(0xFFEEF2FF)

// Status: Neutral (Paused)
/** Gris oscuro para estados neutrales o pausados. */
val StatusNeutral        = Color(0xFF4B5563)
/** Fondo gris claro para estados neutrales. */
val StatusNeutralBg      = Color(0xFFF3F4F6)

/** Color de borde por defecto para componentes UI. */
val BorderDefault        = Color(0xFFE5E7EB)
/** Variante más clara de bordes. */
val BorderLight          = Color(0xFFD1D5DC)

// Alias para compatibilidad con pantallas
/** Alias del color turquesa para facilitar la transición desde versiones antiguas. */
val Teal = Color(0xFF00BFA5)

/**
 * Objeto contenedor de configuraciones de estilo para mapas.
 *
 * **Propósito:**
 * Almacena cadenas de texto en formato JSON que Google Maps utiliza para cambiar
 * la apariencia visual del mapa (colores de calles, parques, agua, etc.).
 */
object MapStyles {
    /**
     * Estilo de mapa oscuro (Dark Mode).
     *
     * Define una paleta de colores oscuros con acentos en naranja y azul,
     * optimizada para reducir la fatiga visual en entornos de poca luz.
     *
     * **Configuración:**
     * - Fondo (Geometry): #242f3e
     * - Agua (Water): #17263c
     * - Parques (Parks): #263c3f
     */
    const val DARK = """
[
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
    """
}
