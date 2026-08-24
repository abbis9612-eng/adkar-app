package app.rafiqaldhikr.ui.screens.hub

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 104.dp),
    ) {
        HubTopBar(onSettings = { navController.navigate(RafiqRoute.Settings.route) })

        Greeting(hijri = home.hijriDate, ar = ar)

        hub.wisdom?.let { WordOfDay(it) }

        Spacer(Modifier.height(30.dp))

        NextPrayerLine(
            name      = home.nextPrayerName.ifEmpty { "—" },
            countdown = home.countdown.ifEmpty { "—" },
            ar        = ar,
        )

        NowAction(
            station = day.nowStation,
            onStart = { day.nowStation?.route?.let { navController.navigate(it) } },
        )

        DayRow(
            stations = day.stations,
            nowId    = day.nowStation?.id,
            ar       = ar,
            onOpen   = { navController.navigate(RafiqRoute.DayPage.route) },
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
                RafiqIcon(RIcon.Sparkles, size = 24.dp, tint = rc.onEmeraldFill)
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
    Row(verticalAlignment = Alignment.Bottom) {
        Text(name, style = RafiqType.titleM, color = rc.ink)
        Spacer(Modifier.width(9.dp))
        Text("بعد ${countdown.localizedDigits(ar)}", style = RafiqType.bodyS, color = rc.inkMed)
    }
}

/* ── الطبقة ٢: زوجٌ كبير — الفعلُ ملتصقٌ بما يفعله ─────────────── */

@Composable
private fun NowAction(
    station: DayCompanionViewModel.StationUi?,
    onStart: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(top = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(station?.title ?: "يومك يبدأ", style = RafiqType.titleXL, color = rc.ink)
            Spacer(Modifier.height(4.dp))
            Text(
                station?.description ?: "افتح أوّل محطّة حين يحين وقتها",
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
        }
        Spacer(Modifier.width(15.dp))
        Box(
            Modifier
                .clip(CircleShape)
                .background(rc.emeraldFill)
                .clickable(enabled = station?.route != null, onClick = onStart)
                .defaultMinSize(minWidth = 96.dp, minHeight = 52.dp)
                .padding(horizontal = 30.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("ابدأ", style = RafiqType.titleM, color = rc.onEmeraldFill)
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
    nowId:    String?,
    ar:       Boolean,
    onOpen:   () -> Unit,
) {
    if (stations.isEmpty()) return
    val rc = LocalRafiqColors.current
    var expanded by remember { mutableStateOf(false) }
    val nowIdx = stations.indexOfFirst { it.id == nowId }

    Column(Modifier.fillMaxWidth().padding(top = 20.dp).animateContentSize()) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom,
        ) {
            stations.forEachIndexed { i, st ->
                val isNow  = i == nowIdx
                val isPast = nowIdx >= 0 && i < nowIdx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = if (i == stations.lastIndex) 0.dp else 10.dp),
                ) {
                    Text(
                        st.short,
                        fontSize   = 14.sp,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        color      = when {
                            isNow  -> rc.ink
                            isPast -> rc.inkMed
                            else   -> rc.inkLight
                        },
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isNow) rc.emeraldFill else Color.Transparent),
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 11.dp)
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (expanded) "اضغط للإغلاق" else "اضغط لتفصيل يومك",
                style = RafiqType.bodyS,
                color = rc.inkMed,
            )
            Text(if (expanded) "▴" else "▾", style = RafiqType.bodyS, color = rc.inkMed)
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
        Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DoorChip(RIcon.Sparkles, "المسبحة", Modifier.weight(1f), onTasbeeh)
        DoorChip(RIcon.Compass,  "القبلة",  Modifier.weight(1f), onQibla)
        DoorChip(RIcon.Clock,    "المواقيت", Modifier.weight(1f), onTimes)
    }
}

@Composable
private fun DoorChip(
    icon: RIcon,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
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
        RafiqIcon(icon, size = 18.dp, tint = rc.inkLight)
        Spacer(Modifier.width(7.dp))
        Text(label, style = RafiqType.titleM, color = rc.ink, maxLines = 1)
    }
}
