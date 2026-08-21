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
            nextName  = home.nextPrayerName.ifEmpty { "—" },
            countdown = home.countdown.ifEmpty { "—" },
            atTime    = home.nextPrayerTime,
            stations  = day.stations,
            nowId     = day.nowStation?.id,
            done      = done,
            ar        = ar,
            onStart   = { day.nowStation?.route?.let { navController.navigate(it) } },
            onOpenDay = { navController.navigate(RafiqRoute.DayPage.route) },
        )

        Spacer(Modifier.height(10.dp))

        // الأبواب هنا هي ما لا يصله الشريط السفلي. المصحف والأدعية
        // والأذكار وأوراقي كلّها تبويبات أسفل الشاشة — فوضعها هنا يهدر
        // أثمن مساحة في التطبيق على أبوابٍ على بُعد ضغطة.
        Door(RIconOf.Misbaha, "المسبحة",
            if (hub.tasbeeh > 0) "${hub.tasbeeh.localized(ar)} تسبيحة اليوم" else "لم تبدأ اليوم",
            polished = hub.tasbeeh > 0) { navController.navigate(RafiqRoute.Tasbeeh.route) }

        Door(RIconOf.Mosque, "مواقيت اليوم",
            "${home.prayers.count { it.done }.localized(ar)} من ٥ صلوات · ${home.nextPrayerTime.localizedDigits(ar)}",
            polished = home.prayers.count { it.done } >= 5) { navController.navigate(RafiqRoute.PrayerTimes.route) }

        Door(RIconOf.Compass, "القبلة", "اتجاه الكعبة من موقعك",
            polished = false, last = true) { navController.navigate(RafiqRoute.Qibla.route) }
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

/* ── اللوح: خطّة يومك محفورةً على المعدن ────────────────────────

   عنصر واحد يقول خمسة أشياء: الصلاة القادمة · خطّتك كاملة · أين وصلت ·
   ما فعلت · وما التالي. فلا قائمة ثانية تحته ولا تكرار.

   الجلاء ينزل من الأعلى لا يصعد من الأسفل — لأن الخطّة تُقرأ من أعلى
   إلى أسفل، فلو صعد الجلاء لكان المُنجَز في الأعلى والصقل في الأسفل،
   وهما يتناقضان. النزول يجعل خطّ الضوء يقف عند موضعك تماماً.
───────────────────────────────────────────────────────────────── */

private val HeadZone = 78.dp
private val RowH     = 29.dp
private val NowRowH  = 58.dp

@Composable
private fun Plate(
    nextName: String, countdown: String, atTime: String,
    stations: List<DayCompanionViewModel.StationUi>,
    nowId: String?, done: Int, ar: Boolean,
    onStart: () -> Unit, onOpenDay: () -> Unit,
) {
    if (stations.isEmpty()) return
    val nowIdx = stations.indexOfFirst { it.id == nowId }

    // ارتفاع اللوح محسوب من الصفوف نفسها، فيقف خطّ الجلاء على حدّ صفٍّ
    // بالضبط لا في منتصفه
    val planH = stations.indices.sumOf { i -> (if (i == nowIdx) NowRowH else RowH).value.toDouble() }
    val plateH = HeadZone + planH.dp
    val cutTarget = HeadZone.value + stations.take(done.coerceAtMost(stations.size))
        .withIndex().sumOf { (i, _) -> (if (i == nowIdx) NowRowH else RowH).value.toDouble() }.toFloat()
    val cut by animateFloatAsState(cutTarget, progressSpec(950), label = "cut")

    val glint by stillableFloat(
        initialValue = -0.45f, targetValue = 0.45f, durationMs = 7000,
        easing = FastOutSlowInEasing, label = "glint", restValue = 0f,
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp)
            .height(plateH)
            .clip(RoundedCornerShape(22.dp))
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width; val h = size.height
            val cutPx = cut.dp.toPx().coerceIn(0f, h)

            drawRect(Rust)
            drawCircle(RustDeep.copy(alpha = .55f), radius = w * .46f, center = Offset(w * .78f, h * .74f))
            drawCircle(Color.White.copy(alpha = .055f), radius = w * .36f, center = Offset(w * .22f, h * .16f))
            drawCircle(Color(0xFF545E4A).copy(alpha = .30f), radius = w * .30f, center = Offset(w * .14f, h * .88f))

            // شعيرات الصقل — على الوجهين بزاوية واحدة، فتُقرأ الحافّة
            // بينهما كحدّ صقلٍ حقيقي لا كخطّ ملوّن
            var x = -h
            while (x < w + h) {
                drawLine(Color.White.copy(alpha = .035f), Offset(x, h), Offset(x + h * .18f, 0f), 1f)
                x += 4f
            }

            if (cutPx > 1f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Brass3, Brass2, Brass1, Brass0), startY = 0f, endY = cutPx,
                    ),
                    size = Size(w, cutPx),
                )
                var bx = -h
                while (bx < w + h) {
                    drawLine(Color(0xFF785C24).copy(alpha = .10f),
                        Offset(bx, cutPx), Offset(bx + cutPx * .18f, 0f), 1f)
                    bx += 4f
                }
                drawLine(Color(0xFFFFFCEC), Offset(0f, cutPx), Offset(w, cutPx), 2.4f)
                drawLine(Color(0xFFFFFCEC).copy(alpha = .30f), Offset(0f, cutPx), Offset(w, cutPx), 9f)
            }

            val gx = w * (0.5f + glint)
            drawRect(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = .18f), Color.Transparent),
                    start = Offset(gx - w * .30f, 0f), end = Offset(gx + w * .30f, h),
                ),
                size = size,
            )
        }

        Box(Modifier.matchParentSize()
            .border(1.dp, PlateGold.copy(alpha = .34f), RoundedCornerShape(22.dp)))

        Column(Modifier.fillMaxSize()) {

            // رأس اللوح — يبقى داكناً دائماً، فلا ينقلب حبره
            Column(
                Modifier.fillMaxWidth().height(HeadZone).padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("الصلاة القادمة", style = RafiqType.micro, color = PlateMed)
                        Text(nextName, style = RafiqType.ayah.copy(fontSize = RafiqType.titleL.fontSize),
                            color = PlateInk, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(countdown.localizedDigits(ar),
                            style = RafiqType.ayah.copy(fontSize = RafiqType.titleL.fontSize),
                            color = PlateGold, fontWeight = FontWeight.Bold)
                        Text(atTime.localizedDigits(ar), style = RafiqType.micro, color = PlateMed)
                    }
                }
            }

            stations.forEachIndexed { i, st ->
                PlanRow(
                    st = st, index = i, isNow = i == nowIdx, isDone = i < done,
                    ar = ar, onStart = onStart, onOpen = onOpenDay,
                )
            }
        }
    }
}

@Composable
private fun PlanRow(
    st: DayCompanionViewModel.StationUi,
    index: Int, isNow: Boolean, isDone: Boolean, ar: Boolean,
    onStart: () -> Unit, onOpen: () -> Unit,
) {
    // الحبر ينقلب مع مرور الجلاء فوق الصفّ: فاتحٌ على المعدن المصدوء،
    // داكنٌ على النحاس. كلاهما مقيس ويعبر ٤٫٥:١
    val ink  = if (isDone) LitInk else if (isNow) PlateInk else PlateInk.copy(alpha = .62f)
    val med  = if (isDone) LitMed else PlateMed
    val pulse by stillableFloat(
        initialValue = .5f, targetValue = 1f, durationMs = 1300,
        easing = FastOutSlowInEasing, label = "nowDot", restValue = 1f,
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(if (isNow) NowRowH else RowH)
            .clickable(onClick = if (isNow) onStart else onOpen)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(if (isNow) 11.dp else 7.dp).clip(CircleShape).background(
                when {
                    isNow  -> Color(0xFFFFFCEC).copy(alpha = pulse)
                    isDone -> Color(0xFF3A2E11)
                    else   -> PlateInk.copy(alpha = .26f)
                }
            )
        )
        Text(
            st.title,
            style = if (isNow) RafiqType.titleM else RafiqType.bodyS,
            color = ink,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (isNow) {
            Box(
                Modifier
                    .clip(RafiqShape.chip)
                    .background(Color(0xFF0A3B24))
                    .clickable(onClick = onStart)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) { Text("ابدأ", style = RafiqType.label, color = PlateGold) }
        } else {
            Text(clockOfLabel(st).localizedDigits(ar), style = RafiqType.micro, color = med)
        }
    }
}

private fun clockOfLabel(st: DayCompanionViewModel.StationUi): String {
    val d = java.util.Date(st.startMillis)
    return java.text.SimpleDateFormat("h:mm", java.util.Locale("ar")).format(d)
}

/* ── الأبواب ───────────────────────────────────────────────────── */

private enum class RIconOf { Misbaha, Mosque, Compass }

@Composable
private fun DoorIcon(kind: RIconOf, tint: Color) {
    when (kind) {
            RIconOf.Misbaha -> IcoMisbaha(20.dp, tint)
            RIconOf.Mosque  -> IcoMosque(20.dp, tint)
        RIconOf.Compass -> IcoCompass(20.dp, tint)
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
