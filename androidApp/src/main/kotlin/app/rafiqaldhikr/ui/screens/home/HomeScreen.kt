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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.animations.breathingAnimation
import app.rafiqaldhikr.ui.components.*
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlin.math.*


/* Colors provided by LocalRafiqColors */

/* ═══════════════════════════════════════════════════════
   NOW CARD — رفيق اليوم: اقتراح العمل المناسب الآن
═══════════════════════════════════════════════════════ */

@Composable
private fun NowCard(
    navController: NavHostController,
    vm: app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val station = state.nowStation ?: return

    // نبضة حيّة كل ثانية لعدّاد نافذة العمل
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(station.id) {
        while (true) { nowMs = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) }
    }
    val remaining = (station.endMillis - nowMs).coerceAtLeast(0L)
    val hh = remaining / 3_600_000L
    val mm = (remaining % 3_600_000L) / 60_000L
    val ss = (remaining % 60_000L) / 1000L
    val countdown = "%02d:%02d:%02d".format(hh, mm, ss)
        .localizedDigits(LocalArabicNumerals.current)
    // تقدّم النافذة الزمنية الحالية (0..1)
    val windowLen = (station.endMillis - station.startMillis).coerceAtLeast(1L)
    val windowProgress = ((nowMs - station.startMillis).toFloat() / windowLen).coerceIn(0f, 1f)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(5.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, rc.gold.copy(alpha = 0.30f), RoundedCornerShape(24.dp))
            .clickable { navController.navigate(RafiqRoute.DayCompanion.route) }
    ) {
        // ─── خلفية سينمائية تتغيّر حسب وقت اليوم ───
        Image(
            painter = painterResource(stationBackground(station.id)),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        // حجاب متدرّج داكن لضمان وضوح النص فوق أي خلفية
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.28f), Color.Black.copy(alpha = 0.62f))
                )
            )
        )

        Column(Modifier.padding(18.dp)) {
            // ─── الرأس: ميدالية + العنوان ───
            Row(verticalAlignment = Alignment.CenterVertically) {
                StationTile(station.id, 58.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "الآن", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(rc.emerald)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                        Text(station.timeLabel, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(station.title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, lineHeight = 24.sp)
                }
            }

            // ─── الفضل بدليله — شريط ناعم كامل العرض ───
            if (station.virtue.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IcoStar(14.dp, rc.goldLight)
                    Spacer(Modifier.width(8.dp))
                    Text(station.virtue, fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.92f),
                        lineHeight = 17.sp, maxLines = 2, modifier = Modifier.weight(1f))
                }
            }

            // ─── شريط تقدّم النافذة + العدّاد ───
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.25f))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(windowProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(rc.goldLight, rc.emeraldMed))))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("تنتهي النافذة بعد", fontSize = 11.sp, color = Color.White.copy(alpha = 0.80f))
                Text(countdown, style = NumbersStyle, fontSize = 17.sp, color = rc.goldLight)
            }

            // ─── زر ابدأ — كامل العرض، فخم ───
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(15.dp))
                    .background(Brush.horizontalGradient(listOf(rc.emerald, rc.emeraldMed)))
                    .clickable { navController.navigate(station.route ?: RafiqRoute.DayCompanion.route) },
                contentAlignment = Alignment.Center
            ) {
                Text("ابدأ الآن", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/** خلفية بطاقة «الآن» حسب وقت المحطة الحالية — تُضمّن كأصول تعمل بلا إنترنت. */
@androidx.annotation.DrawableRes
private fun stationBackground(id: String): Int = when (id) {
    "wake", "fajr_morning"                 -> app.rafiqaldhikr.R.drawable.bg_time_dawn
    "duha"                                 -> app.rafiqaldhikr.R.drawable.bg_time_morning
    "dhuhr", "asr_evening", "friday_kahf"  -> app.rafiqaldhikr.R.drawable.bg_time_noon
    "maghrib"                              -> app.rafiqaldhikr.R.drawable.bg_time_dawn
    else                                   -> app.rafiqaldhikr.R.drawable.bg_time_night
}

/* ═══════════════════════════════════════════════════════
   DAY PATH — مسار محطات اليوم (خط زمني حيّ)
═══════════════════════════════════════════════════════ */

@Composable
private fun DayPath(
    navController: NavHostController,
    vm: app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    if (state.stations.isEmpty()) return

    // نبض المحطة الحالية — هالة تتنفّس
    val pulse by rememberInfiniteTransition(label = "path").animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    // المحطة الحالية (لاسم «الآن» وخلفية الوقت)
    val nowSt = state.nowStation
        ?: state.stations.firstOrNull { it.status == app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus.ACTIVE }
        ?: state.stations.first()
    val nowTitle = nowSt.title.substringBefore(" و").substringBefore(" —")

    // الأذان القادم — أول محطة صلاة قادمة بوقتها
    val prayerNames = mapOf(
        "fajr_morning" to "الفجر", "dhuhr" to "الظهر",
        "asr_evening" to "العصر", "maghrib" to "المغرب", "isha" to "العشاء",
    )
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { nowMs = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }
    val nextPrayer = state.stations
        .filter { it.id in prayerNames.keys && it.startMillis > nowMs }
        .minByOrNull { it.startMillis }
        ?: state.stations.firstOrNull { it.id in prayerNames.keys }
    val azanTime = nextPrayer?.let {
        SimpleDateFormat("h:mm", Locale.US).format(Date(it.startMillis)).localizedDigits(LocalArabicNumerals.current)
    }
    val azanRemain = nextPrayer?.let {
        val d = (it.startMillis - nowMs).coerceAtLeast(0L)
        val h = d / 3_600_000L; val m = (d % 3_600_000L) / 60_000L
        (if (h > 0) "${h} س ${m} د" else "${m} د").localizedDigits(LocalArabicNumerals.current)
    }

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, rc.gold.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        // خلفية سينمائية حسب وقت المحطة الحالية
        Image(
            painter = painterResource(stationBackground(nowSt.id)),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(Modifier.matchParentSize().background(
            Brush.verticalGradient(listOf(Color(0xCC04160E), Color(0xE60A2F27)))))

        Column(Modifier.padding(15.dp)) {
            // ─── الرأس: العنوان + الآن + عدّاد المحطات ───
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("رحلة يومك", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("الآن · $nowTitle", fontSize = 11.5.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFFF7E7AE))
                }
                Text(
                    "${state.doneCount} / ${state.stations.size} محطة".localizedDigits(LocalArabicNumerals.current),
                    style = NumbersStyle, fontSize = 11.5.sp, color = Color(0xFFF7E7AE),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color(0xFFF7E7AE).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
            Spacer(Modifier.height(14.dp))

            // ─── الخطّ الزمني ───
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.Top
            ) {
                state.stations.forEachIndexed { i, st ->
                    val done = st.status == app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus.DONE
                    val active = st.status == app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus.ACTIVE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { navController.navigate(st.route ?: RafiqRoute.DayCompanion.route) }
                            .padding(vertical = 2.dp)
                    ) {
                        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                            if (active) Box(Modifier.size(44.dp).clip(CircleShape)
                                .background(Color(0xFFF7E7AE).copy(alpha = 0.22f * pulse)))
                            Box(
                                Modifier.size(if (active) 40.dp else 34.dp).clip(CircleShape)
                                    .background(
                                        when {
                                            done   -> Brush.linearGradient(listOf(Color(0xFFE4C97A), Color(0xFFEBD48F)))
                                            active -> Brush.linearGradient(listOf(rc.emeraldMed, rc.emerald))
                                            else   -> Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.10f)))
                                        }
                                    )
                                    .then(if (active) Modifier.border(2.dp, Color(0xFFF7E7AE), CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                if (done) IcoCheck(15.dp, Color(0xFF0C4A37))
                                else StationGlyph(st.id, if (active) 21.dp else 17.dp,
                                    tint = if (active) Color.White else Color.White.copy(alpha = 0.82f))
                            }
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(
                            st.title.substringBefore(" و").substringBefore(" —"),
                            fontSize = 9.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                active -> Color(0xFFF7E7AE)
                                else   -> Color.White.copy(alpha = 0.82f)
                            },
                            maxLines = 1,
                        )
                    }
                    if (i < state.stations.lastIndex) {
                        Box(Modifier.padding(top = 21.dp).width(8.dp).height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (done) Color(0xFFE4C97A) else Color.White.copy(alpha = 0.28f)))
                    }
                }
            }

            // ─── شريط الأذان القادم (زجاجي) ───
            if (nextPrayer != null && azanTime != null) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(15.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("الأذان القادم", fontSize = 11.sp, color = Color.White.copy(alpha = 0.70f))
                        Text("${prayerNames[nextPrayer.id]} $azanTime", fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("متبقٍ $azanRemain", style = NumbersStyle, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = Color(0xFFF7E7AE))
                }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   ARC PROGRESS — Circular progress (Wird card)
═══════════════════════════════════════════════════════ */

@Composable
private fun ArcProgress(
    value: Int, max: Int, sizeDp: Dp = 68.dp,
    stroke: Color = LocalRafiqColors.current.goldLight, bg: Color = LocalRafiqColors.current.divider, sw: Dp = 5.dp,
    m: Modifier = Modifier, content: @Composable () -> Unit = {},
) {
    val pct = (value.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    val anim by animateFloatAsState(pct, tween(900, easing = FastOutSlowInEasing), label = "ap")
    Box(m.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = sw.toPx(); val r = (size.minDimension - s * 2) / 2f
            val tl = Offset(size.width / 2f - r, size.height / 2f - r); val sz = Size(r * 2, r * 2)
            drawArc(bg, 0f, 360f, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
            // لا نرسم قوس التقدم عند الصفر — الرأس المدور كان يظهر كنقطة غريبة
            if (anim > 0.005f) {
                drawArc(stroke, -90f, 360f * anim, false, tl, sz, style = Stroke(s, cap = StrokeCap.Round))
            }
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
   SECTION LABEL — Gold bar + text (React: SecLabel)
═══════════════════════════════════════════════════════ */

@Composable
private fun SecLabel(text: String) {
    val rc = LocalRafiqColors.current
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).background(rc.gold))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.ink)
    }
}

/* ═══════════════════════════════════════════════════════
   1. HEADER — Bell(right) + Title + Settings(left)
═══════════════════════════════════════════════════════ */

@Composable
private fun Header(hijri: String, onSettings: () -> Unit = {}, onBell: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        // In RTL: first child = RIGHT side → Bell
        Box {
            PillBtn(onClick = onBell) { IcoBell(c = LocalRafiqColors.current.emerald) }
            Box(Modifier.size(7.dp).align(Alignment.TopStart).offset(x = 8.dp, y = 10.dp)
                .clip(CircleShape).background(LocalRafiqColors.current.error))
        }
        // Center: Title + date
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("رَفِيقُ الذِّكر", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = LocalRafiqColors.current.emerald, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text(hijri.ifEmpty { "— هـ" }.localizedDigits(LocalArabicNumerals.current),
                fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = LocalRafiqColors.current.inkMed,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(LocalRafiqColors.current.chipBg).padding(horizontal = 12.dp, vertical = 2.dp))
        }
        // In RTL: last child = LEFT side → Settings
        PillBtn(onClick = onSettings) { IcoGear(c = LocalRafiqColors.current.emerald) }
    }
}

/* ═══════════════════════════════════════════════════════
   2. GEOM STAR + BASMALAH
═══════════════════════════════════════════════════════ */

@Composable
private fun BasmalahSection() {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        GeomStar(60.dp, rc.gold, 0.30f, Modifier.breathingAnimation(minScale = 0.96f, maxScale = 1.04f))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(38.dp).height(1.dp).background(
                Brush.horizontalGradient(listOf(Color.Transparent, rc.goldLight))))
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ",
                fontSize = 19.sp,
                fontFamily = app.rafiqaldhikr.ui.theme.AmiriFamily,
                color = rc.gold,
            )
            Box(Modifier.width(38.dp).height(1.dp).background(
                Brush.horizontalGradient(listOf(rc.goldLight, Color.Transparent))))
        }
    }
}

/* ═══════════════════════════════════════════════════════
   3. GREETING CARD — Compact (v2)
═══════════════════════════════════════════════════════ */

@Composable
private fun GreetingCard(greeting: String, streak: Long) {
    val rc = LocalRafiqColors.current
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(16.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(16.dp))) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            // RTL: first child(right) = greeting, last child(left) = badges
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(greeting.ifEmpty { "صَبَاحُ الخَيْر" }, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium, color = rc.inkMed)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // شارة السلسلة الحيّة — أيام المواظبة
                    if (streak > 0) {
                        Row(Modifier.clip(RoundedCornerShape(16.dp))
                            .background(rc.gold.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IcoFlame(13.dp, rc.gold)
                            Text(streak.toString().localizedDigits(LocalArabicNumerals.current),
                                style = NumbersStyle, fontSize = 12.sp, color = rc.gold)
                        }
                    }
                    Row(Modifier.clip(RoundedCornerShape(16.dp)).background(rc.emerald)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        IcoBell(11.dp, Color.White)
                        Text("وقت الذِكر", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Text("وَاذْكُر رَّبَّكَ كَثِيرًا", fontSize = 24.sp,
                fontFamily = app.rafiqaldhikr.ui.theme.QuranFamily,
                fontWeight = FontWeight.Medium,
                color = rc.gold, textAlign = TextAlign.Center,
                lineHeight = 34.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            Text("سورة آل عمران – ٤١", fontSize = 10.sp, color = rc.inkMed,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

/* ═══════════════════════════════════════════════════════
   4. NEXT PRAYER CARD — Green gradient + mini strip
═══════════════════════════════════════════════════════ */

@Composable
private fun NextPrayerCard(
    name: String, time: String, countdown: String,
    prevMillis: Long = 0L, nextMillis: Long = 0L,
    onClick: () -> Unit = {},
) {
    val rc = LocalRafiqColors.current
    val arabic = LocalArabicNumerals.current
    val tr = rememberInfiniteTransition(label = "dot")
    val da by tr.animateFloat(1f, 0.5f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "da")

    // نسبة الوقت المنقضي بين الصلاتين — يتحدث كل ثانية مع العداد
    val progress = if (nextMillis > prevMillis && prevMillis > 0) {
        ((System.currentTimeMillis() - prevMillis).toFloat() /
            (nextMillis - prevMillis).toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = rc.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, rc.divider)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // مشهد سماء يتبع الصلاة القادمة نفسها — لون كل صلاة يميزها
            PrayerScene(prayerName = name, modifier = Modifier.fillMaxWidth()) {
              Box(Modifier.fillMaxWidth()) {
                // Content: RTL → first(right)=name, last(left)=time
                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        // RIGHT side in RTL: prayer name
                        Column {
                            Text(name.ifEmpty { "—" }, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold,
                                color = Color.White, lineHeight = 50.sp)
                            Text("باقي على الأذان", fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f))
                        }
                        // LEFT side in RTL: time
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(time.ifEmpty { "—" }.localizedDigits(arabic),
                                style = NumbersStyle, fontSize = 34.sp,
                                color = rc.goldLight, lineHeight = 38.sp)
                            Text("وقت الصلاة", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // ═══ العداد المقسم: ساعة · دقيقة · ثانية ═══
                    CountdownSegments(countdown = countdown, arabic = arabic, colonAlpha = da)

                    Spacer(Modifier.height(14.dp))

                    // ═══ شريط تقدم الوقت بين الصلاتين ═══
                    Box(Modifier.fillMaxWidth().height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.18f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(progress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(rc.goldLight))
                    }
                }
              }
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   COUNTDOWN SEGMENTS — خانات ساعة/دقيقة/ثانية زجاجية
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
                .widthIn(min = 58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.13f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.localizedDigits(arabic),
                style = NumbersStyle, fontSize = 24.sp,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.65f))
    }
}

@Composable
private fun CountdownColon(alpha: Float) {
    Text(
        ":", style = NumbersStyle, fontSize = 22.sp,
        color = Color.White.copy(alpha = alpha),
        modifier = Modifier.padding(horizontal = 6.dp).offset(y = (-8).dp)
    )
}

/* ═══════════════════════════════════════════════════════
   5. WIRD PROGRESS — Cream card (React style)
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
                    Text("الورد اليومي", fontSize = 11.sp, color = rc.inkMed)
                    Spacer(Modifier.height(4.dp))
                    Text("الحمد لله", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = rc.emerald)
                    Text("على كل حال وفي كل أوان", fontSize = 12.sp, color = rc.inkMed,
                        modifier = Modifier.padding(top = 4.dp))
                }
                // حلقة الإنجاز — مسار واضح ورقم بنمط الأرقام الموحد
                ArcProgress(current, total, 74.dp, rc.gold, rc.emeraldPastel2, 6.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$percent٪".localizedDigits(LocalArabicNumerals.current),
                            style = NumbersStyle, fontSize = 17.sp, color = rc.emerald
                        )
                        Text("إنجاز", fontSize = 10.sp, color = rc.inkMed)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            // Progress bar
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(10.dp)).background(rc.divider)) {
                Box(Modifier.fillMaxWidth((percent / 100f).coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(rc.gold, rc.goldLight))))
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
   6. ADHKAR GRID — 2×2 with centered icons + GeomStar bg
═══════════════════════════════════════════════════════ */

@Composable
private fun AdhkarGrid(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    Column(Modifier.padding(horizontal = 16.dp)) {
        // Top row: Morning + Evening
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem("أذكار الصباح", R.drawable.ic_sun, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("morning")) }
            AdhkarItem("أذكار المساء", R.drawable.ic_sunset, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("evening")) }
        }
        Spacer(Modifier.height(12.dp))
        // Bottom row: Sleep + Istighfar
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdhkarItem("أذكار النوم", R.drawable.ic_moon, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.DhikrReading.withCategory("sleep")) }
            AdhkarItem("الاستغفار", R.drawable.ic_st_dua, Modifier.weight(1f))
            { nav.navigate(RafiqRoute.Tasbeeh.route) }
        }
    }
}

@Composable
private fun AdhkarItem(
    label: String, iconRes: Int,
    m: Modifier = Modifier, onClick: () -> Unit = {},
) {
    val rc = LocalRafiqColors.current
    Box(m.clip(RoundedCornerShape(20.dp)).background(rc.card)
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 20.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()) {
            CategoryBadge(iconRes, 56.dp)
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rc.ink, lineHeight = 20.sp)
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
        .border(1.dp, rc.divider, RoundedCornerShape(20.dp))) {
        prayers.forEachIndexed { i, p ->
            Row(Modifier.fillMaxWidth()
                .then(if (p.active) Modifier.background(rc.emerald.copy(alpha = 0.03f)) else Modifier)
                .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                // RTL first(right): Circle + Name + Badge
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status circle
                    Box(Modifier.size(30.dp).clip(CircleShape)
                        .background(when {
                            p.done -> rc.gold.copy(alpha = 0.13f)
                            p.active -> rc.emerald.copy(alpha = 0.10f)
                            else -> rc.divider.copy(alpha = 0.5f)
                        }), contentAlignment = Alignment.Center) {
                        if (p.done) IcoCheck(13.dp, rc.gold)
                        else Box(Modifier.size(7.dp).clip(CircleShape)
                            .background(if (p.active) rc.emerald else rc.inkMed.copy(alpha = 0.5f)))
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
                                .background(rc.emerald).padding(horizontal = 9.dp, vertical = 2.dp))
                    }
                }
                // RTL last(left): Time
                Text(p.time.localizedDigits(LocalArabicNumerals.current),
                    style = NumbersStyle, fontSize = 17.sp,
                    color = if (p.active) rc.emerald else rc.ink)
            }
            if (i < prayers.lastIndex) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(rc.divider))
            }
        }
    }
}

/* ═══════════════════════════════════════════════════════
   HELPER
═══════════════════════════════════════════════════════ */

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
        // خلفية حيّة تتغيّر مع وقت اليوم (خلف كل المحتوى)
        LivingSkyBackground(Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .statusBarsPadding()) {
            // 1. Header
            Header(state.hijriDate,
                onSettings = { navController.navigate(RafiqRoute.Settings.route) },
                onBell     = { navController.navigate(RafiqRoute.NotificationSettings.route) })

            // 2. Basmalah
            BasmalahSection()

            // 3. Greeting card + streak
            GreetingCard(state.greeting, state.streak.current)

            // 3.5 رفيق اليوم — العمل المناسب الآن + مسار اليوم
            SecLabel("رفيق اليوم")
            NowCard(navController)
            Spacer(Modifier.height(10.dp))
            DayPath(navController)

            // 4. Next Prayer
            SecLabel("الصلاة القادمة")
            LocationBadge(
                hasLocation = state.hasLocation,
                onLocationFetched = { lat, lng -> viewModel.saveLocation(lat, lng) }
            )
            NextPrayerCard(
                name = state.nextPrayerName,
                time = state.nextPrayerTime,
                countdown = state.countdown,
                prevMillis = state.prevPrayerMillis,
                nextMillis = state.nextPrayerMillis,
            ) { navController.navigate(RafiqRoute.PrayerTimes.route) }

            // 5. Wird
            SecLabel("وِرْدُكَ اليومي")
            WirdCard(state.wirdCurrent, state.wirdTotal, state.wirdPercent)

            // 6. Adhkar
            SecLabel("فئات الأذكار")
            AdhkarGrid(navController)

            // 7. Prayer times
            SecLabel("مواقيت الصلاة")
            PrayerTimesList(state.prayers)

            Spacer(Modifier.height(28.dp))
        }
    }
}
