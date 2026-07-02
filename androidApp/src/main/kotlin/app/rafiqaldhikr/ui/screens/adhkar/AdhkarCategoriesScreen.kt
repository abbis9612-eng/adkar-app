package app.rafiqaldhikr.ui.screens.adhkar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*
import app.rafiqaldhikr.ui.components.RafiqBackButton

/* ══════════════════════════════════════════════════════════════
   DESIGN TOKENS
══════════════════════════════════════════════════════════════ */

/* General colors provided by LocalRafiqColors */

// ألوان الأقسام تأتي من RafiqPalette (فاتح/داكن) عبر AdhkarAccent

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

@Composable
private fun IconArrow(size: Dp = 14.dp, color: Color = LocalRafiqColors.current.inkLight) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        drawPath(Path().apply {
            moveTo(w * 0.65f, h * 0.15f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.65f, h * 0.85f)
        }, color, style = Stroke(w * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// Category-specific icons
@Composable
private fun AdhkarCategoryIcon(type: Int, color: Color, size: Dp = 28.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        when (type) {
            0 -> { // Sun (morning)
                drawCircle(color.copy(alpha = 0.15f), w * 0.22f, Offset(cx, cy))
                drawCircle(color, w * 0.22f, Offset(cx, cy), style = st)
                for (i in 0 until 8) {
                    val a = (i * 45) * PI.toFloat() / 180f
                    drawLine(color, Offset(cx + w * 0.30f * cos(a), cy + w * 0.30f * sin(a)),
                        Offset(cx + w * 0.42f * cos(a), cy + w * 0.42f * sin(a)), w * 0.05f, StrokeCap.Round)
                }
            }
            1 -> { // Moon (evening)
                val moonPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            Offset(cx - w * 0.28f, cy - w * 0.28f), w * 0.28f
                        )
                    )
                }
                val cutPath = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            Offset(cx + w * 0.08f - w * 0.22f, cy - w * 0.05f - w * 0.22f), w * 0.22f
                        )
                    )
                }
                val crescent = Path().apply { op(moonPath, cutPath, PathOperation.Difference) }
                drawPath(crescent, color.copy(alpha = 0.15f))
                drawPath(crescent, color, style = st)
                drawCircle(color, w * 0.03f, Offset(cx + w * 0.28f, cy - w * 0.22f))
                drawCircle(color, w * 0.025f, Offset(cx + w * 0.35f, cy - w * 0.08f))
            }
            2 -> { // Star (sleep)
                val points = 5
                val outer = w * 0.38f; val inner = w * 0.18f
                val star = Path().apply {
                    for (i in 0 until points * 2) {
                        val r = if (i % 2 == 0) outer else inner
                        val a = (i * 36 - 90) * PI.toFloat() / 180f
                        val px = cx + r * cos(a); val py = cy + r * sin(a)
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }; close()
                }
                drawPath(star, color.copy(alpha = 0.12f))
                drawPath(star, color, style = st)
            }
            3 -> { // Mosque (prayer)
                drawArc(color, 180f, 180f, false,
                    Offset(w * 0.20f, cy - w * 0.12f), Size(w * 0.60f, w * 0.40f), style = st)
                drawRect(color.copy(alpha = 0.08f), Offset(w * 0.18f, cy + w * 0.08f), Size(w * 0.64f, w * 0.28f))
                drawRect(color, Offset(w * 0.18f, cy + w * 0.08f), Size(w * 0.64f, w * 0.28f), style = st)
                drawCircle(color, w * 0.04f, Offset(cx, cy - w * 0.16f))
                drawLine(color, Offset(w * 0.10f, cy + w * 0.36f), Offset(w * 0.90f, cy + w * 0.36f), w * 0.05f, StrokeCap.Round)
            }
            5 -> { // Dua hands (istighfar) — matches React IcoDua
                val stH = Stroke(w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                // Left palm
                val leftHand = Path().apply {
                    moveTo(w * 0.29f, w * 0.71f)
                    cubicTo(w * 0.21f, w * 0.59f, w * 0.21f, w * 0.44f, w * 0.21f, w * 0.38f)
                    cubicTo(w * 0.21f, w * 0.26f, w * 0.32f, w * 0.21f, w * 0.38f, w * 0.21f)
                    cubicTo(w * 0.44f, w * 0.21f, w * 0.44f, w * 0.26f, w * 0.44f, w * 0.29f)
                    lineTo(w * 0.44f, w * 0.56f)
                }
                drawPath(leftHand, color, style = stH)
                // Right palm
                val rightHand = Path().apply {
                    moveTo(w * 0.71f, w * 0.71f)
                    cubicTo(w * 0.79f, w * 0.59f, w * 0.79f, w * 0.44f, w * 0.79f, w * 0.38f)
                    cubicTo(w * 0.79f, w * 0.26f, w * 0.68f, w * 0.21f, w * 0.62f, w * 0.21f)
                    cubicTo(w * 0.56f, w * 0.21f, w * 0.56f, w * 0.26f, w * 0.56f, w * 0.29f)
                    lineTo(w * 0.56f, w * 0.56f)
                }
                drawPath(rightHand, color, style = stH)
                // Connecting cup
                drawPath(Path().apply {
                    moveTo(w * 0.44f, w * 0.56f)
                    cubicTo(w * 0.44f, w * 0.62f, w * 0.56f, w * 0.62f, w * 0.56f, w * 0.56f)
                }, color, style = stH)
                // Bottom curve
                drawPath(Path().apply {
                    moveTo(w * 0.29f, w * 0.71f)
                    cubicTo(w * 0.38f, w * 0.82f, w * 0.62f, w * 0.82f, w * 0.71f, w * 0.71f)
                }, color, style = stH)
                // Dua dot
                drawCircle(color, w * 0.04f, Offset(cx, w * 0.42f))
            }
            else -> { // Sparkle (misc)
                drawLine(color, Offset(cx, cy - w * 0.36f), Offset(cx, cy + w * 0.36f), w * 0.06f, StrokeCap.Round)
                drawLine(color, Offset(cx - w * 0.36f, cy), Offset(cx + w * 0.36f, cy), w * 0.06f, StrokeCap.Round)
                for (i in 0 until 4) {
                    val a = (i * 90 + 45) * PI.toFloat() / 180f
                    drawLine(color, Offset(cx + w * 0.14f * cos(a), cy + w * 0.14f * sin(a)),
                        Offset(cx + w * 0.28f * cos(a), cy + w * 0.28f * sin(a)), w * 0.05f, StrokeCap.Round)
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION
══════════════════════════════════════════════════════════════ */

@Composable
private fun GeomDecoration(
    sizeDp: Dp = 160.dp,
    color: Color = LocalRafiqColors.current.gold.copy(alpha = 0.10f),
    spinDuration: Int = 90_000,
    modifier: Modifier = Modifier,
) {
    val tr = rememberInfiniteTransition(label = "geom")
    val rotation by tr.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(spinDuration, easing = LinearEasing)),
        label = "geomRot"
    )
    Canvas(modifier = modifier.size(sizeDp)) {
        val sz = this.size.width; val cx = sz / 2f; val cy = sz / 2f
        rotate(rotation, pivot = Offset(cx, cy)) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (i * 60 - 90) * PI.toFloat() / 180f; val r = sz * 0.43f
                    if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                    else lineTo(cx + r * cos(a), cy + r * sin(a))
                }; close()
            }
            drawPath(hex, color, style = Stroke(1.2f))
            drawCircle(color, sz * 0.44f, Offset(cx, cy), style = Stroke(0.7f))
            for (i in 0 until 6) {
                val a = (i * 60 - 90) * PI.toFloat() / 180f
                drawLine(color,
                    Offset(cx + sz * 0.27f * cos(a), cy + sz * 0.27f * sin(a)),
                    Offset(cx + sz * 0.43f * cos(a), cy + sz * 0.43f * sin(a)),
                    1.1f)
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   CATEGORY DATA
══════════════════════════════════════════════════════════════ */

private enum class AdhkarAccent { GOLD, INDIGO, PURPLE, GREEN, BROWN }

private data class AdhkarCatDef(
    val key: String,
    val label: String,
    val description: String,
    val accent: AdhkarAccent,
    val iconType: Int,
)

@Composable
private fun AdhkarAccent.colors(): Pair<Color, Color> {
    val rc = LocalRafiqColors.current
    return when (this) {
        AdhkarAccent.GOLD   -> rc.accentGoldBg   to rc.accentGold
        AdhkarAccent.INDIGO -> rc.accentIndigoBg to rc.accentIndigo
        AdhkarAccent.PURPLE -> rc.accentPurpleBg to rc.accentPurple
        AdhkarAccent.GREEN  -> rc.emeraldPastel  to rc.emerald
        AdhkarAccent.BROWN  -> rc.accentBrownBg  to rc.accentBrown
    }
}

private val ADHKAR_CATS = listOf(
    AdhkarCatDef("morning", "أذكار الصباح", "ابدأ يومك بذكر الله", AdhkarAccent.GOLD, 0),
    AdhkarCatDef("evening", "أذكار المساء", "اختم يومك بذكر الله", AdhkarAccent.INDIGO, 1),
    AdhkarCatDef("sleep",   "أذكار النوم",  "أذكار النوم والاستيقاظ", AdhkarAccent.PURPLE, 2),
    AdhkarCatDef("istighfar", "الاستغفار",   "استغفر الله العظيم", AdhkarAccent.GREEN, 5),
    AdhkarCatDef("prayer",  "أذكار الصلاة", "أذكار بعد الصلاة", AdhkarAccent.GREEN, 3),
)

/* ══════════════════════════════════════════════════════════════
   MAIN SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun AdhkarCategoriesScreen(
    navController: NavHostController,
    viewModel: AdhkarCategoriesViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val dbCategories = viewModel.categories

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        // Background decoration
        GeomDecoration(
            sizeDp = 220.dp,
            color = LocalRafiqColors.current.gold.copy(alpha = 0.06f),
            spinDuration = 100_000,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "الأذكار",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalRafiqColors.current.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            // Subtitle
            Text(
                "اختر نوع الذكر",
                fontSize = 14.sp,
                color = LocalRafiqColors.current.inkMed,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ═══ HERO CARD ═══
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp),
                        ambientColor = LocalRafiqColors.current.emerald.copy(alpha = 0.18f))
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.linearGradient(
                                listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                            )
                        )
                )

                GeomDecoration(
                    sizeDp = 180.dp,
                    color = LocalRafiqColors.current.goldLight.copy(alpha = 0.12f),
                    spinDuration = 80_000,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 40.dp, y = (-30).dp)
                        .graphicsLayer { alpha = 0.3f },
                )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    Text(
                        "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                        fontSize = 14.sp,
                        color = LocalRafiqColors.current.goldLight.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "أَلَا بِذِكْرِ اللَّهِ\nتَطْمَئِنُّ الْقُلُوبُ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 34.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "— سورة الرعد ٢٨",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══ CATEGORY CARDS ═══
            // Use DB categories if available, fallback to ADHKAR_CATS
            val catsToShow = if (dbCategories.isNotEmpty()) {
                dbCategories.map { (key, label) ->
                    ADHKAR_CATS.find { it.key == key }
                        ?: AdhkarCatDef(key, label, "أذكار متنوعة", AdhkarAccent.BROWN, 4)
                }
            } else {
                ADHKAR_CATS
            }

            catsToShow.forEach { cat ->
                AdhkarCategoryCard(
                    cat = cat,
                    onClick = { navController.navigate(RafiqRoute.DhikrReading.withCategory(cat.key)) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   CATEGORY CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun AdhkarCategoryCard(
    cat: AdhkarCatDef,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val (catBg, catClr) = cat.accent.colors()
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon circle
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(catBg),
                contentAlignment = Alignment.Center,
            ) {
                AdhkarCategoryIcon(cat.iconType, catClr, 26.dp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    cat.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalRafiqColors.current.ink,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    cat.description,
                    fontSize = 13.sp,
                    color = LocalRafiqColors.current.inkMed,
                )
            }

            IconArrow()
        }
    }
}
