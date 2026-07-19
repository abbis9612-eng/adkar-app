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
import app.rafiqaldhikr.ui.components.IcoDua
import app.rafiqaldhikr.ui.components.IcoMoon
import app.rafiqaldhikr.ui.components.OrnamentMedallion
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.IcoStar
import app.rafiqaldhikr.ui.components.IcoSun
import app.rafiqaldhikr.ui.components.IcoSunset
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
private fun AdhkarCategoryIcon(key: String, color: Color, size: Dp = 28.dp) {
    when (key) {
        "morning"   -> IcoSun(size, color)      // شمس — أذكار الصباح
        "evening"   -> IcoSunset(size, color)   // غروب — أذكار المساء
        "sleep"     -> IcoMoon(size, color)     // هلال — أذكار النوم
        "istighfar" -> IcoDua(size, color)      // كفّان — الاستغفار
        "prayer"    -> IcoMosque(size, color)   // مسجد — أذكار الصلاة
        else        -> IcoStar(size, color)     // نجمة — متنوعة
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
            // ميدالية زخرفية موحّدة
            OrnamentMedallion(size = 54.dp) { s, c -> AdhkarCategoryIcon(cat.key, c, s) }

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
