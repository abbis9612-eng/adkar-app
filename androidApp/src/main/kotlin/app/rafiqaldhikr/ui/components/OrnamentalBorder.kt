package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * Ornamental gold border — Islamic teardrop motifs with connecting line.
 * @param flipped if true, the border is drawn as a bottom border.
 */
@Composable
fun OrnamentalBorder(
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    flipped: Boolean = false
) {
    val rc = LocalRafiqColors.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        // ═══ Main horizontal line ═══
        val lineY = if (flipped) h * 0.92f else h * 0.08f
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    rc.gold.copy(alpha = 0.6f),
                    rc.gold.copy(alpha = 0.8f),
                    rc.gold.copy(alpha = 0.6f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, lineY),
            end = Offset(w, lineY),
            strokeWidth = 1.2f * density
        )

        // ═══ Repeating teardrop motifs ═══
        val motifCount = ((w / (34f * density)).toInt()).coerceAtLeast(5)
        val spacing = w / (motifCount + 1)

        for (i in 1..motifCount) {
            val cx = spacing * i
            val motifCenterY = h * 0.5f

            // Teardrop ellipse
            drawOval(
                color = rc.gold.copy(alpha = 0.5f),
                topLeft = Offset(cx - 4f * density, motifCenterY - 7f * density),
                size = Size(8f * density, 14f * density),
                style = Stroke(width = 0.8f * density)
            )

            // Dot (top for normal, bottom for flipped)
            val dotY = if (flipped) motifCenterY + 10f * density else motifCenterY - 10f * density
            drawCircle(
                color = rc.gold.copy(alpha = 0.7f),
                radius = 1.5f * density,
                center = Offset(cx, dotY)
            )

            // Connector line to main line
            val connStart = if (flipped) motifCenterY + 7f * density else motifCenterY - 7f * density
            drawLine(
                color = rc.gold.copy(alpha = 0.35f),
                start = Offset(cx, connStart),
                end = Offset(cx, lineY),
                strokeWidth = 0.6f * density
            )
        }

        // ═══ Corner diamonds ═══
        val diamondSize = 5f * density
        listOf(18f * density, w - 18f * density).forEach { dx ->
            val dy = h * 0.5f
            val path = Path().apply {
                moveTo(dx, dy - diamondSize)
                lineTo(dx + diamondSize, dy)
                lineTo(dx, dy + diamondSize)
                lineTo(dx - diamondSize, dy)
                close()
            }
            drawPath(path, rc.gold.copy(alpha = 0.6f))
            drawPath(path, rc.gold.copy(alpha = 0.8f), style = Stroke(0.8f * density))
        }
    }
}

/**
 * Decorative section divider with Islamic geometric pattern.
 * Uses a center diamond with radiating lines and flanking dots.
 */
@Composable
fun IslamicDivider(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    val rc = LocalRafiqColors.current

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val cy = h / 2f

        // Side lines (fade toward center)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, rc.gold.copy(alpha = 0.4f)),
                startX = w * 0.1f,
                endX = w * 0.42f
            ),
            start = Offset(w * 0.1f, cy),
            end = Offset(w * 0.42f, cy),
            strokeWidth = 0.8f * density
        )
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(rc.gold.copy(alpha = 0.4f), Color.Transparent),
                startX = w * 0.58f,
                endX = w * 0.9f
            ),
            start = Offset(w * 0.58f, cy),
            end = Offset(w * 0.9f, cy),
            strokeWidth = 0.8f * density
        )

        // Center diamond
        val ds = 6f * density
        val diamondPath = Path().apply {
            moveTo(w / 2f, cy - ds)
            lineTo(w / 2f + ds, cy)
            lineTo(w / 2f, cy + ds)
            lineTo(w / 2f - ds, cy)
            close()
        }
        drawPath(diamondPath, rc.gold.copy(alpha = 0.5f))
        drawPath(diamondPath, rc.gold.copy(alpha = 0.7f), style = Stroke(0.8f * density))

        // Small dots beside diamond
        listOf(-16f, 16f).forEach { offset ->
            drawCircle(
                color = rc.gold.copy(alpha = 0.5f),
                radius = 2f * density,
                center = Offset(w / 2f + offset * density, cy)
            )
        }
    }
}
