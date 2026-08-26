package app.rafiqaldhikr.ui.screens.hub

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
   الرئيسية — كلمةٌ تتصدّرها، وأربعُ طبقاتٍ تحتها

   كان قبلها «اللوح»: خطّة اليوم محفورةً على معدن. عنصرٌ واحد يحمل تسعة
   أسطر، والصقلُ ينزل عليه بمقدار ما أتممت. حُذف كلُّه — لا لأنه رديء،
   بل لأن الشاشة صارت تُقرأ قائمةً رأسية طويلة، والمستخدم يفتح تطبيقه
   ليفعل شيئاً في ثوانٍ لا ليقرأ لوحاً.

   والبنية الآن أربع طبقات لا عشر:

     ١) الكلمة    — اقتباسٌ مسنَد يتصدّر الشاشة، وتحته مصدره ظاهراً
     ٢) سطرٌ رفيع — الصلاة القادمة وكم بقي لها
     ٣) زوجٌ كبير — ما تفعله الآن، وزرُّ «ابدأ» بجانبه لا تحته
     ٤) صفّان     — صفُّ اليوم، وصفُّ الأبواب الثلاثة

   وصفُّ اليوم بديلٌ عن القائمة السباعية: تسعةُ أسماءٍ قصيرة في سطر،
   ما مضى باهتٌ والحاضرُ داكنٌ تحته علامة. المكانُ نفسه هو المعلومة —
   فلا عدّاد ولا علامةُ صحّ ولا شريطُ تقدّم. ومن أراد التفصيل ضغطه
   فانفتح بالأسماء الكاملة والأوقات.

   ═══ الأخضران ═══
   [RafiqPalette.emerald] داكنٌ يُقرأ نصّاً على الورق (7.61) ولا يصلح
   ملءاً. و[RafiqPalette.emeraldFill] فاتحٌ عكسه: ملءٌ ممتاز (8.54 بحبر
   داكن فوقه) ونصٌّ لا يُقرأ (1.66). لونان لدورين — لا لونٌ واحد يُجبَر
   على الدورين.

   ═══ سطحٌ واحد ═══
   لا بطاقةَ في هذه الشاشة ولا حبّةَ ملوّنة: الورق يمتدّ من أعلاها إلى
   أسفلها، والفصلُ من خطوط `divider` الرفيعة والفراغ وثقلِ الحرف.
═══════════════════════════════════════════════════════════════════ */

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

    // التوزيع كما في النموذج: الكلمة تأخذ ما بقي من الارتفاع فتتوسّطه
    // وتدفع الطبقات الثلاث إلى أسفل الشاشة. بلا هذا الوزن تتكدّس الشاشة
    // في أعلاها ويبقى ثلثها الأسفل فراغاً.
    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
    ) {
        HubTopBar(onSettings = { navController.navigate(RafiqRoute.Settings.route) })

        Greeting(hijri = home.hijriDate, ar = ar)

        // بلا verticalScroll: الحاوية المُمرَّرة تُقاس بحجم محتواها فيسقط
        // contentAlignment ولا تتوسّط الكلمة. والنصوص قصيرة فلا تحتاج تمريراً.
        Box(
            Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            hub.wisdom?.let { WordOfDay(it) }
        }

        // خطٌّ يفصل منطقتين متناقضتين: كلمةٌ تتأمّلها فوقه، وعملٌ تفعله
        // تحته. كانتا ملتصقتين بصفر مسافة، فيلتقي السند بسطر الوقت.
        Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        Spacer(Modifier.height(26.dp))

        NextPrayerLine(
            name      = home.nextPrayerName.ifEmpty { "—" },
            countdown = home.countdown.ifEmpty { "—" },
            ar        = ar,
        )

        NowAction(
            station    = day.nowStation,
            ar         = ar,
            onStart    = { day.nowStation?.route?.let { navController.navigate(it) } },
            onDayPage  = { navController.navigate(RafiqRoute.DayPage.route) },
        )

        DayRow(
            stations   = day.stations,
            nowId      = day.nowStation?.id,
            ar         = ar,
            onOpen     = { navController.navigate(RafiqRoute.DayPage.route) },
        )

        DoorsRow(
            onTasbeeh = { navController.navigate(RafiqRoute.Tasbeeh.route) },
            onQibla   = { navController.navigate(RafiqRoute.Qibla.route) },
            onTimes   = { navController.navigate(RafiqRoute.PrayerTimes.route) },
        )
    }
}

/* ── الشريط العلوي ─────────────────────────────────────────────── */

@Composable
private fun HubTopBar(onSettings: () -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 6.dp))
                    .background(rc.emeraldFill),
                contentAlignment = Alignment.Center,
            ) {
                IcoMoon(24.dp, rc.onEmeraldFill)
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text("رفيق الذِّكر", style = RafiqType.titleL, color = rc.emerald)
                Text("رفيقُك في يومك", style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
        RafiqIconButton(onClick = onSettings, label = "الإعدادات") {
            RafiqIcon(RIcon.Settings, size = 21.dp, tint = rc.inkMed)
        }
    }
}

/* ── التحية ─────────────────────────────────────────────────────── */

@Composable
private fun Greeting(hijri: String, ar: Boolean) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text("السلام عليكم", style = RafiqType.bodyS, color = rc.inkMed)
            Spacer(Modifier.height(3.dp))
            Text(greetingText(), style = RafiqType.titleXL, color = rc.ink)
        }
        Text(
            hijri.localizedDigits(ar),
            style = RafiqType.bodyS,
            color = rc.inkMed,
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
}

/**
 * تحيّةُ الوقت. تُشتقّ من ساعة الجهاز لا من مواقيت الصلاة عمداً: الجملة
 * مجاملةٌ لا معلومة، ولو ربطتُها بالمواقيت لصارت تقول «صباحٌ مبارك» بعد
 * الفجر في ليل الشتاء الطويل.
 */
private fun greetingText(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (h) {
        in 4..10  -> "صباحٌ مبارك"
        in 11..15 -> "نهارٌ طيّب"
        in 16..19 -> "مساءٌ مطمئن"
        else      -> "ليلةٌ هادئة"
    }
}

/* ── الكلمة — بطلةُ الشاشة ──────────────────────────────────────

   الاقتباس أوّل ما تقع عليه العين، ومصدرُه تحته مباشرةً لا في حاشية:
   خيطٌ أخضر رأسيّ ثم اسم القائل ثم كتابه. وهذه علامة السند نفسها التي
   تتكرّر حيثما ورد نصٌّ ديني في التطبيق.
──────────────────────────────────────────────────────────────── */

@Composable
private fun WordOfDay(w: app.rafiq.domain.model.Wisdom) {
    val rc = LocalRafiqColors.current
    Column(
        Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            w.text,
            style = RafiqType.ayah,
            color = rc.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(rc.emeraldFill),
            )
            Spacer(Modifier.width(9.dp))
            Column {
                Text(w.author, style = RafiqType.titleM, color = rc.emerald)
                Text(w.source, style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
    }
}

/* ── الطبقة ١: سطرٌ رفيع ────────────────────────────────────────── */

@Composable
private fun NextPrayerLine(name: String, countdown: String, ar: Boolean) {
    val rc = LocalRafiqColors.current
    val human = humanCountdown(countdown)
    Row(verticalAlignment = Alignment.Bottom) {
        // بلا موقعٍ لا وقت — و«الفجر» وحدها اسمٌ معلَّق يوهم أن الوقت
        // قادم. تقول الشاشة سبب النقص بدل عرض نصفِ معلومة.
        if (human == null) {
            // «المواقيت بانتظار موقعك» تتكلّم بلسان النظام عن نقصٍ فيه.
            // والمستخدم لا يفتح تطبيقه ليعرف ما ينقص التطبيق — يفتحه
            // ليذكر. فالجملة تخاطبه هو، لا تشتكي له.
            Text("أذكارك", style = RafiqType.titleM, color = rc.ink)
            Spacer(Modifier.width(9.dp))
            Text("في انتظارك", style = RafiqType.bodyS, color = rc.inkMed)
        } else {
            Text(name, style = RafiqType.titleM, color = rc.ink)
            Spacer(Modifier.width(9.dp))
            Text("بعد ${human.localizedDigits(ar)}", style = RafiqType.bodyS, color = rc.inkMed)
        }
    }
}

/**
 * «٠٠:٥٦:٤٤» رقمُ مؤقّتٍ لا جملةُ رفيق. الشاشة تقول «بعد ٥٦ دقيقة» —
 * والثواني تُحذف لأنها تُعيد التركيب كل ثانية بلا فائدة للقارئ.
 *
 * وتصريفُ العدد عربيّ: ساعة · ساعتان · ٣ ساعات — لا «١ ساعة».
 * تُرجع null حين لا عدّاد بعد، فيُحذف السطر كلّه.
 */
private fun humanCountdown(raw: String): String? {
    val p = raw.split(":")
    if (p.size != 3) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    if (h == 0 && m == 0) return null
    val hs = when (h) {
        0    -> null
        1    -> "ساعة"
        2    -> "ساعتين"
        in 3..10 -> "$h ساعات"
        else -> "$h ساعة"
    }
    val ms = when (m) {
        0    -> null
        1    -> "دقيقة"
        2    -> "دقيقتين"
        in 3..10 -> "$m دقائق"
        else -> "$m دقيقة"
    }
    return listOfNotNull(hs, ms).joinToString(" و")
}

/* ── الطبقة ٢: زوجٌ كبير — الفعلُ ملتصقٌ بما يفعله ─────────────── */

@Composable
private fun NowAction(
    station: DayCompanionViewModel.StationUi?,
    ar: Boolean,
    onStart:    () -> Unit,
    onDayPage:  () -> Unit,
) {
    val rc = LocalRafiqColors.current

    /*  زرٌّ لا يفعل شيئاً ولا يقول لماذا هو أسوأ من غياب الزرّ.
     *
     *  كان `enabled = station?.route != null`، فيموت في حالتين:
     *    ١) لا موقع ← لا مواقيت ← لا محطّات ← station == null
     *    ٢) «الاستيقاظ» و«صلاة الضحى» route = null أصلاً — لا شاشة
     *       أذكارٍ لهما، وإنما تفصيلُهما في صفحة اليوم.
     *
     *  الآن لكل حالةٍ وجهةٌ ونصٌّ يشرحها. ولا يُعطَّل أبداً.
     */
    val title: String
    val subtitle: String
    val cta: String
    val action: () -> Unit
    when {
        station == null -> {
            // يفتح ورقة اليوم — وهي التي تعرض المحطّات وتطلب الموقع في
            // موضعه الصحيح. وكان يفتح شاشة المواقيت مباشرةً، فيقفز
            // المستخدم إلى إعدادٍ قبل أن يرى ما سيصلحه ذلك الإعداد.
            title    = "أذكار يومك"
            subtitle = "من الاستيقاظ إلى النوم — افتح ورقتك"
            cta      = "افتح"
            action   = onDayPage
        }
        station.route == null -> {
            title    = station.title
            subtitle = station.description.localizedDigits(ar)
            cta      = "التفصيل"
            action   = onDayPage
        }
        else -> {
            title    = station.title
            subtitle = station.description.localizedDigits(ar)
            cta      = "ابدأ"
            action   = onStart
        }
    }

    Row(
        Modifier.fillMaxWidth().padding(top = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = RafiqType.titleXL, color = rc.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = RafiqType.bodyS,
                color = rc.inkMed,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(15.dp))
        Box(
            Modifier
                .clip(CircleShape)
                .background(rc.emeraldFill)
                .clickable(onClick = action)
                .defaultMinSize(minWidth = 96.dp, minHeight = 52.dp)
                .padding(horizontal = 26.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(cta, style = RafiqType.titleM, color = rc.onEmeraldFill, maxLines = 1)
        }
    }
}

/* ── الطبقة ٣: صفُّ اليوم ───────────────────────────────────────

   بديلٌ عن قائمةٍ رأسية بتسعة صفوف. الأسماء القصيرة في سطرٍ واحد،
   وموضعُ الاسم الداكن يقول أين أنت. يُمرَّر أفقياً حين لا تتّسع
   التسعة — ولا يُقصّ منها شيء.
──────────────────────────────────────────────────────────────── */

@Composable
private fun DayRow(
    stations: List<DayCompanionViewModel.StationUi>,
    nowId:      String?,
    ar:         Boolean,
    onOpen:     () -> Unit,
) {
    val rc = LocalRafiqColors.current
    var expanded by remember { mutableStateOf(false) }
    val nowIdx = stations.indexOfFirst { it.id == nowId }

    // بلا موقعٍ لا مواقيت، وبلا مواقيت لا محطّات — فكان الصفّ يختفي صامتاً
    // وتفقد الشاشة عمودها الفقري. يظهر الآن بأسماء اليوم مطفأةً وسطرٍ
    // يقول للمستخدم ما ينقصه، بدل فراغٍ لا يفسّر نفسه.
    val waiting = stations.isEmpty()
    val names = if (waiting) FALLBACK_DAY else stations.map { it.short }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 22.dp)
            .animateContentSize()
            .clickable(enabled = waiting, onClick = onOpen),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom,
        ) {
            names.forEachIndexed { i, short ->
                val isNow  = i == nowIdx
                val isPast = nowIdx >= 0 && i < nowIdx
                // اللون يتحرّك حين تتقدّم المحطّة الحاضرة — فلا تقفز الحالة
                val nameColor by animateColorAsState(
                    when {
                        isNow  -> rc.ink
                        isPast -> rc.inkMed
                        else   -> rc.inkLight
                    },
                    progressSpec(500), label = "stationColor",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // IntrinsicSize.Max يجعل الخطّ أدناه بعرض الاسم بالضبط
                    modifier = Modifier.width(IntrinsicSize.Max),
                ) {
                    Text(
                        short,
                        fontSize   = 14.sp,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        color      = nameColor,
                        maxLines   = 1,
                    )
                    Spacer(Modifier.height(8.dp))
                    NowUnderline(active = isNow)
                }
            }
        }

        Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(1.dp).background(rc.divider))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable(enabled = !waiting) { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                when {
                    // كانت «حدّد موقعك لتظهر مواقيتك ومحطّاتك» — نفس
                    // الجملة التي فوقها بثلاثة عناصر، تُقرأ مرّتين.
                    waiting  -> "افتح ورقة يومك"
                    expanded -> "اضغط للإغلاق"
                    else     -> "اضغط لتفصيل يومك"
                },
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
            val turn by animateFloatAsState(
                if (expanded) 90f else 0f, tapSpec(), label = "chevron",
            )
            Box(Modifier.rotate(turn)) {
                RafiqIcon(RIcon.ChevronLeft, size = 18.dp, tint = rc.inkMed)
            }
        }

        if (expanded) {
            stations.forEachIndexed { i, st ->
                val isNow  = i == nowIdx
                val isPast = nowIdx >= 0 && i < nowIdx
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(onClick = onOpen),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(if (isNow) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isNow  -> rc.emeraldFill
                                    isPast -> rc.emeraldFill.copy(alpha = .85f)
                                    else   -> rc.inkLight.copy(alpha = .45f)
                                },
                            ),
                    )
                    Spacer(Modifier.width(13.dp))
                    Text(
                        st.title,
                        style = RafiqType.titleM,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNow || isPast) rc.ink else rc.inkMed,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        st.timeLabel.localizedDigits(ar),
                        style = RafiqType.bodyS,
                        color = rc.inkMed,
                    )
                }
                if (i != stations.lastIndex) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
                }
            }
        }
    }
}

/**
 * خطُّ المحطّة الحاضرة.
 *
 * ينبض نبضاً خافتاً (0.55↔1.0 في 1.4 ثانية) — وهو الحركةُ الوحيدة في
 * الشاشة، فتقع العين على «الآن» بلا أن يُقال لها ذلك. والنبض على
 * الشفافية لا على الحجم: تغيّرُ الحجم يزحزح ما حوله ويشتّت القراءة.
 *
 * ويحترم LocalReducedMotion فيثبت على الوضوح الكامل.
 */
@Composable
private fun NowUnderline(active: Boolean) {
    val rc = LocalRafiqColors.current
    val reduced = LocalReducedMotion.current
    val alpha = if (!active || reduced) 1f else {
        val t = rememberInfiniteTransition(label = "nowPulse")
        t.animateFloat(
            initialValue = 0.55f,
            targetValue  = 1f,
            animationSpec = infiniteRepeatable(
                tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse,
            ),
            label = "nowPulseAlpha",
        ).value
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                if (active) rc.emeraldFill.copy(alpha = alpha) else Color.Transparent,
            ),
    )
}

/** أسماءُ اليوم حين لا مواقيت بعد — تُعرض مطفأةً كلُّها فيرى المستخدم شكل
 *  يومه قبل أن يحدّد موقعه. مطابقة لـ`short` في محطّات DayCompanion. */
private val FALLBACK_DAY = listOf(
    "الاستيقاظ", "الفجر", "الضحى", "الظهر", "العصر", "المغرب", "العشاء", "النوم",
)

/* ── الطبقة ٤: الأبواب الثلاثة في صفّ ───────────────────────────

   ثلاثةٌ لا سِتّ: المصحف والأدعية والأذكار وأوراقي كلّها في الشريط
   السفلي، فوضعُها هنا يهدر أثمن مساحة على أبوابٍ على بُعد ضغطة.
   وصفٌّ واحد (٥٠dp) بدل صفّين كاملين (١٥٨dp) — والصفُّ يُقرأ بنظرةٍ
   واحدة كمجموعة، والصفّان يُقرآن سطراً سطراً كقائمة.
──────────────────────────────────────────────────────────────── */

@Composable
private fun DoorsRow(
    onTasbeeh: () -> Unit,
    onQibla:   () -> Unit,
    onTimes:   () -> Unit,
) {
    Row(
        // الحشوة السفلية ضرورية: Scaffold يفصل ارتفاع الشريط عن المحتوى
        // لكنه لا يترك متنفَّساً بينهما، فبدت الأبواب ملتصقةً بالتنقّل.
        Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DoorChip("المسبحة", Modifier.weight(1f), onTasbeeh) { IcoMisbaha(20.dp, it) }
        DoorChip("القبلة",  Modifier.weight(1f), onQibla)   { IcoCompass(20.dp, it) }
        DoorChip("المواقيت", Modifier.weight(1f), onTimes)  { IcoMosque(20.dp, it) }
    }
}

@Composable
private fun DoorChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val rc = LocalRafiqColors.current
    Row(
        modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, rc.divider, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon(rc.inkMed)
        Spacer(Modifier.width(7.dp))
        Text(label, style = RafiqType.titleM, color = rc.ink, maxLines = 1)
    }
}
