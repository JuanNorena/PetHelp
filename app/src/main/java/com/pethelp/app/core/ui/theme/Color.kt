package com.pethelp.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// ── Paleta principal — Primario (Turquesa PetHelp)
val PetHelpPrimary       = Color(0xFF00BCB4)   // Turquesa (#00BCB4)
val PetHelpPrimaryDark   = Color(0xFF009690)   // Active / Dark variant (#009690)

// Contenedores derivados
val PrimaryLightContainer = Color(0xFFE5F8F7)  // Tint 10% simulado
val PrimaryDarkContainer  = Color(0xFF009690)  // Variante oscura
val OnPrimaryContainer    = Color(0xFF00BCB4)  // Texto sobre contenedor claro

val SecondaryLightContainer = Color(0xFFFFEDD5) // Naranja muy suave
val SecondaryDarkContainer  = Color(0xFFCC9138) // Variante oscura

// ── Acento — Secundario (Naranja Cálido)
val PetHelpSecondary     = Color(0xFFFFB547)   // Naranja (#FFB547)
val PetHelpSecondaryDark = Color(0xFFCC9138)   // Active / Dark variant (#CC9138)

// ── Terciario — Violeta Suave (IA, categorías secundarias)
val PetHelpTertiary      = Color(0xFFA78BFA)   // Violeta (#A78BFA)
val PetHelpTertiaryDark  = Color(0xFF8266D0)   // Variante oscura

// ── Fondos y superficies (ajustados a valores Figma)
val BackgroundLight      = Color(0xFFFAFAFA)   // (#FAFAFA)
val BackgroundDark       = Color(0xFF1A1A1A)   // (#1A1A1A)
val SurfaceLight         = Color(0xFFFFFFFF)   // (#FFFFFF)
val SurfaceDark          = Color(0xFF2A2A2A)   // (#2A2A2A)
val SurfaceVariantLight  = Color(0xFFF5F5F5)   // (#F5F5F5)
val SurfaceVariantDark   = Color(0xFF383838)
val DarkSurface          = Color(0xFF2A2A2A)   // Alias para onPrimary/Secondary oscuro

// ── Bordes y Separadores
val PetHelpOutline       = Color(0xFFE0E0E0)   // Gris claro para bordes (#E0E0E0)
val PetHelpOutlineDark   = Color(0xFF404040)   // Gris oscuro para bordes

// ── Texto y Contenidos
val TextPrimary          = Color(0xFF2D2D2D)   // (#2D2D2D)
val TextSecondary        = Color(0xFF666666)   // (#666666)
val TextHint             = Color(0xFF9E9E9E)   // (#9E9E9E)

// ── Contenedores Terciarios (Específicos para avisos)
val TertiaryLightContainer   = Color(0xFFF3E8FF)
val OnTertiaryLightContainer = Color(0xFF6B21A8)
val TertiaryDarkContainer    = Color(0xFF7E57C2)

// ── Auxiliares ────────────────────────────────────────────────────────────────
val White                = Color(0xFFFFFFFF)
val PetHelpDestructive   = Color(0xFFFF4747)   // Rojo destructivo (#FF4747)
val PetHelpDestructiveDark = Color(0xFFCC3333) // Rojo oscuro para tema oscuro
val BadgeRed             = Color(0xFFFB2C36)

// ── Colores de Acento para Iconos y Categorías ────────────────────────────────
val AccentBlue           = Color(0xFF3B82F6)
val AccentGreen          = Color(0xFF10B981)
val AccentPurple         = Color(0xFF8B5CF6)
val AccentRed            = Color(0xFFF87171)
val AccentGray           = Color(0xFF64748B)
val AccentTeal           = Color(0xFF2DD4BF)
val AccentIndigo         = Color(0xFF6366F1)
val AccentAmber          = Color(0xFFFBBF24)
val AccentCyan           = Color(0xFF06B6D4)

// ── Colores de UI y Fondos Específicos ────────────────────────────────────────
val SurfaceLow           = Color(0xFFFAFAFA)
val SurfaceMedium        = Color(0xFFF3F4F6)
val SurfaceHigh          = Color(0xFFE5E7EB)
val GradientStart        = Color(0xFFFAFAFA)
val GradientEnd          = Color(0xFFE8F5E9)

// ── Colores de Texto y Semánticos ─────────────────────────────────────────────
val StatusSuccess        = Color(0xFF146C2E)
val StatusSuccessBg      = Color(0xFFE8F5E9)
val StatusSuccessBorder  = Color(0xFFC8E6C9)
val StatusSuccessText    = Color(0xFF388E3C)

// Status: Warning (Pending)
val StatusWarning        = Color(0xFF7A2E0E)
val StatusWarningBg      = Color(0xFFFFF4E5)

// Status: Error (Rejected)
val StatusError          = Color(0xFFA12622)
val StatusErrorBg        = Color(0xFFFFECEB)
val ErrorText            = Color(0xFFB42318)

// Status: Info (Active)
val StatusInfo           = Color(0xFF0369A1)
val StatusInfoBg         = Color(0xFFE0F2FE)

// Status: Indigo (Resolved)
val StatusIndigo         = Color(0xFF4338CA)
val StatusIndigoBg       = Color(0xFFEEF2FF)

// Status: Neutral (Paused)
val StatusNeutral        = Color(0xFF4B5563)
val StatusNeutralBg      = Color(0xFFF3F4F6)

val BorderDefault        = Color(0xFFE5E7EB)
val BorderLight          = Color(0xFFD1D5DC)

// Alias para compatibilidad con pantallas
val Teal = Color(0xFF00BFA5)

object MapStyles {
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
