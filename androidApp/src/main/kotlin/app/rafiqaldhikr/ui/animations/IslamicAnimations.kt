package app.rafiqaldhikr.ui.animations

import androidx.compose.animation.core.RepeatMode
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
import app.rafiqaldhikr.ui.theme.stillableFloat

// ═══════════════════════════════════════
// 1. GOLD SHIMMER — Animated gradient on text/elements
// ═══════════════════════════════════════
fun Modifier.goldShimmer(
    durationMs: Int = 3000
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val offsetX by stillableFloat(
        initialValue = -size.width.toFloat(),
        targetValue  = 2f * size.width.toFloat(),
        durationMs   = durationMs,
        easing       = LinearEasing,
        label        = "goldShimmer",
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
    val scale by stillableFloat(
        initialValue = minScale,
        targetValue  = maxScale,
        durationMs   = durationMs,
        easing       = FastOutSlowInEasing,
        repeatMode   = RepeatMode.Reverse,
        label        = "breathe",
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

// ═══════════════════════════════════════
// 4. STAGGERED ENTRANCE — Fade + Slide with delays
// ═══════════════════════════════════════

// ═══════════════════════════════════════
// 5. RIPPLE TOUCH ANIMATION — Scale bounce on press
// ═══════════════════════════════════════

// ═══════════════════════════════════════
// 6. FLOATING PARTICLES — Computed in draw phase only (NO recomposition!)
// ═══════════════════════════════════════


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
