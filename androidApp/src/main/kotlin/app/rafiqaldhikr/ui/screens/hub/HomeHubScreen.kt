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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
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
import app.rafiqaldhikr.ui.components.LocationRequestViewModel
import app.rafiqaldhikr.ui.components.LocationPermissionEffect
import app.rafiqaldhikr.ui.sky.sunPosition
import app.rafiqaldhikr.ui.sky.skyInk
import app.rafiqaldhikr.ui.sky.skyColors
import app.rafiqaldhikr.ui.sky.moonPhase
import app.rafiqaldhikr.ui.sky.SkyCanvas
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.foundation.layout.Box

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
    val locVm: LocationRequestViewModel = koinViewModel()

    /*  الرئيسية هي التي تطلب الموقع — ولم يكن أحدٌ يطلبه.
     *
     *  LocationPermissionEffect معرَّفٌ في المشروع منذ البداية ولا
     *  يُستدعى من أيّ شاشة إطلاقاً (تحقّقٌ بالبحث: صفرُ مواضع). فمن
     *  ثبّت التطبيق حديثاً لا يُسأل عن موقعه أبداً، ولا مواقيت، ولا
     *  محطّات — وبطاقةُ الميقات تظهر فارغةً بلا نافذةٍ ولا خيط، ولا
     *  شيء يخبره لماذا ولا كيف يصلحها.
     *
     *  يُطلب مرّةً واحدة حين لا يكون هناك موقع. ومن رفض يبقى له مدخلُ
     *  البطاقة أدناه وشاشةُ ورقة اليوم بمنتقي المدن.
     */
    LocationPermissionEffect(
        hasLocation = !day.needsLocation,
        isLoading   = day.isLoading,
        onLocationFetched = { lat, lng -> locVm.save(lat, lng) },
    )

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
    /*  السماءُ فوق والورقةُ تحت — والأفقُ حيث يلتقيان.
     *
     *  السماءُ ليست خلفيةً مرسومة: موضعُ الشمس يُحسب من إحداثيات صاحبها
     *  ودقيقته، وهو الحسابُ نفسه الذي تُشتقّ منه المواقيت. فرسمُ مسجدٍ
     *  ثابتٍ هو نفسُه في السويد وفي عُمان وفي الفجر وفي العشاء — وهذه
     *  تختلف بالمدينة وباليوم وبالدقيقة، ولا تتكرّر مرّتين.
     *
     *  والورقةُ ترتفع من الأفق فتغطّي ٨٢dp من السماء: فلا يُدفع من
     *  الارتفاع إلا ٢٣٦، ويُسترجَع منها الشريطُ العلويّ والتحيّةُ وسطرُ
     *  النافذة — إذ صاروا فوقها.
     */
    val now = System.currentTimeMillis()
    val sun = remember(now / 60_000L, home.lat, home.lng) {
        sunPosition(now, home.lat, home.lng)
    }
    val moon = remember(now / 3_600_000L) { moonPhase(now) }
    val sky = remember(sun.altitude) { skyColors(sun.altitude.toFloat()) }
    val skyInk = remember(sky) { skyInk(sky) }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        SkyCanvas(
            altitude = sun.altitude.toFloat(),
            azimuth  = sun.azimuth.toFloat(),
            moon     = moon,
            rc       = rc,
            modifier = Modifier.fillMaxWidth().height(SKY_H),
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            /*  ترتيبُ السماء: كلامٌ أعلى · صورةٌ وسطى · حبّةٌ على الأفق.
             *
             *  كان الكلامُ كلُّه مكدَّساً في الأعلى (شريطٌ فتحيّةٌ فحبّة)،
             *  فبقي أسفلُ السماء فراغاً ميّتاً، ووقع القرصُ خلف «نهارٌ
             *  طيّب». الآن للصورة نطاقٌ خالصٌ بينهما، والحبّةُ — وهي
             *  أقربُ ما يُقرأ إلى الفعل — تجلس على حافّة الورقة مباشرةً.
             */
            CompositionLocalProvider(LocalContentColor provides skyInk) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(SKY_TEXT_H)
                        .padding(horizontal = 20.dp),
                ) {
                    SkyTopBar(
                        hijri = home.hijriDate.localizedDigits(ar),
                        ink   = skyInk,
                        onSettings = { navController.navigate(RafiqRoute.Settings.route) },
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (sun.altitude > 8) stringResource(R.string.greet_day) else if (sun.altitude > -1) stringResource(R.string.greet_blessed) else stringResource(R.string.greet_evening),
                        style = RafiqType.hero,
                        color = skyInk,
                    )
                    Spacer(Modifier.weight(1f))          // نطاقُ الصورة
                    WindowPill(day.nowStation, day.needsLocation, ar, skyInk)
                    Spacer(Modifier.height(14.dp))
                }
            }

            /* الورقة — ترتفع حيث ينتهي كلامُ السماء، فتتبع مقاسَ الخطّ
               بدل ارتفاعٍ مثبَّتٍ يُقصّ حين يكبّره صاحبُه. */
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to lerp(rc.bg, sky.second, 0.14f),
                            0.46f to rc.bg,
                        ),
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                Box(
                    Modifier
                        .padding(top = 9.dp, bottom = 2.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(34.dp).height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(rc.divider),
                )

                MeeqatCard(
                    station   = day.nowStation,
                    needsLoc  = day.needsLocation,
                    nextName  = home.nextPrayerName,
                    nextTime  = home.nextPrayerTime,
                    ar        = ar,
                    onStart   = { day.nowStation?.route?.let { navController.navigate(it) } },
                    onDayPage = { navController.navigate(RafiqRoute.DayPage.route) },
                )

                DayRow(
                    stations   = day.stations,
                    nowId      = day.nowStation?.id,
                    doneIds    = day.completedIds,
                    ar         = ar,
                    onOpen     = { navController.navigate(RafiqRoute.DayPage.route) },
                )

                hub.lastRead?.let { pos ->
                    ContinueReading(pos.surah, pos.page, ar) {
                        navController.navigate(RafiqRoute.Mushaf.atPage(pos.page))
                    }
                }

                hub.wisdom?.let { WordOfDay(it) }

                DoorsRow(
                    onTasbeeh = { navController.navigate(RafiqRoute.Tasbeeh.route) },
                    onQibla   = { navController.navigate(RafiqRoute.Qibla.route) },
                    onTimes   = { navController.navigate(RafiqRoute.PrayerTimes.route) },
                )
            }
        }
    }
}

/** ارتفاعُ السماء، وارتفاعُ كلامها. والورقةُ تبدأ حيث ينتهي الكلام. */
private val SKY_H = 360.dp
private val SKY_TEXT_H = 288.dp

/* ── الشريطُ العلويُّ فوق السماء ─────────────────────────────────

   زجاجُه يشتقّ من الحبر: كان أبيضَ دائماً ولو انقلب الحبرُ داكناً في
   الظهيرة، فيصير حبرٌ داكنٌ فوق زجاجٍ أبيضَ فوق سماءٍ فاتحة — ثلاثُ
   طبقاتٍ تتنازع.
──────────────────────────────────────────────────────────────── */

@Composable
private fun SkyTopBar(hijri: String, ink: Color, onSettings: () -> Unit) {
    val glassBg = ink.copy(alpha = if (ink.luminance() > 0.5f) 0.15f else 0.10f)
    val glassBd = ink.copy(alpha = if (ink.luminance() > 0.5f) 0.28f else 0.20f)
    Row(
        Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 40.dp, height = 45.dp)
                    .clip(RoundedCornerShape(5.dp, 5.dp, 17.dp, 5.dp))
                    .background(glassBg)
                    .border(1.dp, glassBd, RoundedCornerShape(5.dp, 5.dp, 17.dp, 5.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("ر", style = RafiqType.hero, color = ink) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(stringResource(R.string.app_name), style = RafiqType.titleM, color = ink)
                Text(hijri, style = RafiqType.bodyS, color = ink.copy(alpha = 0.78f), maxLines = 1)
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(glassBg)
                .border(1.dp, glassBd, CircleShape)
                .clickable(onClick = onSettings),
            contentAlignment = Alignment.Center,
        ) { RafiqIcon(RIcon.Settings, 19.dp, ink) }
    }
}

/** «نافذةُ الضحى · بقي ١٢ دقيقة» — على السماء، فوق الأفق. */
@Composable
private fun WindowPill(
    station: DayCompanionViewModel.StationUi?,
    needsLoc: Boolean,
    ar: Boolean,
    ink: Color,
) {
    val glassBg = ink.copy(alpha = if (ink.luminance() > 0.5f) 0.15f else 0.10f)
    val glassBd = ink.copy(alpha = if (ink.luminance() > 0.5f) 0.28f else 0.20f)
    val label = when {
        needsLoc -> stringResource(R.string.hub_times_unset)
        station == null -> stringResource(R.string.waraqa_title)
        else -> {
            val left = humanRemaining(station.endMillis - System.currentTimeMillis())
            stringResource(R.string.hub_station_window, stringResource(station.short)) +
                (left?.let { stringResource(R.string.hub_remaining, it.localizedDigits(ar)) } ?: "")
        }
    }
    Row(
        Modifier
            .clip(CircleShape)
            .background(glassBg)
            .border(1.dp, glassBd, CircleShape)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(ink.copy(alpha = 0.8f)))
        Spacer(Modifier.width(8.dp))
        Text(label, style = RafiqType.bodyS, color = ink, maxLines = 1)
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
                Text(stringResource(R.string.app_name), style = RafiqType.titleL, color = rc.emerald)
                Text(stringResource(R.string.hub_companion), style = RafiqType.bodyS, color = rc.inkMed)
            }
        }
        RafiqIconButton(onClick = onSettings, label = stringResource(R.string.settings)) {
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
            Text(stringResource(R.string.greet_salam), style = RafiqType.bodyS, color = rc.inkMed)
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
@Composable
private fun greetingText(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (h) {
        in 4..10  -> stringResource(R.string.greet_morning)
        in 11..15 -> stringResource(R.string.greet_day)
        in 16..19 -> stringResource(R.string.greet_calm_evening)
        else      -> stringResource(R.string.greet_night)
    }
}

/* ── تابِع القراءة ──────────────────────────────────────────────

   `QuranLastRead` جدولٌ في القاعدة وطرقُه في المستودع منذ البداية، وله
   صفرُ مستدعين: لا كاتبَ ولا قارئ. فمن قرأ صفحةَ ٥٧٧ ثمّ خرج، لم يكن
   له إلّا أن يبحث عنها ثانيةً في ست مئةٍ وأربع.

   والبطاقةُ لا تظهر إلّا لمن فتح المصحفَ فعلاً — لا موضعَ محفوظٌ فلا
   بطاقة، ولا تشغل مكاناً في شاشة من لم يقرأ بعد.
──────────────────────────────────────────────────────────────── */

@Composable
private fun ContinueReading(surah: Int, page: Int, ar: Boolean, onOpen: () -> Unit) {
    val rc  = LocalRafiqColors.current
    val ctx = LocalContext.current
    val name = remember(surah) { app.rafiqaldhikr.ui.mushaf.SurahNames.of(ctx, surah) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.card)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RafiqIcon(RIcon.Book, 20.dp, rc.emerald)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.continue_reading), style = RafiqType.bodyS, color = rc.inkMed)
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.continue_reading_at, name, page.toString())
                    .localizedDigits(ar),
                style = RafiqType.body,
                color = rc.ink,
            )
        }
        // السهمُ يتبع اتّجاهَ الكتابة — `autoMirrored` في المورد نفسِه.
        RafiqIcon(RIcon.ChevronLeft, 16.dp, rc.inkLight)
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

/** «بقي ١٢ دقيقة» من فارقٍ بالمللي ثانية. */
@Composable
private fun humanRemaining(millis: Long): String? {
    if (millis <= 0) return null
    val total = millis / 60_000L
    // Locale.ROOT: بلا تحديدٍ يُنتج `format` أرقاماً عربيةً على جهازٍ
    // محلّيتُه ar، فلا يعود `toIntOrNull` يقرؤها.
    return humanCountdown(
        String.format(java.util.Locale.ROOT, "%02d:%02d:00", total / 60, total % 60)
    )
}

@Composable
private fun MeeqatCard(
    station:   DayCompanionViewModel.StationUi?,
    needsLoc:  Boolean,
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
        // بلا إحداثيات لا مواقيت، وبلا مواقيت لا محطّات — فكانت البطاقة
        // تعرض «أذكار يومك» عامّةً بلا نافذةٍ ولا خيطٍ ولا إسناد، ولا
        // تقول لماذا هي كذلك. الآن تقول السبب وتحمل علاجه.
        needsLoc -> {
            title = stringResource(R.string.hub_set_city)
            desc = "محطّاتُ يومك موقوتةٌ بالصلاة — من الاستيقاظ إلى النوم. " +
                "حدِّدها مرّةً واحدة ويُحسب الباقي."
            source = null; cta = stringResource(R.string.hub_set_location); action = onDayPage
        }
        station == null -> {
            title = stringResource(R.string.hub_day_adhkar)
            desc = stringResource(R.string.hub_day_sub)
            source = null; cta = stringResource(R.string.action_open); action = onDayPage
        }
        station.route == null -> {
            title = stringResource(station.title); desc = stringResource(station.description).localizedDigits(ar)
            source = station.source; cta = stringResource(R.string.action_detail); action = onDayPage
        }
        else -> {
            title = stringResource(station.title); desc = stringResource(station.description).localizedDigits(ar)
            source = station.source; cta = stringResource(R.string.action_start); action = onStart
        }
    }


    /*  كانت البطاقةُ سطحاً أخضرَ داكناً لأنّها كانت مركزَ ثقل الشاشة.
     *  والسماءُ صارت المركز، فلو بقيت داكنةً لتنازعتا. فهي الآن على
     *  الورق نفسه، والفعلُ وحده ملوّن. */
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        /*  رأسُ البطاقة وخيطُها انتقلا إلى السماء: النافذةُ وما بقي
         *  منها في الحبّة فوق الأفق، وموضعُ الوقت في موضع الشمس نفسِه.
         *  وإبقاؤهما هنا تكرارٌ لما تقوله السماءُ بلا كلمة. */
        /* الجسم */
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp)) {
            Text(title, style = RafiqType.heroCard, color = rc.ink)
            Spacer(Modifier.height(5.dp))
            Text(
                desc,
                style = RafiqType.bodyS,
                color = rc.inkMed,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        /* الفعل: «ابدأ» وزرُّ «لماذا هذا الآن؟» */
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            if (source != null) {
                // كانت سطراً وحدها فوق الزرّين، فتُقرأ لصاقةً معلَّقة.
                // وهي في صفّ الفعل تُقرأ ما تُقرأ به: سنداً لِما ستفعل.
                Row(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, rc.cardBorder, RoundedCornerShape(10.dp))
                        .padding(start = 9.dp, end = 11.dp, top = 9.dp, bottom = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(3.dp).height(15.dp).clip(RoundedCornerShape(2.dp)).background(rc.gold))
                    Spacer(Modifier.width(8.dp))
                    Text(source, style = RafiqType.caption, color = rc.gold, maxLines = 1)
                }
            }
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
                    .clip(CtaShape)
                    .background(rc.emerald)
                    .clickable(onClick = action)
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(cta, style = RafiqType.titleM, color = rc.onEmerald, maxLines = 1)
                RafiqIcon(RIcon.ChevronLeft, 19.dp, rc.onEmerald)
            }
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
                    .border(1.dp, rc.cardBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onDayPage),
                contentAlignment = Alignment.Center,
            ) {
                RafiqIcon(RIcon.Info, 21.dp, rc.emerald)
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


/**
 * «٠٠:٥٦:٤٤» رقمُ مؤقّتٍ لا جملةُ رفيق. الشاشة تقول «بعد ٥٦ دقيقة» —
 * والثواني تُحذف لأنها تُعيد التركيب كل ثانية بلا فائدة للقارئ.
 *
 * وتصريفُ العدد عربيّ: ساعة · ساعتان · ٣ ساعات — لا «١ ساعة».
 * تُرجع null حين لا عدّاد بعد، فيُحذف السطر كلّه.
 */
@Composable
private fun humanCountdown(raw: String): String? {
    val (h, m) = countdownParts(raw) ?: return null
    /*  التصريفُ يُترك لأندرويد لا يُكتب بالكود.
     *
     *  كانت الصيغُ الستُّ مكتوبةً هنا عربيةً (ساعة · ساعتين · ٣ ساعات …)
     *  — صحيحةً في العربية، ولا شيءَ منها يعمل في الإنجليزية. ونظامُ
     *  `plurals` في أندرويد يعرف مثنّى العربية وصيغتَي جمعها ويعرف
     *  الإنجليزية، فيكفيه ملفُّ موارد.
     */
    val hs = if (h > 0) pluralStringResource(R.plurals.hours, h, h) else null
    val ms = if (m > 0) pluralStringResource(R.plurals.minutes, m, m) else null
    val sep = stringResource(R.string.and_separator)
    return listOfNotNull(hs, ms).joinToString(sep)
}

/**
 * ساعاتُ العدّاد ودقائقُه، أو null حين لا عدّاد بعد.
 *
 * مفصولةٌ عن الصياغة ليختبرها اختبارُ وحدةٍ بلا أندرويد — وكان الاختبار
 * قبلها ينسخ منطقَ الدالّة نسخاً بدل أن يناديها، فلا يحرس شيئاً.
 */
internal fun countdownParts(raw: String): Pair<Int, Int>? {
    val p = raw.split(":")
    if (p.size != 3) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    if (h == 0 && m == 0) return null
    return h to m
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
    doneIds:    Set<String> = emptySet(),
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
                /*  ثلاثُ حالاتٍ لا اثنتان.
                 *
                 *  التطبيقُ يعرف أنّ الوقت مضى، ولا يعرف أنّ صاحبَه صلّى.
                 *  فما مرَّ وقتُه «مضت» بحبرٍ خافت، ولا يصير «تمّت» ذهبيّةً
                 *  إلّا إن سجّلها هو. وكتابةُ «تمّت» لانقضاء الوقت وحده
                 *  شهادةٌ له بعبادةٍ لم يفعلها — وهي أسوأُ من كلِّ عدّاد. */
                val isDone = !waiting && stations.getOrNull(i)?.id in doneIds
                val isGone = nowIdx >= 0 && i < nowIdx && !isDone
                val nameColor by animateColorAsState(
                    when {
                        isNow  -> rc.ink
                        isDone -> rc.gold
                        isGone -> rc.inkLight
                        else   -> rc.inkLight
                    },
                    progressSpec(500), label = "stationColor",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // IntrinsicSize.Max يجعل الخطّ أدناه بعرض الاسم بالضبط
                    modifier = Modifier.width(IntrinsicSize.Max),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDone) {
                            RafiqIcon(RIcon.Check, 11.dp, rc.gold)
                            Spacer(Modifier.width(3.dp))
                        }
                        Text(
                            stringResource(short),
                            fontSize   = 14.sp,
                            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                            color      = nameColor,
                            maxLines   = 1,
                        )
                    }
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
                stringResource(R.string.hub_open_waraqa),
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
                        stringResource(st.title),
                        style = RafiqType.titleM,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNow || isPast) rc.ink else rc.inkMed,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(st.timeLabel).localizedDigits(ar),
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
    R.string.st_wake_s, R.string.st_fajr_s, R.string.st_duha_s, R.string.st_dhuhr_s,
    R.string.st_asr_s, R.string.st_maghrib_s, R.string.st_isha_s, R.string.st_sleep_s,
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
        DoorChip(stringResource(R.string.tasbeeh_title), Modifier.weight(1f), onTasbeeh) { IcoMisbaha(20.dp, it) }
        DoorChip(stringResource(R.string.qibla_title),  Modifier.weight(1f), onQibla)   { IcoCompass(20.dp, it) }
        DoorChip(stringResource(R.string.prayer_times_title), Modifier.weight(1f), onTimes)  { IcoMosque(20.dp, it) }
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
