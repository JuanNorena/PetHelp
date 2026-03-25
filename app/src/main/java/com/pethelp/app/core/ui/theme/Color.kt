package com.pethelp.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// ── Paleta principal — Primario (Turquesa PetHelp)
val PetHelpPrimary       = Color(0xFF00BCB4)   // Turquesa (#00BCB4)
val PetHelpPrimaryDark   = Color(0xFF009690)   // Active / Dark variant (#009690)
// contenedores derivados
val PrimaryLightContainer = Color(0xFFE5F8F7)  // Tint 10% simulado
val PrimaryDarkContainer  = Color(0xFF009690)  // Variante oscura
val OnPrimaryContainer    = Color(0xFF00BCB4)  // Texto sobre contenedor claro

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
val DarkSurface          = Color(0xFF2A2A2A)   // Alias para onPrimary/Secondary oscuro

// ── Auxiliares ────────────────────────────────────────────────────────────────
val White                = Color(0xFFFFFFFF)
val PetHelpDestructive   = Color(0xFFFF4747)   // Rojo destructivo (#FF4747)
val PetHelpDestructiveDark = Color(0xFFCC3333) // Rojo oscuro para tema oscuro
