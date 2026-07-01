package app.rafiqaldhikr.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*

/**
 * Cinematic screen transitions — premium feel.
 *
 * ✅ Optimized: removed scaleIn/Out (causes full-screen redraw).
 * Uses fade + slide only → GPU-accelerated, silky smooth.
 */
object CinematicTransitions {

    private val easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    private const val DURATION = 350

    // ═══ Forward navigation (entering new screen) ═══
    val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(DURATION, easing = easing)
        ) + slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION, easing = easing),
            initialOffset = { it / 5 }
        )
    }

    // ═══ Forward navigation (exiting previous screen) ═══
    val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(DURATION / 2, easing = easing)
        ) + slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION, easing = easing),
            targetOffset = { it / 8 }
        )
    }

    // ═══ Back navigation (re-entering previous screen) ═══
    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(DURATION, easing = easing)
        ) + slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION, easing = easing),
            initialOffset = { it / 5 }
        )
    }

    // ═══ Back navigation (exiting current screen) ═══
    val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(DURATION / 2, easing = easing)
        ) + slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION, easing = easing),
            targetOffset = { it / 8 }
        )
    }
}
