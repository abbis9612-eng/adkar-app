package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.*

/**
 * Islamic Shamsa (Geometric Medallion) — continuously rotating
 * with layered star polygons, concentric circles, and tick marks.
 * A moving light ray effect simulates real sunlight.
 *
 * ✅ Optimized:
 *   - graphicsLayer rotation instead of Canvas rotate() → GPU-only
 *   - Reduced tick marks from 48 to 24 (saves ~50% draw calls)
 *   - Removed glowAlpha animation (static glow looks identical)
 *   - Light ray kept — most visually impactful animation
 */
@Composable
fun Shamsa(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    rotationSpeed: Float = 0.3f
) {
    val transition = rememberInfiniteTransition(label = "shamsa")

    // Main rotation — applied via graphicsLayer (GPU-only, no recomposition)
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (60000 / rotationSpeed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "shamsaRotation"
    )

    // Light ray angle — the most impactful visual effect, worth keeping
    val lightAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        ),
        label = "lightAngle"
    )

    // ✅ Static glow — was animated 0.3→0.7, imperceptible difference
    val glowAlpha = 0.5f

    val rc = LocalRafiqColors.current
    val gold = rc.gold
    val emerald = rc.emerald

    Canvas(
        modifier = modifier
            .size(size)
            // ✅ Rotation via graphicsLayer → hardware-accelerated, no relayout
            .graphicsLayer { rotationZ = rotation }
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f

        // ═══ Background radial glow — static ═══
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    gold.copy(alpha = glowAlpha * 0.5f),
                    gold.copy(alpha = glowAlpha * 0.2f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 1.3f
            ),
            radius = radius * 1.3f,
            center = center
        )

        // ═══ Light ray effect (counter-rotates to appear stationary) ═══
        val effectiveAngle = lightAngle - rotation
        val lightRad = Math.toRadians(effectiveAngle.toDouble())
        val lightX = center.x + cos(lightRad).toFloat() * radius * 0.5f
        val lightY = center.y + sin(lightRad).toFloat() * radius * 0.5f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(lightX, lightY),
                radius = radius * 0.6f
            ),
            radius = radius * 0.6f,
            center = Offset(lightX, lightY)
        )

        // ═══ 24 tick marks (was 48 — halved, looks identical) ═══
        for (i in 0 until 24) {
            val angle = Math.toRadians(i * 15.0)
            val isMajor = i % 3 == 0
            val innerR = radius * if (isMajor) 0.88f else 0.91f
            val outerR = radius * 0.95f
            val strokeW = if (isMajor) 1.5f else 0.7f

            drawLine(
                color = gold.copy(alpha = if (isMajor) 0.8f else 0.4f),
                start = Offset(
                    center.x + cos(angle).toFloat() * innerR,
                    center.y + sin(angle).toFloat() * innerR
                ),
                end = Offset(
                    center.x + cos(angle).toFloat() * outerR,
                    center.y + sin(angle).toFloat() * outerR
                ),
                strokeWidth = strokeW * density
            )
        }

        // ═══ Concentric circles ═══
        val circleRadii = listOf(0.88f, 0.70f, 0.50f, 0.35f)
        circleRadii.forEachIndexed { idx, r ->
            drawCircle(
                color = gold.copy(alpha = 0.4f + idx * 0.08f),
                radius = radius * r,
                center = center,
                style = Stroke(
                    width = if (idx % 2 == 0) 1f * density else 0.7f * density,
                    pathEffect = if (idx % 2 == 1) PathEffect.dashPathEffect(
                        floatArrayOf(4f * density, 3f * density)
                    ) else null
                )
            )
        }

        // ═══ 16-point star ═══
        drawStarPolygon(center, radius * 0.85f, 16, gold.copy(alpha = 0.3f), false)

        // ═══ 12-point star ═══
        drawStarPolygon(center, radius * 0.68f, 12, gold.copy(alpha = 0.4f), false)

        // ═══ 8-point star (filled) ═══
        drawStarPolygon(center, radius * 0.48f, 8, gold.copy(alpha = 0.25f), true)

        // ═══ 6-point star (filled) ═══
        drawStarPolygon(center, radius * 0.33f, 6, gold.copy(alpha = 0.3f), true)

        // ═══ Center disc ═══
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    gold.copy(alpha = 0.8f),
                    gold.copy(alpha = 0.5f),
                    emerald.copy(alpha = 0.4f)
                ),
                center = center,
                radius = radius * 0.18f
            ),
            radius = radius * 0.18f,
            center = center
        )
        drawCircle(
            color = gold.copy(alpha = 0.7f),
            radius = radius * 0.18f,
            center = center,
            style = Stroke(width = 1.5f * density)
        )

        // ═══ Inner dot ═══
        drawCircle(
            color = gold,
            radius = radius * 0.04f,
            center = center
        )
    }
}

/**
 * Draw a star polygon with given number of points.
 */
private fun DrawScope.drawStarPolygon(
    center: Offset,
    radius: Float,
    points: Int,
    color: Color,
    filled: Boolean
) {
    val path = Path()
    val innerRadius = radius * 0.5f

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = Math.toRadians((i * 180.0 / points) - 90.0)
        val x = center.x + cos(angle).toFloat() * r
        val y = center.y + sin(angle).toFloat() * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    if (filled) {
        drawPath(path, color)
    }
    drawPath(path, color.copy(alpha = (color.alpha * 1.5f).coerceAtMost(1f)), style = Stroke(width = 0.8f * density))
}
