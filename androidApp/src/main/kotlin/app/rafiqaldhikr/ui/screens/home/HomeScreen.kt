package app.rafiqaldhikr.ui.screens.home

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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.animations.pressAnimation
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.theme.AmiriFamily
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.components.*
import kotlin.math.*


/* Colors provided by LocalRafiqColors */

/* ═══════════════════════════════════════════════════════
   ARC PROGRESS — Circular progress (Wird card)
═══════════════════════════════════════════════════════ */

@Composable
private fun ArcProgress(
    value: Int, max: Int, sizeDp: Dp = 68.dp,
    stroke: Color = LocalRafiqColors.current.goldLight, bg: Color = LocalRafiqColors.current.divider, sw: Dp = 6.dp,
    m: Modifier = Modifier, content: @Composable () -> Unit = {},
) {
    val pct = (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val anim by animateFloatAsState(pct, tween(900, easing = FastOutSlowInEasing), label = "ap")
    Box(m.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = sw.toPx(); val r = (size.minDimension - s * 2) / 2f
            val tl = Offset(size.width / 2f - r, size.height / 2f - r); val sz = Size(r * 2, r * 2)
            drawArc(bg, 0f, 360f, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
            drawArc(stroke, -90f, 360f * anim, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

/* ═══════════════════════════════════════════════════════
   PILL BUTTON — Header icon buttons
═══════════════════════════════════════════════════════ */

@Composable
private fun PillBtn(onClick: () -> Unit = {}, m: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(m.size(44.dp).clip(RoundedCornerShape(14.dp))
        .background(LocalRafiqColors.current.card).border(1.dp, LocalRafiqColors.current.divider, RoundedCornerShape(14.dp))
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}

/* Icons moved to IslamicIcons.kt */

/* ═══════════════════════════════════════════════════════
   SECTION LABEL — Gold bar + text
═══════════════════════════════════════════════════════ */

@Composable
private fun SecLabel(text: String) {
    val rc = LocalRafiqColors.current
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(4.dp))
            .background(Brush.verticalGradient(listOf(rc.goldLight, rc.gold))))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rc.ink)
    }
}

/* ═══════════════════════════════════════════════════════
   1. HEADER — Bell(right) + Title + Settings(left)
═══════════════════════════════════════════════════════ */

@Composable
private fun Header(hijri: String, onSettings: () -> Unit = {}, onBell: () -> Unit = {}) {
    val rc = LocalRafiqColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        // In RTL: first child = RIGHT side → Bell
        Box {
            PillBtn(onClick = onBell) { IcoBell(c = rc.emerald) }
            Box(Modifier.size(7.dp).align(Alignment.TopStart).offset(x = 8.dp, y = 10.dp)
                .clip(CircleShape).background(rc.error))
        }
        // Center: Title + date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("رَفِيقُ الذِّكر", fontSize = 23.sp, fontWeight = FontWeight.Bold,
                color = rc.emerald, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(hijri.ifEmpty { "— هـ" }, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = rc.gold,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(rc.gold.copy(alpha = 0.10f))
                    .padding(horizontal = 12.dp, vertical = 2.dp))
        }
        // In RTL: last child = LEFT side → Settings
        PillBtn(onClick = onSettings) { IcoGear(c = rc.emerald) }
    }
}

/* ═══════════════════════════════════════════════════════
   2. GEOM STAR + BASMALAH — Amiri calligraphy
═══════════════════════════════════════════════════════ */

@Composable
private fun BasmalahSection() {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GeomStar(56.dp, rc.gold, 0.28f)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.width(36.dp).height(1.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, rc.goldLight))))
            Text("بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ", fontSize = 18.sp,
                fontFamily = AmiriFamily, color = rc.inkDark)
            Box(Modifier.width(36.dp).height(1.dp).background(
                Brush.horizontalGradient(listOf(rc.goldLight, Color.Transparent))))
        }
    }
}

/* ═══════════════════════════════════════════════════════
   3. AYAH CARD — Greeting + verse of remembrance
═══════════════════════════════════════════════════════ */

@Composable
private fun GreetingCard(greeting: String) {
    val rc = LocalRafiqColors.current
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        .clip(RoundedCornerShape(20.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))) {
        // Background decoration
        GeomStar(72.dp, rc.gold, 0.06f, Modifier.align(Alignment.BottomStart).offset((-12).dp, 12.dp))
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            // RTL: first child(right) = greeting, last child(left) = badge
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("${greeting.ifEmpty { "صَبَاحُ الخَيْر" }} 🌤", fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, color = rc.inkMed)
                Row(Modifier.clip(RoundedCornerShape(16.dp)).background(rc.emeraldPastel)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🔔", fontSize = 10.sp)
                    Text("وقت الذِكر", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
                }
            }
            Text("وَاذْكُر رَّبَّكَ كَثِيرًا", fontSize = 27.sp, fontFamily = AmiriFamily,
                fontWeight = FontWeight.Bold, color = rc.gold, textAlign = TextAlign.Center,
                lineHeight = 42.sp, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            Text("سورة آل عمران – ٤١", fontSize = 11.sp, color = rc.inkMed,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 2.dp))
        }
    }
}

/* ═══════════════════════════════════════════════════════
   4. NEXT PRAYER CARD — Emerald hero gradient
═══════════════════════════════════════════════════════ */

@Composable
private fun NextPrayerCard(
    name: String, time: String, countdown: String,
    onClick: () -> Unit = {},
) {
    val rc = LocalRafiqColors.current
    val tr = rememberInfiniteTransition(label = "dot")
    val da by tr.animateFloat(1f, 0.45f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "da")

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .pressAnimation(onClick = onClick)
        .clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(
            listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
            start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))) {
        // Decorations
        GeomStar(120.dp, Color.White, 0.06f,
            Modifier.align(Alignment.TopStart).offset((-24).dp, (-24).dp))
        GeomStar(80.dp, rc.goldLight, 0.10f,
            Modifier.align(Alignment.BottomEnd).offset(16.dp, 20.dp))

        Column(Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
            // Content: RTL → first(right)=name, last(left)=time
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                // RIGHT side in RTL: prayer name + countdown
                Column {
                    Text(name.ifEmpty { "—" }, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, lineHeight = 44.sp)
                    Text("باقي على الأذان", fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(top = 2.dp))
                    Row(Modifier.padding(top = 10.dp).clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.22f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4ADE80))
                            .graphicsLayer { alpha = da })
                        Text(countdown.ifEmpty { "—" }, fontSize = 15.sp, color = Color.White,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                // LEFT side in RTL: time
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(time.ifEmpty { "—" }, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                        color = rc.goldLight, lineHeight = 40.sp)
                    Text("وقت الصلاة", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        // Thin gold hairline at the bottom edge
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(2.dp)
            .background(Brush.horizontalGradient(listOf(
                Color.Transparent, rc.goldLight.copy(alpha = 0.8f), Color.Transparent))))
    }
}

/* ═══════════════════════════════════════════════════════
   5. WIRD PROGRESS — Daily target card
═══════════════════════════════════════════════════════ */

@Composable
private fun WirdCard(current: Int = 0, total: Int = 1000, percent: Int = 0) {
    val rc = LocalRafiqColors.current
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(20.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))) {
        GeomStar(100.dp, rc.emerald, 0.04f, Modifier.align(Alignment.TopStart).offset((-15).dp, (-15).dp))
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            // RTL: first(right)=text, last(left)=circle
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("الورد اليومي", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = rc.inkMed)
                    Spacer(Modifier.height(2.dp))
                    Text("الحمد لله", fontSize = 26.sp, fontFamily = AmiriFamily,
                        fontWeight = FontWeight.Bold, color = rc.emerald, lineHeight = 36.sp)
                    Text("على كل حال وفي كل أوان", fontSize = 12.sp, color = rc.inkMed)
                }
                // Circle progress
                ArcProgress(current, total, 72.dp, rc.gold, rc.divider, 6.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("إنجاز", fontSize = 10.sp, color = rc.inkMed)
                        Text("${toAr(percent)}٪", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = rc.gold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            // Progress bar
            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(10.dp)).background(rc.divider)) {
                Box(Modifier.fillMaxWidth((percent / 100f).coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(rc.gold, rc.goldLight))))
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("التقدّم", fontSize = 11.sp, color = rc.inkMed)
                Text("${toAr(current)} / ${toAr(total)} ذِكر", fontSize = 11.sp,
                    fontWeight = FontWeight.Medium, color = rc.inkDark)
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   6. ADHKAR GRID — 2×2 category tiles
═══════════════════════════════════════════════════════ */

@Composable
private fun AdhkarGrid(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    Column(Modifier.padding(horizontal = 16.dp)) {
        // Top row: Morning + Evening
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem("أذكار الصباح", "بداية يومك بالنور", { IcoSun(26.dp, rc.gold) },
                rc.morningRing.copy(alpha = 0.16f), rc.gold, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("morning")) }
            AdhkarItem("أذكار المساء", "سكينة قبل الغروب", { IcoMoon(26.dp, rc.purple) },
                rc.eveningRing.copy(alpha = 0.28f), rc.purple, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("evening")) }
        }
        Spacer(Modifier.height(12.dp))
        // Bottom row: Sleep + Istighfar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem("أذكار النوم", "ختام يومك بالذكر", { IcoStar(26.dp, rc.purpleSleep) },
                rc.sleepRing.copy(alpha = 0.16f), rc.purpleSleep, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("sleep")) }
            AdhkarItem("الاستغفار", "طهارة القلب", { IcoDua(26.dp, rc.emerald) },
                rc.istighfarRing.copy(alpha = 0.16f), rc.emerald, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.Tasbeeh.route) }
        }
    }
}

@Composable
private fun AdhkarItem(
    label: String, hint: String, icon: @Composable () -> Unit,
    ringColor: Color, starColor: Color,
    m: Modifier = Modifier, onClick: () -> Unit = {},
) {
    val rc = LocalRafiqColors.current
    Box(m.pressAnimation(onClick = onClick)
        .clip(RoundedCornerShape(20.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))
        .padding(horizontal = 12.dp, vertical = 18.dp)) {
        // Background GeomStar decoration
        GeomStar(72.dp, starColor, 0.06f,
            Modifier.align(Alignment.BottomStart).offset((-14).dp, 14.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            // Icon disc
            Box(Modifier.size(54.dp).clip(CircleShape).background(ringColor),
                contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rc.ink, lineHeight = 20.sp)
            Spacer(Modifier.height(2.dp))
            Text(hint, fontSize = 10.sp, color = rc.inkMed, lineHeight = 14.sp,
                textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

/* ═══════════════════════════════════════════════════════
   7. PRAYER TIMES LIST — Circle(right) + Name + Time(left)
═══════════════════════════════════════════════════════ */

@Composable
private fun PrayerTimesList(prayers: List<HomeViewModel.PrayerUi>) {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        .clip(RoundedCornerShape(20.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))
        .padding(vertical = 4.dp)) {
        prayers.forEachIndexed { i, p ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(if (p.active) Modifier.background(rc.emeraldPastel.copy(alpha = 0.55f)) else Modifier)
                .padding(horizontal = 10.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                // RTL first(right): Circle + Name + Badge
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status circle
                    Box(Modifier.size(32.dp).clip(CircleShape)
                        .background(when {
                            p.done -> rc.gold.copy(alpha = 0.13f)
                            p.active -> rc.emerald.copy(alpha = 0.12f)
                            else -> rc.divider.copy(alpha = 0.5f)
                        }), contentAlignment = Alignment.Center) {
                        if (p.done) IcoCheck(13.dp, rc.gold)
                        else Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(if (p.active) rc.emerald else rc.inkMed.copy(alpha = 0.4f)))
                    }
                    // Prayer name
                    Text(p.ar, fontSize = 15.sp,
                        fontWeight = if (p.active) FontWeight.Bold else FontWeight.Medium,
                        color = if (p.active) rc.emerald else rc.ink)
                    // "الآن" badge
                    if (p.active) {
                        Text("الآن", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(rc.emerald).padding(horizontal = 10.dp, vertical = 2.dp))
                    }
                }
                // RTL last(left): Time
                Text(p.time, fontSize = 14.sp,
                    fontWeight = if (p.active) FontWeight.Bold else FontWeight.Medium,
                    color = if (p.active) rc.emerald else rc.inkMed)
            }
            if (i < prayers.lastIndex) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(1.dp)
                    .background(rc.divider.copy(alpha = 0.6f)))
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   HELPER
═══════════════════════════════════════════════════════ */

private fun toAr(n: Int): String {
    val d = charArrayOf('٠','١','٢','٣','٤','٥','٦','٧','٨','٩')
    return n.toString().map { if (it.isDigit()) d[it - '0'] else it }.joinToString("")
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
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .statusBarsPadding().navigationBarsPadding()) {
            // 1. Header
            Header(state.hijriDate,
                onSettings = { navController.navigate(RafiqRoute.Settings.route) },
                onBell     = { navController.navigate(RafiqRoute.NotificationSettings.route) })

            // 2. Basmalah
            BasmalahSection()

            // 3. Greeting card
            GreetingCard(state.greeting)

            // 4. Next Prayer
            SecLabel("الصلاة القادمة")
            NextPrayerCard(state.nextPrayerName, state.nextPrayerTime, state.countdown)
            { navController.navigate(RafiqRoute.PrayerTimes.route) }

            // 5. Wird
            SecLabel("وِرْدُكَ اليومي")
            WirdCard(state.wirdCurrent, state.wirdTotal, state.wirdPercent)

            // 6. Adhkar
            SecLabel("فئات الأذكار")
            AdhkarGrid(navController)

            // 7. Prayer times
            SecLabel("مواقيت الصلاة")
            PrayerTimesList(state.prayers)

            Spacer(Modifier.height(32.dp))
        }
    }
}
