package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors

/**
 * Bismillah ornament with decorative lines and center dots.
 */
@Composable
fun BismillahOrnament(
    modifier: Modifier = Modifier
) {
    val rc = LocalRafiqColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Decorative line with dots
        DecolineWithDots()

        Spacer(Modifier.height(6.dp))

        // Bismillah text
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = 3.sp,
                fontSize = 16.sp
            ),
            color = rc.gold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(6.dp))

        // Bottom decorative line
        DecolineWithDots()
    }
}

@Composable
private fun DecolineWithDots(modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .padding(horizontal = 40.dp)
    ) {
        val w = size.width
        val cy = size.height / 2f

        // Left line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, rc.gold.copy(alpha = 0.5f)),
                startX = 0f,
                endX = w * 0.42f
            ),
            start = Offset(0f, cy),
            end = Offset(w * 0.42f, cy),
            strokeWidth = 0.8f * density
        )

        // Right line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(rc.gold.copy(alpha = 0.5f), Color.Transparent),
                startX = w * 0.58f,
                endX = w
            ),
            start = Offset(w * 0.58f, cy),
            end = Offset(w, cy),
            strokeWidth = 0.8f * density
        )

        // Center 3 dots
        listOf(-8f, 0f, 8f).forEach { offset ->
            drawCircle(
                color = rc.gold.copy(alpha = 0.7f),
                radius = 1.8f * density,
                center = Offset(w / 2f + offset * density, cy)
            )
        }
    }
}

/**
 * Gold shimmer Quranic verse display.
 */
@Composable
fun QuranicVerseShimmer(
    verse: String,
    translation: String,
    modifier: Modifier = Modifier
) {
    val rc = LocalRafiqColors.current
    val transition = rememberInfiniteTransition(label = "verseShimmer")
    val shimmerOffset by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "verseShimmerOffset"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = verse,
            style = TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        rc.gold,
                        rc.gold.copy(alpha = 0.8f),
                        rc.gold.copy(alpha = 0.6f),
                        rc.gold.copy(alpha = 0.8f),
                        rc.gold
                    ),
                    startX = shimmerOffset * 500f,
                    endX = (shimmerOffset + 1f) * 500f
                ),
                letterSpacing = 1.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = translation,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = rc.inkMed,
            textAlign = TextAlign.Center
        )
    }
}
