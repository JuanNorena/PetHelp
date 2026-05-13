package com.pethelp.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset

// ═══════════════════════════════════════════════════════════════════════════════
// PETHELP ANIMATIONS — Utilidades de animación reutilizables para toda la app
// ═══════════════════════════════════════════════════════════════════════════════

/** Duración estándar para animaciones de entrada de elementos */
const val PETHELP_ANIM_DURATION = 350

/** Duración rápida para micro-interacciones */
const val PETHELP_ANIM_FAST = 200

/** Duración lenta para transiciones de navegación */
const val PETHELP_ANIM_SLOW = 450

/** Delay escalonado entre items de lista */
const val PETHELP_STAGGER_DELAY = 60

/** Easing por defecto para animaciones suaves */
val PETHELP_EASING = FastOutSlowInEasing

/** Especificación de spring para rebote suave */
val PETHELP_SPRING = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

// ═══════════════════════════════════════════════════════════════════════════════
// ENTRADAS DE ELEMENTOS
// ═══════════════════════════════════════════════════════════════════════════════

/** Fade + scale para cards y elementos de lista */
fun pethelpFadeScaleIn(
    duration: Int = PETHELP_ANIM_DURATION,
    delay: Int = 0
): EnterTransition = fadeIn(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    initialAlpha = 0f
) + scaleIn(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    initialScale = 0.92f
)

/** Slide vertical desde abajo + fade */
fun pethelpSlideUpFadeIn(
    duration: Int = PETHELP_ANIM_DURATION,
    delay: Int = 0
): EnterTransition = slideInVertically(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    initialOffsetY = { it / 4 }
) + fadeIn(
    animationSpec = tween(duration, delay, PETHELP_EASING)
)

/** Slide horizontal desde derecha + fade (para navegación) */
fun pethelpSlideRightFadeIn(
    duration: Int = PETHELP_ANIM_SLOW,
    delay: Int = 0
): EnterTransition = slideInHorizontally(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    initialOffsetX = { it }
) + fadeIn(
    animationSpec = tween(duration, delay, PETHELP_EASING)
)

/** Slide horizontal desde izquierda + fade */
fun pethelpSlideLeftFadeIn(
    duration: Int = PETHELP_ANIM_SLOW,
    delay: Int = 0
): EnterTransition = slideInHorizontally(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    initialOffsetX = { -it }
) + fadeIn(
    animationSpec = tween(duration, delay, PETHELP_EASING)
)

// ═══════════════════════════════════════════════════════════════════════════════
// SALIDAS DE ELEMENTOS
// ═══════════════════════════════════════════════════════════════════════════════

/** Fade + scale out */
fun pethelpFadeScaleOut(
    duration: Int = PETHELP_ANIM_FAST
): ExitTransition = fadeOut(
    animationSpec = tween(duration, easing = PETHELP_EASING)
) + scaleOut(
    animationSpec = tween(duration, easing = PETHELP_EASING),
    targetScale = 0.95f
)

/** Slide vertical hacia abajo + fade */
fun pethelpSlideDownFadeOut(
    duration: Int = PETHELP_ANIM_FAST
): ExitTransition = slideOutVertically(
    animationSpec = tween(duration, easing = PETHELP_EASING),
    targetOffsetY = { it / 4 }
) + fadeOut(
    animationSpec = tween(duration, easing = PETHELP_EASING)
)

/** Slide horizontal hacia derecha + fade (para navegación back) */
fun pethelpSlideRightFadeOut(
    duration: Int = PETHELP_ANIM_SLOW
): ExitTransition = slideOutHorizontally(
    animationSpec = tween(duration, easing = PETHELP_EASING),
    targetOffsetX = { it }
) + fadeOut(
    animationSpec = tween(duration, easing = PETHELP_EASING)
)

// ═══════════════════════════════════════════════════════════════════════════════
// EXPANDIR / COLAPSAR
// ═══════════════════════════════════════════════════════════════════════════════

/** Expandir verticalmente con fade */
fun pethelpExpandIn(
    duration: Int = PETHELP_ANIM_DURATION,
    delay: Int = 0
): EnterTransition = expandVertically(
    animationSpec = tween(duration, delay, PETHELP_EASING),
    expandFrom = androidx.compose.ui.Alignment.Top
) + fadeIn(
    animationSpec = tween(duration, delay, PETHELP_EASING)
)

/** Colapsar verticalmente con fade */
fun pethelpCollapseOut(
    duration: Int = PETHELP_ANIM_FAST
): ExitTransition = shrinkVertically(
    animationSpec = tween(duration, easing = PETHELP_EASING),
    shrinkTowards = androidx.compose.ui.Alignment.Top
) + fadeOut(
    animationSpec = tween(duration, easing = PETHELP_EASING)
)

// ═══════════════════════════════════════════════════════════════════════════════
// ANIMACIONES DE INTERACCIÓN
// ═══════════════════════════════════════════════════════════════════════════════

/** Heartbeat animation spec para favoritos */
fun pethelpHeartbeatSpec() = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMedium
)

/** Pop animation spec para votos y contadores */
fun pethelpPopSpec() = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSE REUTILIZABLES
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Wrapper animado para items de lista con delay escalonado.
 * Calcula el delay automáticamente basado en el índice.
 */
@Composable
fun PetHelpAnimatedListItem(
    index: Int,
    modifier: Modifier = androidx.compose.ui.Modifier,
    content: @Composable () -> Unit
) {
    val delay = index * PETHELP_STAGGER_DELAY
    AnimatedVisibility(
        visible = true,
        enter = pethelpFadeScaleIn(delay = delay),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Wrapper animado para contenido que aparece con slide up.
 */
@Composable
fun PetHelpAnimatedContent(
    visible: Boolean,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = pethelpSlideUpFadeIn(),
        exit = pethelpSlideDownFadeOut(),
        modifier = modifier
    ) {
        content()
    }
}
