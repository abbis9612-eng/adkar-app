package app.rafiqaldhikr.ui.screens.waraqa

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RafiqIconButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationUi
import app.rafiqaldhikr.ui.components.NeedsLocation
import app.rafiqaldhikr.ui.screens.home.HomeViewModel
import app.rafiqaldhikr.ui.theme.*
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ═══════════════════════════════════════════════════════════════════
   ورقة اليوم — الصفحة الرئيسية

   كانت الرئيسية رفَّ بطاقات: تحية، ثم «الآن»، ثم رحلة اليوم، ثم
   الصلاة القادمة، ثم الورد، ثم شبكة الفئات، ثم قائمة المواقيت. سبع
   بطاقات مكدّسة — وهي بنية كل تطبيق إسلامي على المتجر، ولا لوحة
   معلومات أحسّها أحد يوماً «خاصّة به».

   الورقة تقلب ذلك: صفحة واحدة متّصلة هي يومك، من الفجر أعلاها إلى
   العشاء أسفلها، تُفتح عند موضعك منه.

   والحبر هو ما يشفّر الوقت — لا لون ولا أيقونة:
     • ما مضى  → حبر كامل، والعنوان بوزن عادي (فُرِغ منه)
     • ما أنت فيه → أكبر، وحده مفتوح، وحده يحمل الفعل
     • ما لم يأتِ → باهت لم يُخطّ بعد

   الجرأة كلّها مصروفة هنا. كل ما حولها هادئ: لا تدرّجات، ولا ظلال،
   ولا أيقونات ملوّنة، ولا خلفية.
═══════════════════════════════════════════════════════════════════ */

private const val PastInk   = 1f
private const val FutureInk = 0.38f

@Composable
fun WaraqaScreen(
    navController: NavHostController,
    dayVm:  DayCompanionViewModel = koinViewModel(),
    homeVm: HomeViewModel         = koinViewModel(),
) {
    val day  by dayVm.uiState.collectAsStateWithLifecycle()
    val home by homeVm.uiState.collectAsStateWithLifecycle()
    val rc   = LocalRafiqColors.current
    val ar   = LocalArabicNumerals.current

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            // الحشوة قبل التمرير: بعده كانت تتمرّر مع المحتوى،
            // فيزحف الرأس تحت ساعة الجهاز عند السحب.
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
    ) {
        Header(
            hijri = home.hijriDate,
            onSettings = { navController.navigate(RafiqRoute.Settings.route) },
            onBell     = { navController.navigate(RafiqRoute.NotificationSettings.route) },
        )

        // محطّات اليوم موقوتة كلّها بأوقات الصلاة. بلا موقع لا ورقة —
        // والطلب هنا صريح بدل جدولٍ محسوب على مدينةٍ ليست مدينته.
        if (day.needsLocation) {
            NeedsLocation(
                message = "محطّات يومك موقوتة بأوقات الصلاة، وهي تُحسب من موقعك."
            )
        } else {
            StateLine(day.nowStation)

            Sheet(day.stations, day.nowStation, navController)

            if (day.stations.isNotEmpty()) {
                Footer(day.doneCount, day.stations.size, ar)
            }
        }
    }
}

/* ── الرأس: أقلّ ما يمكن ───────────────────────────────────────── */

@Composable
private fun Header(hijri: String, onSettings: () -> Unit, onBell: () -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RafiqIconButton(onClick = onBell, label = "التنبيهات") {
            RafiqIcon(RIcon.Bell, 18.dp, rc.emerald)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("رَفِيقُ الذِّكر", style = RafiqType.titleM, color = rc.emerald)
            Text(
                hijri.ifEmpty { "— هـ" }.localizedDigits(LocalArabicNumerals.current),
                style = RafiqType.caption, color = rc.inkMed,
            )
        }
        RafiqIconButton(onClick = onSettings, label = "الإعدادات") {
            RafiqIcon(RIcon.Settings, 18.dp, rc.emerald)
        }
    }
}

/* ── سطر الحال: الجملة الوحيدة أعلى الورقة ────────────────────── */

@Composable
private fun StateLine(now: StationUi?) {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 4.dp)) {
        if (now == null) {
            Text("ورقة يومك", style = RafiqType.ayah, color = rc.ink)
            Text("تُفتح مع أول ميقات", style = RafiqType.bodyS, color = rc.inkMed)
        } else {
            Text(
                "أنت الآن في ${now.title}",
                style = RafiqType.ayah, color = rc.ink,
            )
            Text(now.timeLabel, style = RafiqType.bodyS, color = rc.inkMed)
        }
    }
}

/* ── الورقة: عمود الحبر والمحطات ──────────────────────────────── */

private val GutterWidth = 44.dp

@Composable
private fun Sheet(
    stations: List<StationUi>,
    now: StationUi?,
    nav: NavHostController,
) {
    if (stations.isEmpty()) return
    val rc = LocalRafiqColors.current
    val meeqat = LocalMeeqat.current

    // امتلاء العمود من طبقة الميقات نفسها — لا حساب ثانٍ للوقت
    val fill by animateFloatAsState(
        targetValue = if (meeqat.resolved) meeqat.dayProgress
                      else stations.count { it.status == StationStatus.DONE ||
                                            it.status == StationStatus.PASSED }
                           .toFloat() / stations.size,
        animationSpec = progressSpec(900),
        label = "sheetFill",
    )

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // العمود يُرسم بحجم الورقة نفسها — matchParentSize لأن الارتفاع
        // داخل التمرير الرأسي غير محدود، فلا يصلح fillMaxHeight.
        Canvas(Modifier.matchParentSize()) {
            val x = size.width - (GutterWidth.toPx() / 2f)
            val w = 2.dp.toPx()
            drawRoundRect(
                color = rc.divider,
                topLeft = Offset(x - w / 2f, 0f),
                size = Size(w, size.height),
                cornerRadius = CornerRadius(w, w),
            )
            val inked = size.height * fill
            if (inked > 1f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(rc.gold, rc.goldLight), startY = 0f, endY = inked,
                    ),
                    topLeft = Offset(x - w / 2f, 0f),
                    size = Size(w, inked),
                    cornerRadius = CornerRadius(w, w),
                )
            }
        }
        Column(Modifier.fillMaxWidth()) {
            stations.forEach { st ->
                Station(st, st.id == now?.id, nav)
            }
        }
    }
}

@Composable
private fun Station(st: StationUi, isNow: Boolean, nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val done   = st.status == StationStatus.DONE
    val passed = st.status == StationStatus.PASSED
    val future = st.status == StationStatus.UPCOMING && !isNow

    val ink by animateColorAsState(
        targetValue = when {
            isNow  -> rc.ink
            future -> rc.ink.copy(alpha = FutureInk)
            else   -> rc.ink.copy(alpha = PastInk)
        },
        animationSpec = progressSpec(),
        label = "stationInk",
    )
    val soft = if (future) rc.inkMed.copy(alpha = FutureInk) else rc.inkMed

    val route = st.route
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.item)
            .then(
                if (route != null && (isNow || future))
                    Modifier.clickable { nav.navigate(route) } else Modifier
            )
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.width(GutterWidth), contentAlignment = Alignment.TopCenter) {
            Dot(done = done || passed, now = isNow, faint = future)
        }
        Column(Modifier.weight(1f).padding(end = 2.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    st.title,
                    style = if (isNow) RafiqType.ayah.copy(fontSize = RafiqType.titleL.fontSize)
                            else RafiqType.titleM.copy(
                                fontWeight = if (passed || done) FontWeight.Normal else FontWeight.Bold
                            ),
                    color = ink,
                )
                Text(clockOf(st.startMillis).localizedDigits(LocalArabicNumerals.current),
                    style = RafiqType.caption, color = soft)
            }
            Text(st.description, style = RafiqType.bodyS, color = soft,
                modifier = Modifier.padding(top = 2.dp))

            if (isNow) OpenStation(st, nav)
        }
    }
}

@Composable
private fun Dot(done: Boolean, now: Boolean, faint: Boolean) {
    val rc = LocalRafiqColors.current
    Box(Modifier.padding(top = 7.dp).size(if (now) 14.dp else 12.dp), contentAlignment = Alignment.Center) {
        if (now) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(rc.gold))
        } else {
            Box(
                Modifier.size(12.dp).clip(CircleShape)
                    .background(if (done) rc.gold else rc.bg)
                    .border(2.dp, if (done) rc.gold
                                  else rc.divider.copy(alpha = if (faint) 0.6f else 1f), CircleShape)
            )
        }
    }
}

/** البطاقة المفتوحة — واحدة في الورقة، للمحطة الحاضرة وحدها. */
@Composable
private fun OpenStation(st: StationUi, nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(st.id) {
        while (true) { nowMs = System.currentTimeMillis(); delay(1000) }
    }
    val span = (st.endMillis - st.startMillis).coerceAtLeast(1L)
    val pct  = ((nowMs - st.startMillis).toFloat() / span).coerceIn(0f, 1f)
    val left = (st.endMillis - nowMs).coerceAtLeast(0L)
    val hh = left / 3_600_000L
    val mm = (left % 3_600_000L) / 60_000L

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 11.dp)
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.divider, RafiqShape.card)
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        if (st.virtue.isNotBlank()) {
            Text(st.virtue, style = RafiqType.dhikr.copy(fontSize = RafiqType.body.fontSize),
                color = rc.ink)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                Modifier.weight(1f).height(4.dp).clip(RafiqShape.chip)
                    .background(rc.bg)
            ) {
                Box(Modifier.fillMaxWidth(pct).fillMaxHeight()
                    .clip(RafiqShape.chip).background(rc.gold))
            }
            Text(
                (if (hh > 0) "يبقى ${hh}س ${mm}د" else "يبقى ${mm}د").localizedDigits(ar),
                style = RafiqType.caption, color = rc.inkMed,
            )
        }
        val route = st.route
        if (route != null) {
            Box(
                Modifier.fillMaxWidth().height(48.dp)
                    .clip(RafiqShape.item).background(rc.ink)
                    .clickable { nav.navigate(route) },
                contentAlignment = Alignment.Center,
            ) {
                Text("ابدأ ${st.title}", style = RafiqType.label,
                    fontWeight = FontWeight.Bold, color = rc.bg)
            }
        }
    }
}

/* ── ذيل الورقة ───────────────────────────────────────────────── */

@Composable
private fun Footer(done: Int, total: Int, arabic: Boolean) {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 20.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        Row(
            Modifier.fillMaxWidth().padding(top = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("خُطّ من يومك", style = RafiqType.bodyS, color = rc.inkMed)
            Text(
                "$done من $total محطات".localizedDigits(arabic),
                style = NumbersStyle, fontSize = RafiqType.label.fontSize, color = rc.gold,
            )
        }
    }
}

private fun clockOf(millis: Long): String =
    SimpleDateFormat("h:mm", Locale.US).format(Date(millis))
