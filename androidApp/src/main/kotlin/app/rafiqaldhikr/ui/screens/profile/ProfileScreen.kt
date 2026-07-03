package app.rafiqaldhikr.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*

/* Colors provided by LocalRafiqColors */

/* البرتقالي يأتي من RafiqPalette (accentOrange) */

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

@Composable
private fun IconSettings(size: Dp = 17.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val hex = Path().apply {
            for (i in 0 until 6) {
                val a = (i * 60 - 30) * PI.toFloat() / 180f; val r = w * 0.42f
                if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                else lineTo(cx + r * cos(a), cy + r * sin(a))
            }; close()
        }
        drawPath(hex, color, style = st)
        drawCircle(color, w * 0.13f, Offset(cx, cy), style = Stroke(w * 0.065f))
    }
}

@Composable
private fun IconMosque(size: Dp = 22.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val st = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Dome
        drawArc(color, 180f, 180f, false,
            Offset(w * 0.20f, h * 0.22f), Size(w * 0.60f, h * 0.50f), style = st)
        // Body
        drawRect(color.copy(alpha = 0.08f), Offset(w * 0.15f, h * 0.47f), Size(w * 0.70f, h * 0.40f))
        drawRect(color, Offset(w * 0.15f, h * 0.47f), Size(w * 0.70f, h * 0.40f), style = st)
        // Crescent
        drawCircle(color, w * 0.05f, Offset(w * 0.50f, h * 0.18f))
        drawLine(color, Offset(w * 0.50f, h * 0.23f), Offset(w * 0.50f, h * 0.13f), w * 0.04f, StrokeCap.Round)
        // Base
        drawLine(color, Offset(w * 0.08f, h * 0.87f), Offset(w * 0.92f, h * 0.87f), w * 0.06f, StrokeCap.Round)
    }
}

@Composable
private fun IconTrophy(size: Dp = 22.dp, color: Color = LocalRafiqColors.current.goldLight) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val st = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Cup body
        val cup = Path().apply {
            moveTo(w * 0.22f, h * 0.15f)
            lineTo(w * 0.78f, h * 0.15f)
            lineTo(w * 0.70f, h * 0.55f)
            cubicTo(w * 0.65f, h * 0.65f, w * 0.35f, h * 0.65f, w * 0.30f, h * 0.55f)
            close()
        }
        drawPath(cup, color.copy(alpha = 0.12f))
        drawPath(cup, color, style = st)
        // Handles
        drawArc(color, 270f, 180f, false, Offset(w * 0.08f, h * 0.22f), Size(w * 0.18f, h * 0.22f), style = st)
        drawArc(color, 90f, 180f, false, Offset(w * 0.74f, h * 0.22f), Size(w * 0.18f, h * 0.22f), style = st)
        // Stem + base
        drawLine(color, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.50f, h * 0.78f), w * 0.06f, StrokeCap.Round)
        drawLine(color, Offset(w * 0.32f, h * 0.82f), Offset(w * 0.68f, h * 0.82f), w * 0.06f, StrokeCap.Round)
    }
}

@Composable
private fun IconFlame(size: Dp = 22.dp, color: Color = LocalRafiqColors.current.accentOrange) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        val flame = Path().apply {
            moveTo(w * 0.50f, h * 0.08f)
            cubicTo(w * 0.30f, h * 0.30f, w * 0.15f, h * 0.55f, w * 0.22f, h * 0.75f)
            cubicTo(w * 0.28f, h * 0.90f, w * 0.72f, h * 0.90f, w * 0.78f, h * 0.75f)
            cubicTo(w * 0.85f, h * 0.55f, w * 0.70f, h * 0.30f, w * 0.50f, h * 0.08f)
            close()
        }
        drawPath(flame, color.copy(alpha = 0.15f))
        drawPath(flame, color, style = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Inner flame
        val inner = Path().apply {
            moveTo(w * 0.50f, h * 0.40f)
            cubicTo(w * 0.40f, h * 0.55f, w * 0.38f, h * 0.70f, w * 0.42f, h * 0.78f)
            cubicTo(w * 0.46f, h * 0.82f, w * 0.54f, h * 0.82f, w * 0.58f, h * 0.78f)
            cubicTo(w * 0.62f, h * 0.70f, w * 0.60f, h * 0.55f, w * 0.50f, h * 0.40f)
            close()
        }
        drawPath(inner, color.copy(alpha = 0.3f))
    }
}

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

@Composable
private fun IconCheck(size: Dp = 13.dp, color: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width; val h = this.size.height
        drawPath(Path().apply {
            moveTo(w * 0.17f, h * 0.54f)
            lineTo(w * 0.38f, h * 0.75f)
            lineTo(w * 0.83f, h * 0.25f)
        }, color, style = Stroke(w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
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
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   PILL BUTTON
══════════════════════════════════════════════════════════════ */

@Composable
private fun PillBtn(onClick: () -> Unit, content: @Composable () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier.size(40.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ══════════════════════════════════════════════════════════════
   PROFILE HERO CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun ProfileHeroCard() {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp),
                ambientColor = LocalRafiqColors.current.emerald.copy(alpha = 0.22f))
            .clip(RoundedCornerShape(24.dp))
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(
                    listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
        )
        GeomDecoration(
            sizeDp = 200.dp, color = LocalRafiqColors.current.goldLight.copy(alpha = 0.14f), spinDuration = 80_000,
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-50).dp, y = (-50).dp)
                .graphicsLayer { alpha = 0.3f },
        )

        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(2.dp, rc.goldLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(36.dp)) {
                    val w = size.width; val cx = w / 2f; val cy = w / 2f
                    drawCircle(Color.White, w * 0.22f, Offset(cx, cy - w * 0.10f))
                    drawArc(Color.White, 0f, 180f, true,
                        Offset(cx - w * 0.32f, cy + w * 0.05f), Size(w * 0.64f, w * 0.50f))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "المستخدم",
                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "بارك الله فيك على مثابرتك",
                fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   STAT CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    iconBg: Color,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    Column(
        modifier
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.height(8.dp))
        Text(value.localizedDigits(LocalArabicNumerals.current),
            style = NumbersStyle, fontSize = 22.sp, color = LocalRafiqColors.current.emerald)
        Text(label, fontSize = 11.sp, color = LocalRafiqColors.current.inkMed)
    }
}

/* ══════════════════════════════════════════════════════════════
   SECTION HEADER
══════════════════════════════════════════════════════════════ */

@Composable
private fun SectionHeader(title: String) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(rc.gold))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark)
    }
}

/* ══════════════════════════════════════════════════════════════
   TODAY ACHIEVEMENT ROW
══════════════════════════════════════════════════════════════ */

@Composable
private fun TodayRow(label: String, value: String, isAchieved: Boolean, isLast: Boolean = false) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp, color = LocalRafiqColors.current.inkDark)
        if (isAchieved && value == "✓") {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(rc.emeraldPastel),
                contentAlignment = Alignment.Center,
            ) { IconCheck(11.dp, rc.emerald) }
        } else {
            Text(
                value,
                fontSize = 15.sp, fontWeight = FontWeight.Bold,
                color = if (isAchieved) rc.emerald else rc.inkLight,
            )
        }
    }
    if (!isLast) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(rc.gold.copy(alpha = 0.07f)))
    }
}

/* ══════════════════════════════════════════════════════════════
   WEEK DAY CIRCLES
══════════════════════════════════════════════════════════════ */

@Composable
private fun WeekCircles(weekProgress: List<app.rafiq.domain.model.DailyProgressInfo>, todayIdx: Int) {
    val rc = LocalRafiqColors.current
    val days = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")

    Row(
        Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        weekProgress.takeLast(7).forEachIndexed { idx, day ->
            val score = ((if (day.morningDone) 1 else 0) +
                    (if (day.eveningDone) 1 else 0) +
                    day.prayersLogged.toInt().coerceAtMost(5))
            val filled = score >= 5
            val isToday = idx == weekProgress.size - 1

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                filled -> rc.emerald
                                score >= 2 -> rc.emeraldPastel
                                else -> rc.bg
                            }
                        )
                        .then(
                            if (isToday) Modifier.border(2.dp, rc.goldLight, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (filled) {
                        IconCheck(12.dp, Color.White)
                    } else {
                        Text(
                            "$score",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = if (score >= 2) rc.emerald else rc.inkLight,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (idx < days.size) {
                    Text(
                        days[idx],
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) rc.gold else rc.inkMed,
                    )
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   QUICK LINK CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun QuickLinkCard(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(rc.emeraldPastel),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.width(12.dp))
            Text(
                label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = LocalRafiqColors.current.ink, modifier = Modifier.weight(1f),
            )
            IconArrow(14.dp, rc.inkLight)
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN PROFILE SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // ═══ TOP BAR ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillBtn(onClick = { navController.navigate(RafiqRoute.Settings.route) }) {
                    IconSettings()
                }
                Text("حسابي", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            }

            // ═══ PROFILE HERO ═══
            ProfileHeroCard()

            Spacer(Modifier.height(16.dp))

            // ═══ STATS ROW (3 cards) ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    icon = { IconMosque(20.dp, rc.emerald) },
                    value = "${state.todayProgress?.prayersLogged ?: 0}/5",
                    label = "الصلوات",
                    iconBg = LocalRafiqColors.current.emeraldPastel,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = { IconTrophy(20.dp, rc.goldLight) },
                    value = "${state.streak.longest}",
                    label = "أطول سلسلة",
                    iconBg = LocalRafiqColors.current.accentGoldBg,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = { IconFlame(20.dp, LocalRafiqColors.current.accentOrange) },
                    value = "${state.streak.current}",
                    label = "سلسلة حالية",
                    iconBg = LocalRafiqColors.current.accentOrangeBg,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══ TODAY'S ACHIEVEMENTS ═══
            SectionHeader("إنجازات اليوم")
            Spacer(Modifier.height(10.dp))

            val p = state.todayProgress
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .shadow(3.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(rc.card)
                    .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            ) {
                TodayRow("أذكار الصباح", if (p?.morningDone == true) "✓" else "—", p?.morningDone == true)
                TodayRow("أذكار المساء", if (p?.eveningDone == true) "✓" else "—", p?.eveningDone == true)
                TodayRow("صفحات القرآن", "${p?.quranPages ?: 0}", (p?.quranPages ?: 0) > 0)
                TodayRow("التسبيح", "${p?.tasbeehCount ?: 0}", (p?.tasbeehCount ?: 0) > 0)
                TodayRow("الصلوات", "${p?.prayersLogged ?: 0} / ٥", (p?.prayersLogged ?: 0) > 0, isLast = true)
            }

            Spacer(Modifier.height(20.dp))

            // ═══ THIS WEEK ═══
            SectionHeader("هذا الأسبوع")
            Spacer(Modifier.height(10.dp))

            Box(Modifier.padding(horizontal = 14.dp)) {
                WeekCircles(
                    weekProgress = state.weekProgress,
                    todayIdx = state.weekProgress.size - 1,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══ QUICK LINKS ═══
            SectionHeader("روابط سريعة")
            Spacer(Modifier.height(10.dp))

            Column(
                Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickLinkCard(
                    icon = {
                        Canvas(Modifier.size(18.dp)) {
                            val w = size.width
                            // Bar chart
                            drawRect(rc.emerald, Offset(w * 0.10f, w * 0.50f), Size(w * 0.20f, w * 0.40f))
                            drawRect(rc.emerald, Offset(w * 0.40f, w * 0.25f), Size(w * 0.20f, w * 0.65f))
                            drawRect(rc.emerald, Offset(w * 0.70f, w * 0.10f), Size(w * 0.20f, w * 0.80f))
                        }
                    },
                    label = "الإحصائيات التفصيلية",
                ) { navController.navigate(RafiqRoute.Statistics.route) }

                QuickLinkCard(
                    icon = {
                        Canvas(Modifier.size(18.dp)) {
                            val w = size.width
                            drawRect(rc.emerald.copy(alpha = 0.15f), Offset(w * 0.05f, w * 0.15f), Size(w * 0.90f, w * 0.70f),
                                style = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            drawLine(rc.emerald, Offset(w * 0.20f, w * 0.65f), Offset(w * 0.45f, w * 0.35f), w * 0.07f, StrokeCap.Round)
                            drawLine(rc.emerald, Offset(w * 0.45f, w * 0.35f), Offset(w * 0.60f, w * 0.55f), w * 0.07f, StrokeCap.Round)
                            drawLine(rc.emerald, Offset(w * 0.60f, w * 0.55f), Offset(w * 0.80f, w * 0.30f), w * 0.07f, StrokeCap.Round)
                        }
                    },
                    label = "التقرير الأسبوعي",
                ) { navController.navigate(RafiqRoute.WeeklyReport.route) }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickLinkCard(
                        icon = {
                            Canvas(Modifier.size(18.dp)) {
                                val w = size.width; val cx = w / 2f; val cy = w / 2f
                                drawCircle(rc.emerald, w * 0.12f, Offset(cx, cy - w * 0.18f))
                                drawCircle(rc.emerald, w * 0.08f, Offset(cx - w * 0.22f, cy + w * 0.08f))
                                drawCircle(rc.emerald, w * 0.10f, Offset(cx + w * 0.20f, cy + w * 0.12f))
                                drawLine(rc.emerald, Offset(cx, cy + w * 0.10f), Offset(cx, cy + w * 0.40f), w * 0.06f, StrokeCap.Round)
                            }
                        },
                        label = "الحديقة",
                        modifier = Modifier.weight(1f),
                    ) { navController.navigate(RafiqRoute.Garden.route) }

                    QuickLinkCard(
                        icon = {
                            Canvas(Modifier.size(18.dp)) {
                                val w = size.width; val cx = w / 2f; val cy = w / 2f
                                val star = Path().apply {
                                    for (i in 0 until 10) {
                                        val a = (i * 36 - 90) * PI.toFloat() / 180f
                                        val r = if (i % 2 == 0) w * 0.42f else w * 0.22f
                                        if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                                        else lineTo(cx + r * cos(a), cy + r * sin(a))
                                    }; close()
                                }
                                drawPath(star, rc.goldLight.copy(alpha = 0.15f))
                                drawPath(star, rc.goldLight, style = Stroke(w * 0.06f, join = StrokeJoin.Round))
                            }
                        },
                        label = "الإنجازات",
                        modifier = Modifier.weight(1f),
                    ) { navController.navigate(RafiqRoute.Achievements.route) }
                }

                QuickLinkCard(
                    icon = {
                        Canvas(Modifier.size(18.dp)) {
                            val w = size.width
                            drawLine(rc.emerald, Offset(w * 0.20f, w * 0.30f), Offset(w * 0.80f, w * 0.30f), w * 0.06f, StrokeCap.Round)
                            drawLine(rc.emerald, Offset(w * 0.60f, w * 0.15f), Offset(w * 0.80f, w * 0.30f), w * 0.06f, StrokeCap.Round)
                            drawLine(rc.emerald, Offset(w * 0.60f, w * 0.45f), Offset(w * 0.80f, w * 0.30f), w * 0.06f, StrokeCap.Round)
                            drawLine(rc.emerald, Offset(w * 0.20f, w * 0.70f), Offset(w * 0.80f, w * 0.70f), w * 0.06f, StrokeCap.Round)
                        }
                    },
                    label = "مشاركة إنجازي",
                ) { navController.navigate(RafiqRoute.ShareCard.route) }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
