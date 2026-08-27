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
import androidx.compose.foundation.verticalScroll
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
import app.rafiqaldhikr.ui.theme.RafiqPalette
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxWithConstraints

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
    /*  التمرير أوّلاً، والتنفّس ثانياً — لا العكس.
     *
     *  كانت الكلمة تأخذ Modifier.weight(1f) لتتوسّط ما بقي من الارتفاع.
     *  وweight لا يعمل داخل عمودٍ قابل للتمرير (القيد الرأسي هناك لا
     *  نهائي فلا «باقي» يُقسَّم)، فحُذف التمرير ليعمل التوسيط.
     *
     *  والأثر لا يظهر ما دامت قائمة اليوم مطويّة — فحين تُفتح تسعُ
     *  محطّات يفيض المحتوى ولا شيء يتحرّك. شاشةٌ عالقة.
     *
     *  فالتمرير يعود، والتوسيط يُستبدل بارتفاعٍ أدنى للكلمة: تتنفّس
     *  حين يتّسع المكان، ويُمرَّر ما زاد حين لا يتّسع.
     */
    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        HubTopBar(onSettings = { navController.navigate(RafiqRoute.Settings.route) })

        Greeting(hijri = home.hijriDate, ar = ar)

        Box(
            // 210 كانت تدفع الأبواب تحت الشريط السفلي فتُقصّ. 132 تكفي
            // لتتنفّس الكلمة، وتُبقي الشاشة كاملةً بلا تمرير — والتمرير
            // يبقى متاحاً حين تُفتح قائمة اليوم.
            Modifier.fillMaxWidth().heightIn(min = 132.dp),
            contentAlignment = Alignment.Center,
        ) {
            hub.wisdom?.let { WordOfDay(it) }
        }

        // خطٌّ يفصل منطقتين متناقضتين: كلمةٌ تتأمّلها فوقه، وعملٌ تفعله
        // تحته. كانتا ملتصقتين بصفر مسافة، فيلتقي السند بسطر الوقت.
        Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        Spacer(Modifier.height(20.dp))

        MeeqatCard(
            station   = day.nowStation,
            nextName  = home.nextPrayerName,
            nextTime  = home.nextPrayerTime,
            ar        = ar,
            onStart   = { day.nowStation?.route?.let { navController.navigate(it) } },
            onDayPage = { navController.navigate(RafiqRoute.DayPage.route) },
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
        Modifier.fillMaxWidth().padding(top = 12.dp),
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
        Modifier.fillMaxWidth().padding(top = 20.dp),
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

/* ══════════════════════════════════════════════════════════════
   بطاقةُ الميقات — الطبقتان ١ و٢ صارتا واحدة

   كانتا سطراً رفيعاً («الظهر بعد ١٢ دقيقة») فوق زوجٍ كبير («صلاة
   الضحى» + زرّ). ومصدراهما لا يعرف أحدهما الآخر: السطرُ من
   HomeViewModel والزوجُ من DayCompanionViewModel — فيظهر اسمُ صلاةٍ
   فوق اسمِ محطّةٍ أخرى بلا رابط، ويقرأهما القارئ خبرين متناقضين.

   وهما مرتبطان: نافذةُ الضحى تنتهي عند الظهر، أي أنّ `endMillis` هو
   الظهرُ نفسه. الرقمُ واحدٌ والمعنى مختلف — «الظهر بعد ١٢ دقيقة» خبرٌ
   عن التطبيق، و«نافذةُ الضحى · بقي ١٢ دقيقة» خبرٌ عن صاحبه.

   ── الخيط ──────────────────────────────────────────────────────
   ليس شريطَ تقدّم: شريطُ التقدّم يمتلئ بما فعلتَ ويحاسبك عليه، وهذا
   يمتلئ بما مضى من الوقت — لو نمتَ اليومَ كلَّه لتحرّك كما هو. ولهذا
   حبّةٌ تمشي على خيطٍ لا شريطٌ يُكافئ: مِزْولةٌ لا رصيد.

   ولونُه من سلَّم الضوء في اللوحة — goldLight للفجر والضحى،
   lightDusk للعصر والمغرب، lightNight للعشاء والنوم — لأنّ أسماء
   الصلوات كلَّها أسماءُ حالات ضوء. فيتغيّر لونُ البطاقة مع النهار
   بلا كلمةٍ واحدة.

   وهو حدُّ البطاقة نفسُه لا سطرٌ زائد: يفصل رأسَها عن جسمها، فلا
   يكلّف ارتفاعاً وهو أوّلُ ما تقع عليه العين.
══════════════════════════════════════════════════════════════ */

/** أطرافُ سلَّم الضوء داكنةٌ فلا تُقرأ على بطاقةٍ خضراء داكنة: goldLight
 *  نبرتُه هناك 2.63 وlightNight 1.25. تُرفع نحو [RafiqPalette.onHero]
 *  حتى تتجاوز عتبةَ الرسم 3.0 — فتصير 4.46 و3.86 و3.47. */
private fun lightOf(stationId: String, rc: RafiqPalette): Color {
    val base = when (stationId) {
        "fajr_morning", "duha", "dhuhr", "friday_kahf" -> rc.goldLight
        "asr_evening", "maghrib"                       -> rc.lightDusk
        else                                           -> rc.lightNight   // العشاء والنوم والاستيقاظ
    }
    // في الليل تكون أطرافُ السلّم فاتحةً أصلاً، فيكفيها رفعٌ يسير
    val lift = if (rc.bg.luminance() > 0.5f) 0.55f else 0.18f
    return lerp(base, rc.onHero, lift)
}

/** «بقي ١٢ دقيقة» من فارقٍ بالمللي ثانية — بتصريف العدد العربي. */
private fun humanRemaining(millis: Long): String? {
    if (millis <= 0) return null
    val total = millis / 60_000L
    return humanCountdown("%02d:%02d:00".format(total / 60, total % 60))
}

@Composable
private fun MeeqatCard(
    station:   DayCompanionViewModel.StationUi?,
    nextName:  String,
    nextTime:  String,
    ar:        Boolean,
    onStart:   () -> Unit,
    onDayPage: () -> Unit,
) {
    val rc = LocalRafiqColors.current

    /*  ثلاثُ حالاتٍ ولا تعطيل — كما كان في NowAction:
     *    ١) لا موقع ← لا مواقيت ← لا محطّات: البطاقة تفتح ورقة اليوم،
     *       وهي التي تطلب الموقع في موضعه الصحيح.
     *    ٢) «الاستيقاظ» و«الضحى» route = null: تفصيلُهما في ورقة اليوم.
     *    ٣) الباقي: «ابدأ» إلى شاشة أذكاره. */
    val title:  String
    val desc:   String
    val source: String?
    val cta:    String
    val action: () -> Unit
    when {
        station == null -> {
            title = "أذكار يومك"
            desc = "من الاستيقاظ إلى النوم — افتح ورقتك"
            source = null; cta = "افتح"; action = onDayPage
        }
        station.route == null -> {
            title = station.title; desc = station.description.localizedDigits(ar)
            source = station.source; cta = "التفصيل"; action = onDayPage
        }
        else -> {
            title = station.title; desc = station.description.localizedDigits(ar)
            source = station.source; cta = "ابدأ"; action = onStart
        }
    }

    // الوقتُ يتقدّم بنبضة الدقيقة في DayCompanionViewModel: كلُّ نبضةٍ
    // تُعيد بناء الحالة فتُعاد قراءةُ الساعة هنا. ودقيقةٌ هي دقّةُ ما
    // يُعرض أصلاً («بقي ١٢ دقيقة»)، فلا حاجة إلى مؤقّتٍ ثانٍ.
    val now = System.currentTimeMillis()
    val span = station?.let { it.endMillis - it.startMillis } ?: 0L
    val progress = if (span > 0L)
        ((now - station!!.startMillis).toFloat() / span).coerceIn(0f, 1f) else null
    val remaining = station?.let { humanRemaining(it.endMillis - now) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(MeeqatShape)
            .background(
                Brush.linearGradient(
                    // أفتحُ طرفٍ أعلى: هناك يقع الخيط، وهناك أصعبُ تباين
                    listOf(rc.heroEnd, rc.heroMid, rc.heroStart),
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end   = Offset(0f, Float.POSITIVE_INFINITY),
                )
            )
            .border(1.dp, rc.onHero.copy(alpha = 0.11f), MeeqatShape),
    ) {
        /* الرأس: النافذةُ وما بقي منها يميناً، وما بعدها يساراً */
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (station != null) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(lightOf(station.id, rc)),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (station == null) "ورقةُ يومك"
                    else "نافذةُ ${station.short}" + (remaining?.let { " · بقي ${it.localizedDigits(ar)}" } ?: ""),
                    style = RafiqType.bodyS,
                    color = rc.onHero,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (nextName.isNotEmpty() && nextTime.isNotEmpty() && nextTime != "—") {
                Spacer(Modifier.width(10.dp))
                Text(
                    "ثمّ $nextName ${nextTime.localizedDigits(ar)}",
                    style = RafiqType.caption,
                    color = rc.onHero.copy(alpha = 0.72f),
                    maxLines = 1,
                )
            }
        }

        /* الخيط — وهو الفاصلُ بين الرأس والجسم */
        MeeqatThread(progress, station?.let { lightOf(it.id, rc) } ?: rc.onHero, rc)

        /* الجسم */
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
            Text(title, style = RafiqType.heroCard, color = rc.onHero)
            Spacer(Modifier.height(5.dp))
            Text(
                desc,
                style = RafiqType.bodyS,
                color = rc.onHero.copy(alpha = 0.86f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (source != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(rc.onHero.copy(alpha = 0.08f))
                        .border(1.dp, rc.onHero.copy(alpha = 0.13f), RoundedCornerShape(10.dp))
                        .padding(start = 9.dp, end = 11.dp, top = 7.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(15.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(station?.let { lightOf(it.id, rc) } ?: rc.onHero),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(source, style = RafiqType.caption, color = rc.onHero)
                }
            }
        }

        /* الفعل: «ابدأ» وزرُّ «لماذا هذا الآن؟» */
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
                    .clip(CtaShape)
                    .background(rc.card)
                    .clickable(onClick = action)
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(cta, style = RafiqType.titleM, color = rc.emerald, maxLines = 1)
                RafiqIcon(RIcon.ChevronLeft, 19.dp, rc.emerald)
            }
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(rc.onHero.copy(alpha = 0.07f))
                    .border(1.dp, rc.onHero.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onDayPage),
                contentAlignment = Alignment.Center,
            ) {
                RafiqIcon(RIcon.Info, 21.dp, rc.onHero)
            }
        }
    }
}

/** الزاويةُ المميّزة في bottomStart — نفسُ موضعِ زاوية شعار الشريط
 *  العلوي `RoundedCornerShape(16, 16, 16, 6)`، فتُقرأ توقيعاً واحداً. */
private val MeeqatShape = RoundedCornerShape(
    topStart = 26.dp, topEnd = 26.dp, bottomEnd = 26.dp, bottomStart = 44.dp,
)
private val CtaShape = RoundedCornerShape(
    topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 26.dp,
)

@Composable
private fun MeeqatThread(progress: Float?, tone: Color, rc: RafiqPalette) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(CircleShape)
                .background(rc.onHero.copy(alpha = 0.18f)),
        )
        if (progress != null) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceAtLeast(0.02f))
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(tone),
            )
            // offset يحترم اتّجاه التخطيط، فتمشي الحبّةُ يميناً←يساراً
            Box(
                Modifier
                    .offset(x = (maxWidth - BEAD) * progress)
                    .size(BEAD)
                    .clip(CircleShape)
                    .background(rc.card),
            )
        }
    }
}

private val BEAD = 13.dp

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
            .padding(top = 18.dp)
            .animateContentSize()
            // كان enabled = waiting: أي لا يُضغط إلّا حين لا موقع. فمن
            // ضبط موقعه لم يبقَ له بابٌ إلى ورقة يومه — وهي أغنى شاشة
            // في التطبيق. الصفُّ كلُّه بابٌ الآن.
            .clickable(onClick = onOpen),
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
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // فعلان مختلفان لا فعلٌ واحد: النصّ يفتح ورقة اليوم كاملةً،
            // والشيفرون يطوي القائمة في مكانها. كانا مدموجين في ضغطةٍ
            // واحدة تفعل الطيّ وحده — فلم يبقَ للورقة بابٌ من الرئيسية.
            Text(
                "افتح ورقة يومك",
                style = RafiqType.bodyS,
                color = rc.emerald,
                modifier = Modifier
                    .clickable(onClick = onOpen)
                    .padding(vertical = 10.dp),
            )
            val turn by animateFloatAsState(
                if (expanded) 90f else 0f, tapSpec(), label = "chevron",
            )
            Box(
                Modifier
                    .clickable(enabled = !waiting) { expanded = !expanded }
                    .padding(10.dp)
                    .rotate(turn),
            ) {
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
        Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp),
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
