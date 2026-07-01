package app.rafiqaldhikr.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.sin

// ═══════════════════════════════════════
// 1. GOLD SHIMMER — Animated gradient on text/elements
// ═══════════════════════════════════════
fun Modifier.goldShimmer(
    durationMs: Int = 3000
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "goldShimmer")
    val offsetX by transition.animateFloat(
        initialValue = -size.width.toFloat(),
        targetValue = 2f * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing)
        ),
        label = "shimmerX"
    )
    val gold = LocalRafiqColors.current.gold
    background(
        brush = Brush.linearGradient(
            colors = listOf(
                gold.copy(alpha = 0.4f),
                gold,
                gold.copy(alpha = 0.4f),
            ),
            start = Offset(offsetX, 0f),
            end = Offset(offsetX + size.width.toFloat(), 0f),
        )
    ).onGloballyPositioned { size = it.size }
}

// ═══════════════════════════════════════
// 2. BREATHING ANIMATION — Uses graphicsLayer (NO recomposition!)
// ═══════════════════════════════════════
fun Modifier.breathingAnimation(
    minScale: Float = 1f,
    maxScale: Float = 1.015f,
    durationMs: Int = 4000
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )
    // ✅ graphicsLayer — GPU-only, no recomposition, no relayout
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ═══════════════════════════════════════
// 3. GLOW PULSE — Animated shadow/glow
// ═══════════════════════════════════════
@Composable
fun rememberGlowAlpha(
    minAlpha: Float = 0.15f,
    maxAlpha: Float = 0.45f,
    durationMs: Int = 3000
): Float {
    val transition = rememberInfiniteTransition(label = "glowPulse")
    val alpha by transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    return alpha
}

// ═══════════════════════════════════════
// 4. STAGGERED ENTRANCE — Fade + Slide with delays
// ═══════════════════════════════════════
@Composable
fun rememberStaggeredVisibility(
    index: Int,
    delayPerItem: Int = 100,
    durationMs: Int = 600
): Pair<Float, Float> {
    var alpha by remember { mutableFloatStateOf(0f) }
    var translationY by remember { mutableFloatStateOf(30f) }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(
            durationMillis = durationMs,
            delayMillis = index * delayPerItem,
            easing = FastOutSlowInEasing
        ),
        label = "staggerAlpha"
    )
    val animatedTransY by animateFloatAsState(
        targetValue = translationY,
        animationSpec = tween(
            durationMillis = durationMs,
            delayMillis = index * delayPerItem,
            easing = FastOutSlowInEasing
        ),
        label = "staggerTransY"
    )

    LaunchedEffect(Unit) {
        alpha = 1f
        translationY = 0f
    }

    return Pair(animatedAlpha, animatedTransY)
}

// ═══════════════════════════════════════
// 5. RIPPLE TOUCH ANIMATION — Scale bounce on press
// ═══════════════════════════════════════
@Composable
fun rememberPressScale(): Pair<MutableState<Boolean>, Float> {
    val isPressed = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )
    return Pair(isPressed, scale)
}

// ═══════════════════════════════════════
// 6. FLOATING PARTICLES — Computed in draw phase only (NO recomposition!)
// ═══════════════════════════════════════

/**
 * Animates a single time value; particle positions are computed
 * inside drawBehind → zero recomposition, zero allocation per frame.
 */
@Composable
fun rememberParticleTime(): State<Float> {
    val transition = rememberInfiniteTransition(label = "particles")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing)
        ),
        label = "particleTime"
    )
}

/**
 * Draws gold particles directly in the draw phase.
 * No List allocation, no recomposition — only GPU redraw.
 */
fun Modifier.goldParticleOverlay(
    timeState: State<Float>,
    count: Int = 12
): Modifier = composed {
    val gold = LocalRafiqColors.current.gold
    drawBehind {
        val time = timeState.value
    for (i in 0 until count) {
        val seed = i * 137.5f
        val px = ((seed + time * (0.3f + i * 0.05f)) % 1000f) / 1000f
        val py = ((seed * 2.3f + time * (0.2f + i * 0.03f)) % 1000f) / 1000f
        val alpha = 0.15f + 0.25f * ((seed + time * 0.5f) % 360f / 360f)
        val radius = (1.5f + (i % 3) * 0.8f) * density

        drawCircle(
            color = gold.copy(alpha = alpha),
            radius = radius,
            center = Offset(px * size.width, py * size.height)
        )
    }
    }
}
