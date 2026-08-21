package app.rafiqaldhikr.ui.screens.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.*
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel
import app.rafiqaldhikr.ui.screens.home.HomeViewModel
import app.rafiqaldhikr.ui.theme.*
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import org.koin.androidx.compose.koinViewModel

/* ═══════════════════════════════════════════════════════════════════
   الرئيسية — مركز انطلاق لا صفحة قراءة

   كانت الرئيسية ورقةَ اليوم: صفحةً تجلس معها وتقرؤها، ولا يصلها من
   أقسام التطبيق إلا الإعدادات. والمستخدم يفتح تطبيقه عشر مرات في اليوم
   ليصل إلى شيء — لا ليقرأ.

   فصارت مركزاً: ستّة أبواب، وثلاثة منها (المسبحة والمواقيت والقبلة) لا
   باب لها في الشريط السفلي إطلاقاً. وورقة اليوم بقيت كما هي خلف باب
   «يومك» — فهي أحسن ما في التطبيق، لكن مكانها ليس الشاشة الأولى.

   وكل باب يحمل حاله: قضيبٌ مصقول إن فعلتَه اليوم، مصدوءٌ إن لم تفعله.
   من معنى «صدأ القلب وجلاؤه» عند ابن القيّم في الوابل الصيّب: التقدّم
   حالٌ لا نسبةٌ مئوية.
═══════════════════════════════════════════════════════════════════ */

private val Rust      = Color(0xFF39443A)
private val RustDeep  = Color(0xFF2A332C)
private val Brass0    = Color(0xFFBE9F5C)
private val Brass1    = Color(0xFFE9D49E)
private val Brass2    = Color(0xFFF9EDC8)
private val Brass3    = Color(0xFFFFFEF6)
private val PlateInk  = Color(0xFFF6F2E6)
private val PlateMed  = Color(0xFFA9B7A7)
private val PlateGold = Color(0xFFF0CE7E)
private val LitInk    = Color(0xFF201A0C)
private val LitMed    = Color(0xFF4E4326)
private val LitGold   = Color(0xFF6B4708)

/** فوق هذه النسبة يبلغ الجلاء منطقة النصّ، فينقلب الحبر داكناً ليبقى مقروءاً. */
private const val LitThreshold = 0.68f

@Composable
fun HomeHubScreen(
    navController: NavHostController,
    hubVm:  HomeHubViewModel       = koinViewModel(),
    dayVm:  DayCompanionViewModel  = koinViewModel(),
    homeVm: HomeViewModel          = koinViewModel(),
) {
    val hub  by hubVm.uiState.collectAsStateWithLifecycle()
    val day  by dayVm.uiState.collectAsStateWithLifecycle()
    val home by homeVm.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    val total = day.stations.size.coerceAtLeast(1)
    val done  = day.doneCount

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        HubHeader(home.hijriDate, ar)

        Plate(
            nextName = home.nextPrayerName.ifEmpty { "—" },
            countdown = home.countdown.ifEmpty { "—" },
            atTime = home.nextPrayerTime,
            done = done,
            total = total,
            ar = ar,
            onClick = { navController.navigate(RafiqRoute.Profile.route) },
        )

        day.nowStation?.let { now ->
            DueNow(
                title = now.title,
                sub = now.description,
                onStart = { now.route?.let { navController.navigate(it) } },
            )
            ThenLine(day.stations, now, ar)
        }

        Spacer(Modifier.height(6.dp))

        Door(RIconOf.Quran, "المصحف",
            if (hub.quranPages > 0) "${hub.quranPages.localized(ar)} صفحة اليوم" else "ابدأ ورد اليوم",
            polished = hub.quranPages > 0) { navController.navigate(RafiqRoute.QuranList.route) }

        Door(RIconOf.Misbaha, "المسبحة",
            if (hub.tasbeeh > 0) "${hub.tasbeeh.localized(ar)} تسبيحة اليوم" else "لم تبدأ اليوم",
            polished = hub.tasbeeh > 0) { navController.navigate(RafiqRoute.Tasbeeh.route) }

        Door(RIconOf.Dua, "الأدعية", "أدعية مأثورة بمصادرها",
            polished = false) { navController.navigate(RafiqRoute.DuaCategories.route) }

        Door(RIconOf.Mosque, "مواقيت اليوم",
            "${home.prayers.count { it.done }.localized(ar)} من ٥ صلوات",
            polished = false) { navController.navigate(RafiqRoute.PrayerTimes.route) }

        Door(RIconOf.Compass, "القبلة", "اتجاه الكعبة من موقعك",
            polished = false) { navController.navigate(RafiqRoute.Qibla.route) }

        Door(RIconOf.Day, "يومك",
            "${done.localized(ar)} من ${total.localized(ar)} محطّات",
            polished = done > 0, half = done in 1 until total,
            last = true) { navController.navigate(RafiqRoute.DayPage.route) }
    }
}

/* ── الرأس ─────────────────────────────────────────────────────── */

@Composable
private fun HubHeader(hijri: String, ar: Boolean) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("رفيق الذِّكر", style = RafiqType.caption, color = rc.inkMed)
        Text(hijri.localizedDigits(ar), style = RafiqType.caption, color = rc.inkMed)
    }
}

/* ── اللوح ─────────────────────────────────────────────────────── */

@Composable
private fun Plate(
    nextName: String, countdown: String, atTime: String,
    done: Int, total: Int, ar: Boolean, onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val target = (done.toFloat() / total).coerceIn(0f, 1f)
    val fill by animateFloatAsState(target, progressSpec(950), label = "polish")
    val lit = fill >= LitThreshold

    // اللمعة تمرّ على المعدن — تجعله يبدو معدناً حيّاً لا صورة معدن
    val glint by stillableFloat(
        initialValue = -0.45f, targetValue = 0.45f, durationMs = 7000,
        easing = FastOutSlowInEasing, label = "glint", restValue = 0f,
    )

    val ink  by animateColorAsState(if (lit) LitInk  else PlateInk, progressSpec(900), label = "pInk")
    val med  by animateColorAsState(if (lit) LitMed  else PlateMed, progressSpec(900), label = "pMed")
    val gold by animateColorAsState(if (lit) LitGold else PlateGold, progressSpec(900), label = "pGold")

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp)
            .height(212.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width; val h = size.height

            // المعدن المصدوء — تبقيع خفيف يمنع السطح المسطّح
            drawRect(Rust)
            drawCircle(RustDeep.copy(alpha = .55f), radius = w * .42f, center = Offset(w * .76f, h * .70f))
            drawCircle(Color.White.copy(alpha = .06f), radius = w * .34f, center = Offset(w * .24f, h * .20f))
            drawCircle(Color(0xFF545E4A).copy(alpha = .34f), radius = w * .28f, center = Offset(w * .16f, h * .86f))

            // شعيرات الصقل — على الوجهين، فتُقرأ الحافّة بينهما كحدّ صقل
            var x = -h
            while (x < w + h) {
                drawLine(Color.White.copy(alpha = .035f), Offset(x, h), Offset(x + h * .35f, 0f), 1f)
                x += 4f
            }

            // الجلاء يصعد من الأسفل بمقدار ما أُتمّ من اليوم
            val top = h * (1f - fill)
            if (fill > 0.001f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Brass3, Brass2, Brass1, Brass0), startY = top, endY = h,
                    ),
                    topLeft = Offset(0f, top), size = Size(w, h - top),
                )
                var bx = -h
                while (bx < w + h) {
                    drawLine(Color(0xFF785C24).copy(alpha = .10f),
                        Offset(bx, h), Offset(bx + h * .35f, top), 1f)
                    bx += 4f
                }
                // خطّ الضوء عند حدّ الصقل — كأن يداً تمسحه للتوّ
                drawLine(Color(0xFFFFFCEC), Offset(0f, top), Offset(w, top), 2.4f)
                drawLine(Color(0xFFFFFCEC).copy(alpha = .32f), Offset(0f, top), Offset(w, top), 9f)
            }

            // اللمعة المارّة
            val gx = w * (0.5f + glint)
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = .20f), Color.Transparent),
                    start = Offset(gx - w * .30f, 0f), end = Offset(gx + w * .30f, h),
                ),
                size = size,
            )
        }

        // الحافّة الذهبية
        Box(Modifier.matchParentSize()
            .border(1.dp, PlateGold.copy(alpha = .34f), RoundedCornerShape(22.dp)))

        // محطّات اليوم على الحافّة — شكل يومك كلّه بلا سطر إضافي
        Column(
            Modifier.fillMaxHeight().padding(start = 12.dp, top = 20.dp, bottom = 20.dp).width(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (i in (total - 1) downTo 0) {
                StationMark(state = when {
                    i < done - 1  -> 2
                    i == done - 1 -> 1
                    else          -> 0
                })
            }
        }

        Column(
            Modifier.fillMaxSize().padding(start = 32.dp, top = 18.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("الصلاة القادمة", style = RafiqType.micro, color = med)
                    Text(nextName, style = RafiqType.ayah.copy(fontSize = RafiqType.display.fontSize),
                        color = ink, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(countdown.localizedDigits(ar),
                        style = RafiqType.ayah.copy(fontSize = RafiqType.titleL.fontSize),
                        color = gold, fontWeight = FontWeight.Bold)
                    Text(atTime.localizedDigits(ar), style = RafiqType.micro, color = med)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Text(
                    if (done >= total) "جُلي يومك كلّه"
                    else "جُلي ${done.localized(ar)} من ${total.localized(ar)}",
                    style = RafiqType.micro, color = med,
                )
                Text("أوراقي ←", style = RafiqType.micro, color = med)
            }
        }
    }
}

@Composable
private fun StationMark(state: Int) {
    // ٢ = أُنجز · ١ = الآن · ٠ = لم يأتِ
    val pulse by stillableFloat(
        initialValue = .55f, targetValue = 1f, durationMs = 1300,
        easing = FastOutSlowInEasing, label = "markPulse", restValue = 1f,
    )
    val size = if (state == 1) 12.dp else 7.dp
    Box(
        Modifier.size(size).clip(CircleShape).background(
            when (state) {
                2    -> Color(0xFF3A2E11)
                1    -> Color(0xFFFFFCEC).copy(alpha = pulse)
                else -> PlateInk.copy(alpha = .24f)
            }
        )
    )
}

/* ── ما وقته الآن ──────────────────────────────────────────────── */

@Composable
private fun DueNow(title: String, sub: String, onStart: () -> Unit) {
    val rc = LocalRafiqColors.current
    // وميض خافت على القضيب المصدوء — الشيء الوحيد الذي يومض في الشاشة،
    // لأنه الشيء الوحيد الذي لم يُفعل بعد
    val hint by stillableFloat(
        initialValue = 0f, targetValue = 1f, durationMs = 3400,
        easing = LinearEasing, repeatMode = RepeatMode.Restart,
        label = "dueHint", restValue = 0f,
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = .30f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(Modifier.width(6.dp).height(52.dp).clip(RoundedCornerShape(3.dp)).background(Rust)) {
            val t = ((hint - .62f) / .20f).coerceIn(0f, 1f)
            if (hint > .62f) {
                Box(
                    Modifier.fillMaxWidth().fillMaxHeight(.34f)
                        .offset(y = (52.dp * (t * 1.6f - .3f)))
                        .background(Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFFFFFCEC).copy(alpha = .45f), Color.Transparent)))
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = RafiqType.titleL, color = rc.ink, fontWeight = FontWeight.Bold)
            Text(sub, style = RafiqType.caption, color = rc.gold, maxLines = 2)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(13.dp))
                .background(rc.emerald)
                .clickable(onClick = onStart)
                .padding(horizontal = 22.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("ابدأ", style = RafiqType.titleM, color = rc.onEmerald)
        }
    }
}

@Composable
private fun ThenLine(
    stations: List<DayCompanionViewModel.StationUi>,
    now: DayCompanionViewModel.StationUi,
    ar: Boolean,
) {
    val rc = LocalRafiqColors.current
    val idx = stations.indexOfFirst { it.id == now.id }
    val next = if (idx >= 0) stations.drop(idx + 1).take(2) else emptyList()
    val text = if (next.isEmpty()) "وهي آخر محطّات يومك"
               else next.joinToString(" · ") { "ثمّ ${it.title} ${it.timeLabel}" }
    Text(
        text.localizedDigits(ar),
        style = RafiqType.caption, color = rc.inkMed,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp),
        maxLines = 1,
    )
}

/* ── الأبواب ───────────────────────────────────────────────────── */

private enum class RIconOf { Quran, Misbaha, Dua, Mosque, Compass, Day }

@Composable
private fun DoorIcon(kind: RIconOf, tint: Color) {
    when (kind) {
        RIconOf.Quran   -> IcoQuran(20.dp, tint)
        RIconOf.Misbaha -> IcoMisbaha(20.dp, tint)
        RIconOf.Dua     -> IcoDua(20.dp, tint)
        RIconOf.Mosque  -> IcoMosque(20.dp, tint)
        RIconOf.Compass -> IcoCompass(20.dp, tint)
        RIconOf.Day     -> IcoBookmark(20.dp, tint)
    }
}

@Composable
private fun Door(
    icon: RIconOf, title: String, sub: String,
    polished: Boolean, half: Boolean = false, last: Boolean = false,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RafiqShape.item)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            MetalBar(polished = polished, half = half)
            DoorIcon(icon, rc.inkMed)
            Column(Modifier.weight(1f)) {
                Text(title, style = RafiqType.titleM, color = rc.ink)
                Text(sub, style = RafiqType.caption, color = rc.inkMed, maxLines = 1)
            }
            Text("‹", style = RafiqType.titleM, color = rc.divider)
        }
        if (!last) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(1.dp).background(rc.divider))
        }
    }
}

/** قضيب الحال: مصقول = فعلتَه اليوم · مصدوء = لم تفعله · نصفان = جزئي. */
@Composable
private fun MetalBar(polished: Boolean, half: Boolean) {
    val rc = LocalRafiqColors.current
    Box(Modifier.width(6.dp).height(34.dp).clip(RoundedCornerShape(3.dp))) {
        when {
            half -> {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(.52f)
                        .background(Brush.verticalGradient(listOf(Brass3, Brass1))))
                    Box(Modifier.fillMaxWidth().weight(.48f).background(rc.divider))
                }
            }
            polished -> Box(Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Brass3, Color(0xFFD9BE7C)))))
            else -> Box(Modifier.fillMaxSize().background(rc.divider))
        }
    }
}

private fun Int.localized(arabic: Boolean): String = toString().localizedDigits(arabic)
