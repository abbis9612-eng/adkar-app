package app.rafiqaldhikr.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.animations.breathingAnimation
import app.rafiqaldhikr.ui.animations.goldShimmer
import app.rafiqaldhikr.ui.animations.goldParticleOverlay
import app.rafiqaldhikr.ui.animations.rememberParticleTime
import app.rafiqaldhikr.ui.animations.rememberGlowAlpha
import app.rafiqaldhikr.ui.animations.rememberStaggeredVisibility
import app.rafiqaldhikr.ui.animations.pulseAnimation
import app.rafiqaldhikr.ui.animations.pressAnimation
import app.rafiqaldhikr.ui.components.*
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlin.math.*


/* ═══════════════════════════════════════════════════════
   PILL BUTTON — Header icon buttons
═══════════════════════════════════════════════════════ */

@Composable
private fun PillBtn(onClick: () -> Unit = {}, m: Modifier = Modifier, content: @Composable () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        m.size(42.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(13.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ═══════════════════════════════════════════════════════
   ARC PROGRESS — Circular progress
═══════════════════════════════════════════════════════ */

@Composable
private fun ArcProgress(
    value: Int, max: Int, sizeDp: Dp = 68.dp,
    stroke: Color = LocalRafiqColors.current.goldLight,
    bg: Color = LocalRafiqColors.current.divider,
    sw: Dp = 5.dp,
    m: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val pct = (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val anim by animateFloatAsState(pct, tween(900, easing = FastOutSlowInEasing), label = "ap")
    Box(m.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = sw.toPx(); val r = (size.minDimension - s * 2) / 2f
            val tl = Offset(size.width / 2f - r, size.height / 2f - r); val sz = Size(r * 2, r * 2)
            drawArc(bg, 0f, 360f, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
            if (anim > 0.005f) {
                drawArc(stroke, -90f, 360f * anim, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        content()
    }
}

/* ═══════════════════════════════════════════════════════
   SECTION LABEL — Premium diamond + text
═══════════════════════════════════════════════════════ */

@Composable
private fun SecLabel(text: String, index: Int = 0) {
    val rc = LocalRafiqColors.current
    val (alpha, transY) = rememberStaggeredVisibility(index = index, delayPerItem = 80, durationMs = 500)

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
            .graphicsLayer { this.alpha = alpha; translationY = transY },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Diamond ornament
        Canvas(Modifier.size(16.dp)) {
            val cx = size.width / 2f; val cy = size.height / 2f; val r = size.minDimension * 0.38f
            val diamond = Path().apply {
                moveTo(cx, cy - r * 1.4f); lineTo(cx + r, cy)
                lineTo(cx, cy + r * 1.4f); lineTo(cx - r, cy); close()
            }
            drawPath(diamond, rc.gold)
            drawPath(diamond, rc.gold, style = Stroke(1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = rc.ink,
            letterSpacing = 0.5.sp
        )
        // Trailing line
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(rc.gold.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
    }
}

/* ═══════════════════════════════════════════════════════
   COUNTDOWN SEGMENTS — Glass boxes
═══════════════════════════════════════════════════════ */

@Composable
private fun CountdownSegments(countdown: String, arabic: Boolean, colonAlpha: Float) {
    val parts = countdown.split(":")
    val h = parts.getOrNull(0) ?: "--"
    val m = parts.getOrNull(1) ?: "--"
    val s = parts.getOrNull(2) ?: "--"

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountdownBox(h, "ساعة", arabic)
        CountdownColon(colonAlpha)
        CountdownBox(m, "دقيقة", arabic)
        CountdownColon(colonAlpha)
        CountdownBox(s, "ثانية", arabic)
    }
}

@Composable
private fun CountdownBox(value: String, label: String, arabic: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .widthIn(min = 60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.localizedDigits(arabic),
                style = NumbersStyle, fontSize = 26.sp,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.65f))
    }
}

@Composable
private fun CountdownColon(alpha: Float) {
    Text(
        ":",
        style = NumbersStyle, fontSize = 24.sp,
        color = Color.White.copy(alpha = alpha),
        modifier = Modifier.padding(horizontal = 6.dp).offset(y = (-10).dp)
    )
}

/* ═══════════════════════════════════════════════════════
   HERO SECTION — Header + Basmalah + Greeting + Prayer Card
   (All fused into one immersive sky canvas)
═══════════════════════════════════════════════════════ */

@Composable
private fun HeroSection(
    hijri: String,
    greeting: String,
    nextPrayerName: String,
    nextPrayerTime: String,
    countdown: String,
    prevMillis: Long,
    nextMillis: Long,
    onSettings: () -> Unit,
    onBell: () -> Unit,
    onPrayerClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val arabic = LocalArabicNumerals.current

    // Colon pulse for countdown
    val tr = rememberInfiniteTransition(label = "dot")
    val da by tr.animateFloat(
        1f, 0.4f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "da"
    )

    // Progress between prayers
    val progress = if (nextMillis > prevMillis && prevMillis > 0) {
        ((System.currentTimeMillis() - prevMillis).toFloat() /
                (nextMillis - prevMillis).toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Gold glow pulse
    val glowAlpha = rememberGlowAlpha(0.18f, 0.5f, 2800)

    // Particles
    val particleTime = rememberParticleTime()

    // Staggered entrance
    val (heroAlpha, heroTransY) = rememberStaggeredVisibility(0, 0, 700)

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = heroAlpha; translationY = heroTransY }
    ) {
        // Sky scene as the hero background
        PrayerScene(
            prayerName = nextPrayerName,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .goldParticleOverlay(particleTime, count = 16)
            ) {
                Column(Modifier.fillMaxWidth()) {

                    // ── Header row ──
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // RTL: first child = RIGHT → Bell
                        Box {
                            PillBtn(onClick = onBell) { IcoBell(c = Color.White) }
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .align(Alignment.TopStart)
                                    .offset(x = 8.dp, y = 10.dp)
                                    .clip(CircleShape)
                                    .background(rc.error)
                            )
                        }
                        // Center
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "رَفِيقُ الذِّكر",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                hijri.ifEmpty { "— هـ" }.localizedDigits(arabic),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 3.dp)
                            )
                        }
                        // Last child = LEFT → Settings
                        PillBtn(onClick = onSettings) { IcoGear(c = Color.White) }
                    }

                    // ── Basmalah ──
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GeomStar(
                            54.dp, rc.goldLight, 0.55f,
                            Modifier.breathingAnimation(minScale = 0.95f, maxScale = 1.08f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                Modifier.width(40.dp).height(1.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.5f))))
                            )
                            Text(
                                "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                                fontSize = 18.sp,
                                fontFamily = app.rafiqaldhikr.ui.theme.AmiriFamily,
                                color = rc.goldLight,
                            )
                            Box(
                                Modifier.width(40.dp).height(1.dp)
                                    .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)))
                            )
                        }

                        // Greeting line
                        Spacer(Modifier.height(4.dp))
                        Text(
                            greeting.ifEmpty { "صَبَاحُ الخَيْر" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    // ── Divider ornament ──
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Spacer(Modifier.height(14.dp))

                    // ── Prayer name + time row ──
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                "الصلاة القادمة",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                nextPrayerName.ifEmpty { "—" },
                                fontSize = 46.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 50.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "وقت الصلاة",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                nextPrayerTime.ifEmpty { "—" }.localizedDigits(arabic),
                                style = NumbersStyle,
                                fontSize = 36.sp,
                                color = rc.goldLight,
                                lineHeight = 40.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── Countdown glass segments ──
                    CountdownSegments(countdown, arabic, da)

                    Spacer(Modifier.height(16.dp))

                    // ── Progress bar ──
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(rc.goldLight, rc.gold))
                                )
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── "عرض كل المواقيت" pill at bottom ──
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(20.dp))
                            .clickable(onClick = onPrayerClick)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "عرض مواقيت الصلاة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   NOW CARD — رفيق اليوم: Enhanced CTA with glowing border
═══════════════════════════════════════════════════════ */

@Composable
private fun NowCard(
    navController: NavHostController,
    vm: app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val station = state.nowStation ?: return

    // Pulsing gold glow
    val glowAlpha = rememberGlowAlpha(0.20f, 0.60f, 2000)

    // Staggered entrance
    val (alpha, transY) = rememberStaggeredVisibility(index = 1, delayPerItem = 100, durationMs = 550)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer { this.alpha = alpha; translationY = transY }
    ) {
        // Outer glow ring
        Box(
            Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(22.dp))
                .background(rc.gold.copy(alpha = glowAlpha * 0.12f))
        )

        Box(
            Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            rc.emeraldPastel,
                            rc.card,
                            rc.accentGoldBg.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            rc.gold.copy(alpha = glowAlpha),
                            rc.goldLight.copy(alpha = glowAlpha * 0.5f),
                            rc.gold.copy(alpha = glowAlpha)
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .clickable { navController.navigate(RafiqRoute.DayCompanion.route) }
                .padding(16.dp)
        ) {
            // Decorative GeomStar background
            GeomStar(
                80.dp, rc.gold, 0.06f,
                Modifier.align(Alignment.TopEnd).offset(x = 10.dp, y = (-10).dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Station icon with premium circle
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(rc.emerald.copy(alpha = 0.15f), rc.gold.copy(alpha = 0.08f))
                            )
                        )
                        .border(1.2.dp, rc.gold.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) { StationIcon(station.id, 30.dp) }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        "رفيق اليوم",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = rc.gold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        station.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = rc.ink
                    )
                    Spacer(Modifier.height(4.dp))

                    // Station progress dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val total = state.stations.size.coerceAtLeast(1)
                        val done = state.doneCount
                        repeat(minOf(total, 8)) { i ->
                            Box(
                                Modifier
                                    .size(if (i < done) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < done) rc.emerald
                                        else rc.divider.copy(alpha = 0.8f)
                                    )
                            )
                        }
                        if (total > 8) {
                            Text(
                                "+${total - 8}".localizedDigits(LocalArabicNumerals.current),
                                fontSize = 9.sp,
                                color = rc.inkMed
                            )
                        }
                    }

                    Spacer(Modifier.height(3.dp))
                    Text(
                        station.timeLabel.localizedDigits(LocalArabicNumerals.current),
                        fontSize = 11.sp,
                        color = rc.inkMed
                    )
                }

                // Start CTA button
                Box(
                    Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(rc.emerald, rc.emeraldMed)
                            )
                        )
                        .pressAnimation(
                            onClick = { navController.navigate(station.route ?: RafiqRoute.DayCompanion.route) }
                        )
                        .padding(horizontal = 18.dp, vertical = 11.dp)
                ) {
                    Text(
                        "ابدأ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   WIRD CARD — Premium gradient + enhanced arc
═══════════════════════════════════════════════════════ */

@Composable
private fun WirdCard(current: Int = 0, total: Int = 1000, percent: Int = 0) {
    val rc = LocalRafiqColors.current
    val (alpha, transY) = rememberStaggeredVisibility(index = 2, delayPerItem = 100, durationMs = 550)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer { this.alpha = alpha; translationY = transY }
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        rc.card,
                        rc.accentGoldBg.copy(alpha = 0.5f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(1.dp, rc.gold.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
    ) {
        // Decorative background ornament
        GeomStar(110.dp, rc.emerald, 0.04f, Modifier.align(Alignment.TopStart).offset((-18).dp, (-18).dp))
        GeomStar(70.dp, rc.gold, 0.05f, Modifier.align(Alignment.BottomEnd).offset(12.dp, 12.dp))

        Column(Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    // Label chip
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(rc.accentGoldBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "الورد اليومي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = rc.gold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "الحمد لله",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = rc.emerald
                    )
                    Text(
                        "على كل حال وفي كل أوان",
                        fontSize = 12.sp,
                        color = rc.inkMed,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Premium arc progress
                ArcProgress(
                    current, total, 80.dp,
                    stroke = rc.gold, bg = rc.emeraldPastel2, sw = 7.dp
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$percent٪".localizedDigits(LocalArabicNumerals.current),
                            style = NumbersStyle, fontSize = 18.sp, color = rc.emerald,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("إنجاز", fontSize = 10.sp, color = rc.inkMed)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rc.emeraldPastel2)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(listOf(rc.emerald, rc.gold, rc.goldLight))
                        )
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$current / $total".localizedDigits(LocalArabicNumerals.current),
                    style = NumbersStyle, fontSize = 12.sp, color = rc.inkMed
                )
                Text("الهدف اليومي", fontSize = 11.sp, color = rc.inkLight)
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   ADHKAR GRID — Distinct color identity per category
═══════════════════════════════════════════════════════ */

@Composable
private fun AdhkarGrid(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val (alpha, transY) = rememberStaggeredVisibility(index = 3, delayPerItem = 100, durationMs = 550)

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .graphicsLayer { this.alpha = alpha; translationY = transY },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem(
                label = "أذكار الصباح",
                icon = { IcoSun(28.dp, rc.gold) },
                gradientColors = listOf(rc.accentGoldBg, rc.morningRing.copy(alpha = 0.08f)),
                borderColor = rc.morningRing.copy(alpha = 0.4f),
                labelColor = rc.accentBrown,
                iconBg = rc.morningRing.copy(alpha = 0.15f),
                modifier = Modifier.weight(1f)
            ) { nav.navigate(RafiqRoute.DhikrReading.withCategory("morning")) }

            AdhkarItem(
                label = "أذكار المساء",
                icon = { IcoMoon(28.dp, rc.purple) },
                gradientColors = listOf(rc.accentPurpleBg, rc.eveningRing.copy(alpha = 0.08f)),
                borderColor = rc.eveningRing.copy(alpha = 0.40f),
                labelColor = rc.accentPurple,
                iconBg = rc.eveningRing.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f)
            ) { nav.navigate(RafiqRoute.DhikrReading.withCategory("evening")) }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem(
                label = "أذكار النوم",
                icon = { IcoStar(28.dp, rc.purpleSleep) },
                gradientColors = listOf(rc.accentPurpleBg, rc.sleepRing.copy(alpha = 0.06f)),
                borderColor = rc.sleepRing.copy(alpha = 0.35f),
                labelColor = rc.purpleSleep,
                iconBg = rc.sleepRing.copy(alpha = 0.18f),
                modifier = Modifier.weight(1f)
            ) { nav.navigate(RafiqRoute.DhikrReading.withCategory("sleep")) }

            AdhkarItem(
                label = "الاستغفار",
                icon = { IcoDua(28.dp, rc.emerald) },
                gradientColors = listOf(rc.emeraldPastel, rc.istighfarRing.copy(alpha = 0.06f)),
                borderColor = rc.istighfarRing.copy(alpha = 0.4f),
                labelColor = rc.emerald,
                iconBg = rc.emeraldPastel2.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            ) { nav.navigate(RafiqRoute.Tasbeeh.route) }
        }
    }
}

@Composable
private fun AdhkarItem(
    label: String,
    icon: @Composable () -> Unit,
    gradientColors: List<Color>,
    borderColor: Color,
    labelColor: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier
            .shadow(3.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.2.dp, borderColor, RoundedCornerShape(22.dp))
            .pressAnimation(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 22.dp)
    ) {
        // Subtle background star
        GeomStar(
            55.dp, labelColor, 0.06f,
            Modifier.align(Alignment.BottomEnd).offset(8.dp, 8.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(iconBg)
                    .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.height(12.dp))
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = labelColor,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════
   PRAYER TIMES LIST — Cinematic active row highlight
═══════════════════════════════════════════════════════ */

@Composable
private fun PrayerTimesList(prayers: List<HomeViewModel.PrayerUi>) {
    val rc = LocalRafiqColors.current
    val (alpha, transY) = rememberStaggeredVisibility(index = 4, delayPerItem = 100, durationMs = 550)
    val glowAlpha = rememberGlowAlpha(0.10f, 0.28f, 2500)

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer { this.alpha = alpha; translationY = transY }
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(rc.card)
            .border(1.dp, rc.divider, RoundedCornerShape(22.dp))
    ) {
        prayers.forEachIndexed { i, p ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (p.active) Modifier.background(
                            Brush.horizontalGradient(
                                listOf(
                                    rc.emerald.copy(alpha = glowAlpha * 0.4f),
                                    rc.emeraldPastel,
                                    rc.emerald.copy(alpha = glowAlpha * 0.2f)
                                )
                            )
                        ) else Modifier
                    )
            ) {
                // Active left accent bar
                if (p.active) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, rc.emerald, Color.Transparent)
                                )
                            )
                    )
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // RTL first(right): circle + name + badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Status indicator
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        p.done   -> rc.accentGoldBg
                                        p.active -> rc.emeraldPastel
                                        else     -> rc.divider.copy(alpha = 0.3f)
                                    }
                                )
                                .then(
                                    if (p.active) Modifier.border(1.dp, rc.emerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    else if (p.done) Modifier.border(1.dp, rc.gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (p.done) IcoCheck(14.dp, rc.gold)
                            else Box(
                                Modifier.size(8.dp).clip(CircleShape).background(
                                    if (p.active) rc.emerald else rc.inkMed.copy(alpha = 0.4f)
                                ).then(if (p.active) Modifier.pulseAnimation(0.9f, 1.1f, 1200) else Modifier)
                            )
                        }

                        // Prayer name
                        Text(
                            p.ar,
                            fontSize = 15.sp,
                            fontWeight = if (p.active) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (p.active) rc.emerald else rc.ink
                        )

                        // "الآن" badge
                        if (p.active) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(rc.emerald)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "الآن",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // RTL last(left): time
                    Text(
                        p.time.localizedDigits(LocalArabicNumerals.current),
                        style = NumbersStyle,
                        fontSize = 14.sp,
                        color = if (p.active) rc.emerald else rc.inkMed,
                        fontWeight = if (p.active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (i < prayers.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .height(1.dp)
                        .background(rc.divider.copy(alpha = 0.6f))
                )
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   HOME SCREEN — Assembly
═══════════════════════════════════════════════════════ */

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LocationPermissionEffect(
        hasLocation = state.hasLocation,
        isLoading = state.isLoading,
        onLocationFetched = { lat, lng -> viewModel.saveLocation(lat, lng) }
    )

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── 1. Hero: Header + Basmalah + Greeting + NextPrayer (fused sky) ──
            HeroSection(
                hijri          = state.hijriDate,
                greeting       = state.greeting,
                nextPrayerName = state.nextPrayerName,
                nextPrayerTime = state.nextPrayerTime,
                countdown      = state.countdown,
                prevMillis     = state.prevPrayerMillis,
                nextMillis     = state.nextPrayerMillis,
                onSettings     = { navController.navigate(RafiqRoute.Settings.route) },
                onBell         = { navController.navigate(RafiqRoute.NotificationSettings.route) },
                onPrayerClick  = { navController.navigate(RafiqRoute.PrayerTimes.route) }
            )

            // ── 2. رفيق اليوم ──
            SecLabel("رفيق اليوم", index = 0)
            NowCard(navController)

            // ── 3. الورد اليومي ──
            SecLabel("وِرْدُكَ اليومي", index = 1)
            WirdCard(state.wirdCurrent, state.wirdTotal, state.wirdPercent)

            // ── 4. فئات الأذكار ──
            SecLabel("فئات الأذكار", index = 2)
            AdhkarGrid(navController)

            // ── 5. مواقيت الصلاة ──
            SecLabel("مواقيت الصلاة", index = 3)
            PrayerTimesList(state.prayers)

            Spacer(Modifier.height(32.dp))
        }
    }
}
