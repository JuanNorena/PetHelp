package com.pethelp.app.core.ui.theme

import androidx.compose.ui.graphics.Color

// ── Paleta principal — Verdes (Figma: verde salvia)
//    primario claro para interfaz diurna y variante oscura para nocturna
val PetHelpGreen         = Color(0xFF4CAF50)   // verde salvia (#4CAF50)
val PetHelpGreenDark     = Color(0xFF388E3C)   // variante oscura (#388E3C)
// contenedores derivados (usar en primaryContainer)
val LightGreenContainer  = Color(0xFFC8E6C9)   // tono pálido para superficies
val DarkGreenContainer   = Color(0xFF2E7D32)   // tono más saturado para tema oscuro
val DarkGreen            = Color(0xFF2E7D32)   // texto sobre contenedores claros

// ── Acento — Coral / naranja suave (Figma)
val WarmOrange           = Color(0xFFFF8A65)   // coral (#FF8A65)
val WarmOrangeDark       = Color(0xFFFF7043)   // variante oscurizada (#FF7043)

// ── Terciario — Púrpura pastel (IA, categorías secundarias)
val SoftBlue             = Color(0xFFB39DDB)   // púrpura (#B39DDB) según Figma
val SoftBlueDark         = Color(0xFF9575CD)   // variante oscura (#9575CD)

// ── Fondos y superficies (ajustados a valores Figma)
val BackgroundLight      = Color(0xFFFAFAFA)   // crema claro (#FAFAFA)
val BackgroundDark       = Color(0xFF121212)   // casi negro (#121212)
val SurfaceLight         = Color(0xFFFFFFFF)
val SurfaceDark          = Color(0xFF1E1E1E)   // superficie oscuro Figma
val DarkSurface          = Color(0xFF1E1E1E)

// ── Auxiliares ────────────────────────────────────────────────────────────────
val White                = Color(0xFFFFFFFF)
val ErrorRed             = Color(0xFFE57373)   // rojo pastel amable (#E57373)
val ErrorRedDark         = Color(0xFFD32F2F)   // rojo intenso para tema oscuro
