package app.rafiqaldhikr.ui.screens.adhkar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.*

/* ══════════════════════════════════════════════════════════════
   DESIGN TOKENS
══════════════════════════════════════════════════════════════ */

/* Colors provided by LocalRafiqColors */

/* ══════════════════════════════════════════════════════════════
   CELEBRATION SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun CelebrationScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current
    val inf = rememberInfiniteTransition(label = "celebrate")

    val pulseScale by inf.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val shimmer by inf.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shimmer"
    )

    val rotation by inf.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "rot"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ═══ CELEBRATION ART ═══
            Box(
                Modifier.size(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(220.dp).scale(pulseScale)) {
                    val w = size.width; val cx = w / 2f; val cy = w / 2f

                    // Outer glow
                    drawCircle(
                        Brush.radialGradient(
                            listOf(rc.emeraldPastel, rc.emeraldPastel.copy(alpha = 0.2f), Color.Transparent),
                            Offset(cx, cy), w * 0.50f,
                        ),
                        w * 0.50f, Offset(cx, cy),
                    )

                    // Rotating ring
                    rotate(rotation, Offset(cx, cy)) {
                        drawCircle(rc.gold.copy(alpha = 0.15f), w * 0.42f, Offset(cx, cy),
                            style = Stroke(w * 0.006f))
                        for (i in 0 until 12) {
                            val a = (i * 30) * PI.toFloat() / 180f
                            drawCircle(rc.gold.copy(alpha = 0.20f), w * 0.010f,
                                Offset(cx + w * 0.42f * cos(a), cy + w * 0.42f * sin(a)))
                        }
                    }

                    // Inner circle (check)
                    drawCircle(
                        Brush.radialGradient(
                            listOf(rc.emerald, rc.emeraldMed),
                            Offset(cx, cy), w * 0.25f,
                        ),
                        w * 0.25f, Offset(cx, cy),
                    )

                    // Gold ring around check
                    drawCircle(
                        rc.gold.copy(alpha = 0.4f + shimmer * 0.3f),
                        w * 0.27f, Offset(cx, cy),
                        style = Stroke(w * 0.008f),
                    )

                    // Check mark
                    val checkPath = Path().apply {
                        moveTo(cx - w * 0.10f, cy)
                        lineTo(cx - w * 0.02f, cy + w * 0.08f)
                        lineTo(cx + w * 0.12f, cy - w * 0.08f)
                    }
                    drawPath(
                        checkPath, Color.White,
                        style = Stroke(w * 0.030f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )

                    // Sparkles around
                    val sparkles = listOf(
                        Offset(cx - w * 0.36f, cy - w * 0.26f),
                        Offset(cx + w * 0.38f, cy - w * 0.20f),
                        Offset(cx - w * 0.30f, cy + w * 0.30f),
                        Offset(cx + w * 0.32f, cy + w * 0.28f),
                        Offset(cx + w * 0.05f, cy - w * 0.40f),
                        Offset(cx - w * 0.10f, cy + w * 0.38f),
                    )
                    sparkles.forEachIndexed { i, pos ->
                        val sparkAlpha = (0.3f + shimmer * 0.4f + i * 0.05f).coerceAtMost(1f)
                        val sz = w * (0.014f + i * 0.002f)
                        drawLine(rc.goldLight.copy(alpha = sparkAlpha),
                            Offset(pos.x - sz, pos.y), Offset(pos.x + sz, pos.y),
                            w * 0.006f, StrokeCap.Round)
                        drawLine(rc.goldLight.copy(alpha = sparkAlpha),
                            Offset(pos.x, pos.y - sz), Offset(pos.x, pos.y + sz),
                            w * 0.006f, StrokeCap.Round)
                    }

                    // Mini crescents
                    drawCircle(rc.gold.copy(alpha = 0.3f), w * 0.022f,
                        Offset(cx + w * 0.20f, cy - w * 0.36f))
                    drawCircle(rc.bg, w * 0.016f,
                        Offset(cx + w * 0.21f, cy - w * 0.37f))
                }
            }

            Spacer(Modifier.height(32.dp))

            // ═══ TEXT ═══
            Text(
                "بارك الله فيك!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LocalRafiqColors.current.emerald,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "لقد أتممت الأذكار بنجاح\nجعلها الله في ميزان حسناتك",
                fontSize = 16.sp,
                color = LocalRafiqColors.current.inkMed,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "﴿ فَاذْكُرُونِي أَذْكُرْكُمْ ﴾",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LocalRafiqColors.current.gold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            // ═══ DONE BUTTON ═══
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(listOf(rc.emerald, rc.emeraldMed))
                    )
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "العودة",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalRafiqColors.current.card,
                )
            }
        }
    }
}
