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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import app.rafiqaldhikr.ui.utils.formatClock
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.ui.components.nextMunasaba
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
import app.rafiqaldhikr.ui.hero.HeroAnim
import app.rafiqaldhikr.ui.hero.HeroBackdrop
import app.rafiqaldhikr.ui.hero.HeroWreath
import app.rafiqaldhikr.ui.hero.MeeqatLine
import app.rafiqaldhikr.ui.hero.Nida
import app.rafiqaldhikr.ui.hero.NidaBlock
import app.rafiqaldhikr.ui.hero.NidaStore
import app.rafiqaldhikr.ui.hero.nidaCall
import app.rafiqaldhikr.ui.hero.HeroKind
import app.rafiqaldhikr.ui.hero.HeroStore
import app.rafiqaldhikr.ui.hero.heroPen
import app.rafiqaldhikr.ui.hero.rememberHeroEntrance
import app.rafiqaldhikr.ui.sky.WeatherStore
import app.rafiqaldhikr.ui.sky.moonPosition
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

   ═══ الأخضر ═══
   كانا أخضرين: داكنٌ يُقرأ نصّاً وفاتحٌ يصلح ملءاً. وصارا **واحداً**
   هو #17402F — أعلى تدرّج `HeroBackdrop` عند الظهيرة بعينه، فالأيقونةُ
   والزرُّ من لون السماء التي فوقهما لا من لونٍ يجاورها. ويصحّ الدوران
   معاً: نصّاً ١١٫٣٩:١ على الورق، وملءاً ٧٫٥٤:١ بحبرٍ فاتحٍ فوقه.

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
    //  وموضعُه الحقيقيّ — كان يُرسم في مكانٍ ثابتٍ لا يطلع ولا يغيب.
    val moonAt = remember(now / 60_000L, home.lat, home.lng) {
        moonPosition(now, home.lat, home.lng)
    }

    /*  طقسُ مكانك — طبقةٌ تُضاف، لا شرطٌ للعمل.
     *
     *  يُقرأ المحفوظُ فوراً بلا شبكة فتُرسم السماءُ بحالٍ صحيحةٍ من أوّل
     *  إطار، ثمّ يُجلب الجديدُ إن وُجد اتّصال. وبلا إنترنتٍ تبقى السماءُ
     *  تامّةً بشمسها وقمرها ونجومها — ويسقط المطرُ والضبابُ وحدَهما.
     *
     *  ولا تُطلب إلّا حين يُعرف المكان: طقسُ إحداثيّاتٍ صفريّةٍ طقسُ
     *  نقطةٍ في المحيط الأطلسيّ، لا طقسُ صاحب الهاتف. */
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var weather by remember { mutableStateOf(WeatherStore.cached(ctx)) }

    /*  بطاقةُ الشاشة الأولى — انظر `ui/hero/HeroCard.kt`.
     *
     *  المحفوظُ يُقرأ فوراً بلا شبكة، ثمّ يُجدَّد في الخلفية إن مضت
     *  ثلاثُ ساعات. فلا تنتظر الشاشةُ الشبكةَ لحظةً واحدة، ولا تظهر
     *  فارغةً في الطائرة. */
    val heroBase = stringResource(R.string.hero_base_url)
    var hero by remember { mutableStateOf(HeroStore.cached(ctx)) }
    LaunchedEffect(heroBase) {
        if (heroBase.isNotBlank()) HeroStore.refresh(ctx, heroBase)?.let { hero = it }
    }
    //  الصورةُ تُفكّ مرّةً لكلّ ملفّ، لا في كلّ إطار.
    val heroBg = remember(hero?.src) {
        val c = hero
        if (c == null || c.kind != HeroKind.IMAGE) null
        else HeroStore.mediaFile(ctx, c.src)?.let {
            runCatching { android.graphics.BitmapFactory.decodeFile(it.path) }.getOrNull()
        }
    }
    LaunchedEffect(home.lat, home.lng) {
        if (home.lat != 0.0 || home.lng != 0.0) {
            weather = WeatherStore.refresh(ctx, home.lat, home.lng)
        }
    }
    val sky = remember(sun.altitude) { skyColors(sun.altitude.toFloat()) }
    val skyInk = remember(sky) { skyInk(sky) }

    /*  السماءُ بمقاس ما فيها.
     *
     *  الارتفاعُ **مثبَّتٌ ولا بدّ**: بداخله `Spacer(weight(1f))` يوزّع
     *  الفراغَ بين الكلام والقوس، و`weight` في عمودٍ بلا سقفٍ يبتلع
     *  الشاشةَ كلَّها فتُدفَع الورقةُ خارجها. فيُثبَّت — لكن بقيمتين:
     *  حين يُرسم القوسُ يحتاج مئةً وثمانيَ نقاطٍ وسطرَه، وحين لا
     *  يُرسم لا معنى لحجز مكانه فارغاً. */
    val meeqat = LocalMeeqat.current
    val times = meeqat.times

    /*  موعظةُ الطور — انظر `ui/hero/Nida.kt`.
     *
     *  تُقرأ من الأصول مرّةً واحدةً خارج الخيط الرئيسيّ، ولا تُعرض حتى
     *  تجهز: لا إطارَ فارغاً ينتظر محتواه. */
    var nidaAll by remember { mutableStateOf<List<Nida>>(emptyList()) }
    LaunchedEffect(Unit) { nidaAll = NidaStore.load(ctx) }
    val nida = remember(nidaAll, meeqat.phase) {
        nidaAll.firstOrNull { it.phase == NidaStore.keyOf(meeqat.phase) }
    }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        /*  الخلفيّة — انظر `ui/hero/HeroBackdrop.kt`.
         *
         *  مرّت هذه الرقعةُ بأربعة أطوار وسقط كلُّها: تدرُّجٌ رماديٌّ
         *  يقطعه شريط، فسماءٌ على المعالج الرسوميّ، فبستانٌ مرسوم،
         *  فصورةُ نهرٍ فوتوغرافيّة. والصورةُ سقطت بالقياس لا بالذوق:
         *  ٦:١ تبايناً خلف السطر بعد حجابٍ ثقيل، مقابل ما فوق ١٢:١
         *  للتدرُّج بحجابٍ خفيف — و٣٠٨ كيلوبايت مقابل سطور حساب.
         *
         *  وما بقي حركةٌ واحدةٌ صادقة: الضوءُ يزحف مع الساعة. */
        HeroBackdrop(
            sunAlt        = sun.altitude,
            reducedMotion = LocalReducedMotion.current,
            weather       = weather,
            /*  **لا ذوبانَ في الورقة.**
             *
             *  كان أسفلُ السماء يذوب في لون الورق «فلا يبقى قطعٌ حادّ»
             *  — وعلى الجهاز خرج **ضباباً أبيضَ متّسخاً** فوق حافّة
             *  الورقة، لا ذوباناً. والورقةُ لها حافّةٌ مدوّرةٌ تجلس على
             *  السماء: الحدُّ الحادُّ هنا صوابٌ لا عيب — ورقةٌ فوق
             *  سماءٍ، لا سماءٌ تُمسح. */
            background    = heroBg,
            modifier      = Modifier.fillMaxWidth().height(SKY_H),
        )

        /*  الإكليل — انظر `ui/hero/HeroWreath.kt`.
         *
         *  هنا لا داخلَ `HeroBackdrop`: ذاك يُنهي رسمَه بحجابٍ يُطفئ ما
         *  تحته ليُقرأ الكلام، والغصنُ إن وقع أسفلَه ابتُلع. */
        HeroWreath(
            reducedMotion = LocalReducedMotion.current,
            modifier      = Modifier.fillMaxWidth().height(SKY_H),
        )

        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            /*  ترتيبُ السماء: كلامٌ أعلى · صورةٌ وسطى · حبّةٌ على الأفق.
             *
             *  كان الكلامُ كلُّه مكدَّساً في الأعلى (شريطٌ فتحيّةٌ فحبّة)،
             *  فبقي أسفلُ السماء فراغاً ميّتاً، ووقع القرصُ خلف «نهارٌ
             *  طيّب». الآن للصورة نطاقٌ خالصٌ بينهما، والحبّةُ — وهي
             *  أقربُ ما يُقرأ إلى الفعل — تجلس على حافّة الورقة مباشرةً.
             */
            CompositionLocalProvider(LocalContentColor provides heroInk) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(SKY_TEXT_H)
                        .padding(horizontal = 20.dp),
                ) {
                    SkyTopBar(
                        hijri = home.hijriDate.localizedDigits(ar),
                        ink   = heroInk,
                        onSettings = { navController.navigate(RafiqRoute.Settings.route) },
                    )
                    Spacer(Modifier.height(17.dp))
                    /*  نداءٌ لا تحيّة.
                     *
                     *  «نهارٌ طيّب» مجاملةٌ تُقرأ مرّةً ثمّ تُهمَل. والشاشةُ
                     *  الأولى موضعُ فعلٍ لا مجاملة، فصار السطرُ فعلاً
                     *  مضارعاً يتبع الساعة: ابدأ · أدِمْ · أمسِكْ · اختم.
                     *
                     *  وهو **كلامُنا نحن لا حديثٌ مرويّ**، فلا إسنادَ
                     *  يُطلب منه. وما كان حديثاً فله بطاقتُه كاملاً
                     *  بتخريجه في «كلمةُ اليوم» — ولا يُقصُّ منه سطرٌ
                     *  ليُكتب فوق صورة. */
                    /*  والقلمُ يكتبه: القناعُ يمرّ عليه فيُقرأ كتابةً،
                     *  ويبقى **نصّاً** لا صورة — فيكبر مع خطّ المستخدم
                     *  ويقرؤه التدقيقُ الصوتيّ. */
                    /*  النداءُ يتبع **طورَ الميقات** لا ساعةَ الجدار: «أقبِلْ
                     *  قبل أن يُطبِقَ الليل» تُقال عند المغرب حيث وقع، لا عند
                     *  السابعة في كلّ بلد. وفيه دعاءٌ لصاحبه — «رعاك الله» —
                     *  فهو نداءٌ من رفيقٍ لا أمرٌ من آلة.
                     *
                     *  **وهو كلامُنا خالصاً**: لا يستعير لفظَ آيةٍ ولا حديث.
                     *  والنصُّ المرويُّ له موضعُه على الورق بخطِّه وإسناده. */
                    val (callId, subId) = nidaCall(meeqat.phase)
                    val line = hero?.title?.takeIf { it.isNotBlank() }
                        ?: stringResource(callId)
                    val anim = hero?.anim ?: HeroAnim.PEN
                    val write by rememberHeroEntrance(line, anim, LocalReducedMotion.current)
                    Text(
                        line,
                        //  خطُّ الواجهة لا الأميري — انظر `RafiqType.nida`.
                        style = RafiqType.nida,
                        color = heroInk,
                        modifier = Modifier.heroPen(anim, write),
                    )
                    /*  سطرُ الطقس تحت التحيّة مباشرةً — لا حبّةً ثانيةً
                     *  تزاحم حبّةَ الميقات في الأسفل.
                     *
                     *  والسماءُ تُظهر المطرَ والضبابَ، لكنّ العينَ تحتاج
                     *  من يسمّي لها ما ترى: «غائمٌ · ٢٨°» تُقرأ في لحظة،
                     *  والغيمُ وحدَه يحتمل أن يكون زينة. */
                    //  سطرُ البطاقة يحلّ محلَّ سطر الطقس حين يوجد — سطرٌ
                    //  واحدٌ تحت التحيّة لا سطران يتزاحمان.
                    //  سطرٌ واحدٌ تحت النداء لا ثلاثة: بطاقةُ المدير إن
                    //  وُجدت، وإلّا الطقسُ إن عُرف، وإلّا تتمّةُ النداء.
                    val sub = hero?.note?.takeIf { it.isNotBlank() }
                        ?: weatherLine(weather, ar).takeIf { weather.known }
                        ?: stringResource(subId)
                    if (sub != null) {
                        Spacer(Modifier.height(5.dp))
                        //  السطرُ الصغيرُ يتبع الكبيرَ بعد أن يُكتب نصفُه.
                        Text(
                            sub,
                            style = RafiqType.bodyS,
                            color = heroInk.copy(alpha = 0.80f * ((write - 0.45f) / 0.55f).coerceIn(0f, 1f)),
                        )
                    }
                    Spacer(Modifier.weight(1f))          // نطاقُ الصورة
                    /*  الميقاتُ على قوس الشمس — انظر `ui/hero/SunArc.kt`.
                     *
                     *  كانت هنا حبّةٌ زجاجيّةٌ مكتوبٌ فيها «الظهر بعد
                     *  ساعةٍ وربع». والحبّةُ شكلٌ يُستعار من أيّ تطبيق،
                     *  والجملةُ تقول ما يقوله عدّادٌ في أيّ تطبيق.
                     *
                     *  والقوسُ يقول الشيءَ نفسَه **بموضعٍ لا بعبارة**:
                     *  الظهرُ في القمّة لأنّه الزوال، والمغربُ على الأفق
                     *  لأنّه الغروب، والفجرُ والعشاءُ تحته فيصير القرصُ
                     *  هلالاً. والسطرُ الذي كانت الحبّةُ تحمله باقٍ
                     *  تحته — اسمُ القادم ووقتُه وكم بقي.
                     *
                     *  والمواقيتُ من `LocalMeeqat` لا من حسابٍ جديد، فإن
                     *  لم تُحلّ لم يُرسم شيء — لا نخترع وقتاً. */
                    /*  سطرٌ واحد: الاسمُ والساعةُ وكم بقي. لا رسمَ.
                     *
                     *  كان هنا قوسُ شمسٍ يمشي عليه قرصٌ وعليه ستُّ
                     *  علامات. وكان صحيحاً في حسابه — ومرفوضاً في
                     *  شاشته: لا يُفهم بلا شرح، وبلا موقعٍ يصير بيضةً
                     *  فارغة. والقاعدةُ التي تعلّمتُها منه: **ما احتاج
                     *  شرحاً على الشاشة الأولى فقد سقط**.
                     *
                     *  فبقي ما كان القوسُ يخدمه أصلاً: متى الصلاةُ
                     *  القادمةُ وكم بقي لها. وهو ما يُقرأ في نصف ثانية.
                     *
                     *  وبلا مواقيتَ لا سطر: البطاقةُ تحته تقول «حدِّد
                     *  مدينتك» مرّةً واحدة، وتكرارُها هنا ضجيج. */
                    if (times != null) {
                        //  خطٌّ رفيعٌ يفصل الميقاتَ عن النداء — كان غائباً،
                        //  فيلتصق السطرُ بما فوقه بلا حدٍّ يفصله.
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .height(1.dp)
                                .background(heroInk.copy(alpha = 0.18f)),
                        )
                        Spacer(Modifier.height(12.dp))
                        MeeqatLine(
                            times = times,
                            nowMs = now,
                            left  = humanRemaining(nextMeeqatAt(times, now) - now),
                            ink   = heroInk,
                            ar    = ar,
                        )
                        Spacer(Modifier.height(18.dp))
                    }
                }
            }

            /* الورقة — ترتفع حيث ينتهي كلامُ السماء، فتتبع مقاسَ الخطّ
               بدل ارتفاعٍ مثبَّتٍ يُقصّ حين يكبّره صاحبُه. */
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    //  لونٌ واحدٌ لا تدرّج. كان طرفُ الورق يحمل نبرةً
                    //  من السماء (١٤٪) فيُقرأ ظلّاً باهتاً تحت الحافّة،
                    //  والنموذج ورقٌ صِرف.
                    .background(rc.bg)
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

                /*  الموعظةُ أوّلَ الورقة.
                 *
                 *  على **الورق** لا على السماء، وبخطّ `AmiriFamily` لا خطّ
                 *  الواجهة، وإسنادُها ذهبيٌّ يُلمَس فيفتح المصدر. ثلاثُ
                 *  طبقاتٍ تفصل صوتَ الوحي والأثر عن صوتنا — سطحاً وحرفاً
                 *  ولوناً — فلا يلتبسان على عينٍ ولا على قارئٍ عجل. */
                nida?.let { NidaBlock(it) }

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

                /*  «كلمةُ اليوم» نزلت إلى «أوراقي».
                 *
                 *  كانت الشاشةُ الأولى تحمل ثلاثةَ نصوصٍ معاً: موعظةَ
                 *  الطور، وفضلَ المحطّة، وكلمةَ اليوم. والنصُّ الصريح:
                 *  «كانَ يَتَخَوَّلُنا بالمَوْعِظَةِ في الأيَّامِ **كَراهَةَ
                 *  السَّآمَةِ عَلَيْنا**» — البخاري ٦٨. فبقي نصٌّ واحد. */

                DoorsRow(
                    onTasbeeh = { navController.navigate(RafiqRoute.Tasbeeh.route) },
                    onQibla   = { navController.navigate(RafiqRoute.Qibla.route) },
                    onTimes   = { navController.navigate(RafiqRoute.PrayerTimes.route) },
                )

                MunasabaLine(ar)
            }
        }
    }
}

/**
 * حبرُ الشاشة الأولى — كريميٌّ ثابتٌ في كلّ ساعة.
 *
 * كان يُشتقّ من ألوان السماء المحسوبة، ولا سماءَ محسوبةً اليوم: الصورةُ
 * تحته شجرٌ داكنٌ في أعلى الإطار مهما تغيّرت الساعة، وحجابُ `Tareeq`
 * يضمن التباينَ فوق ذلك.
 */
private val heroInk = Color(0xFFF4EFE2)

/*  علامةُ الميقات القادم.
 *
 *  جُرّب الأخضرُ الزيتونيُّ نفسُه فاختفى ليلاً: القوسُ يقع على سماءٍ
 *  تُظلم، وداكنٌ على داكنٍ لا يُرى. والذهبُ يُقرأ على طرفَي اليوم كليهما. */
private val MEEQAT_GOLD = Color(0xFFE8C46A)

/** أوّلُ ميقاتٍ لم يأتِ بعد، وإلّا فجرُ الغد — بالترتيب نفسِه الذي
 *  يمشي عليه `SunArc`، فلا يفترق السطرُ عن العلامة. */
private fun nextMeeqatAt(t: app.rafiq.domain.model.PrayerTimesResult, now: Long): Long =
    listOf(t.fajr, t.sunrise, t.dhuhr, t.asr, t.maghrib, t.isha)
        .firstOrNull { it > now } ?: (t.fajr + 86_400_000L)

/** ارتفاعُ السماء، وارتفاعُ كلامها. والورقةُ تبدأ حيث ينتهي الكلام. */
private val SKY_TEXT_H = 288.dp
/** وأسفلُ التدرّج يبقى خلف الورقة، فلا تنتهي السماءُ عند حافّتها. */
private val SKY_H = 360.dp

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
                    .size(44.dp)
                    .clip(RoundedCornerShape(5.dp, 5.dp, 17.dp, 5.dp))
                    .background(glassBg)
                    .border(1.dp, glassBd, RoundedCornerShape(5.dp, 5.dp, 17.dp, 5.dp)),
                contentAlignment = Alignment.Center,
            ) { Text("ر", style = RafiqType.mark, fontSize = 30.sp, color = ink) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(stringResource(R.string.app_name), style = RafiqType.titleM, color = ink)
                //  التاريخُ الهجريُّ ليس كلاماً — كوفيٌّ كما في النموذج
                Text(hijri, style = RafiqType.metaL, color = ink.copy(alpha = 0.78f), maxLines = 1)
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
        ) { RafiqIcon(RIcon.Settings, 17.dp, ink) }
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
    /*  الفضلُ بنصّه لا شارةُ تخريجه.
     *
     *  كانت البطاقة تعرض [StationUi.source] وحدَه — «رواه مسلم» — فيقرأ
     *  المستخدم إسناداً بلا مُسنَد. والنموذج يعرض [StationUi.virtue]:
     *  الحديثَ وتخريجَه معاً، فيعرف **لماذا** هذه المحطّة قبل أن يبدأها. */
    val virtue: String?
    val cta:    String
    /** «اقرأ على مهل» — لا تُقال إلّا حين يكون الزرُّ بدايةَ قراءةٍ فعلاً. */
    val unhurried: Boolean
    val action: () -> Unit
    when {
        // بلا إحداثيات لا مواقيت، وبلا مواقيت لا محطّات — فكانت البطاقة
        // تعرض «أذكار يومك» عامّةً بلا نافذةٍ ولا خيطٍ ولا إسناد، ولا
        // تقول لماذا هي كذلك. الآن تقول السبب وتحمل علاجه.
        needsLoc -> {
            title = stringResource(R.string.hub_set_city)
            desc = "محطّاتُ يومك موقوتةٌ بالصلاة — من الاستيقاظ إلى النوم. " +
                "حدِّدها مرّةً واحدة ويُحسب الباقي."
            virtue = null; cta = stringResource(R.string.hub_set_location)
            unhurried = false; action = onDayPage
        }
        station == null -> {
            title = stringResource(R.string.hub_day_adhkar)
            desc = stringResource(R.string.hub_day_sub)
            virtue = null; cta = stringResource(R.string.action_open)
            unhurried = false; action = onDayPage
        }
        station.route == null -> {
            title = stringResource(station.title); desc = stringResource(station.description).localizedDigits(ar)
            virtue = station.virtue; cta = stringResource(R.string.action_detail)
            unhurried = false; action = onDayPage
        }
        else -> {
            title = stringResource(station.title); desc = stringResource(station.description).localizedDigits(ar)
            virtue = station.virtue
            //  «ابدأ» وحدَها لا تقول ماذا تبدأ. «ابدأ صلاة الضحى» تقول.
            cta = stringResource(R.string.action_start_named, stringResource(station.title))
            unhurried = true; action = onStart
        }
    }


    /*  كانت البطاقةُ سطحاً أخضرَ داكناً لأنّها كانت مركزَ ثقل الشاشة.
     *  والسماءُ صارت المركز، فلو بقيت داكنةً لتنازعتا. ثمّ صارت عاريةً
     *  على الورق بلا حدٍّ ولا سطح — فذابت فيه ولم تعد تُقرأ بطاقةً.
     *
     *  فهي الآن **صندوق**: سطحُ [card] على ورق [bg]، وحدٌّ رفيع، وزاويةٌ
     *  واحدةٌ عريضةٌ في bottomStart توقيعاً كسائر أسطح التطبيق. */
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(ActShape)
            .background(rc.card)
            .border(1.dp, rc.cardBorder, ActShape)
            .padding(horizontal = 17.dp, vertical = 16.dp),
    ) {
        /*  رأسُ البطاقة: «خطوتك الآن» ⟷ «محطّة الضحى».
         *
         *  سطرٌ صغيرٌ يفعل شيئين لا تفعلهما البطاقةُ بدونه: يقول إنّ ما
         *  تحته **فعلٌ مطلوبٌ الآن** لا خبر، ويقول **أين أنت من يومك**
         *  بلا أن تنزل العينُ إلى صفّ المحطّات. وكان غائباً. */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.hub_eyebrow_now),
                style = RafiqType.metaS,
                color = rc.inkLight,
                maxLines = 1,
            )
            //  بلا موقعٍ لا محطّةَ يُوسَم بها، ولو كانت آخرَ محطّةٍ محسوبة:
            //  البطاقةُ تطلب الموقع، فوسمُ «محطّة العصر» فوقها يناقضها.
            station?.takeIf { !needsLoc }?.let {
                Text(
                    stringResource(R.string.hub_station_tag, stringResource(it.short)),
                    style = RafiqType.metaS,
                    fontWeight = FontWeight.SemiBold,
                    color = rc.emerald,
                    maxLines = 1,
                )
            }
        }

        /*  عنوانُ البطاقة **أخفضُ من النصّ المرويّ** فوقه (٢٢ مقابل ٢٦)،
         *  وبخطّ الواجهة. كان ثلاثين أميريّاً عريضاً — فتقع العينُ على
         *  «صلاة الضحى» قبل كلام النبيّ ﷺ. */
        Text(
            title,
            style = RafiqType.titleL,
            //  عشرون لا اثنتان وعشرون — النموذج يخفض العنوان ستَّ نقاطٍ
            //  تحت النصّ المرويّ فوقه، لا أربعاً.
            fontSize = 20.sp,
            lineHeight = 31.sp,
            color = rc.ink,
            modifier = Modifier.padding(top = 3.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            desc,
            style = RafiqType.bodyS,
            fontSize = 13.5.sp,
            color = rc.inkMed,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (virtue != null) {
            /*  الفضلُ بعرض البطاقة على سطح الورق.
             *
             *  كان حبّةً محاطةً **تقاسم الزرَّ الصفَّ**، فيضيق كلاهما ولا
             *  يتّسع إلّا لشارة «رواه مسلم». وهو هنا سطرُه، فيتّسع للحديث
             *  بتخريجه. والشريطُ الذهبيُّ يمتدّ بارتفاع النصّ لا بمقاسٍ
             *  ثابت — ومن هنا [IntrinsicSize.Min]. */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(rc.bg)
                    .padding(horizontal = 11.dp, vertical = 9.dp)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    Modifier
                        .width(3.dp)
                        .heightIn(min = 16.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(rc.gold),
                )
                Text(virtue, style = RafiqType.caption, fontSize = 11.5.sp, color = rc.gold)
            }
        }

        /*  الزرُّ بعرض البطاقة.
         *
         *  كان جزءَ عرضٍ بجانب حبّةِ المصدر وزرِّ ⓘ — ثلاثةُ أهدافٍ في صفٍّ
         *  واحد، وأحدُها (ⓘ) بابُه مكرَّرٌ في صفّ «افتح ورقة يومك» تحته.
         *  فحُذف المكرَّر، وبقي هدفٌ واحدٌ لا يُخطئه الإصبع.
         *
         *  و«اقرأ على مهل» في طرفه بدل السهم: السهمُ يقول «إلى أين»
         *  وهو معلومٌ من النصّ، وهذه تقول **كيف** — وهي الأنفع. */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .heightIn(min = 54.dp)
                .clip(CtaShape)
                .background(rc.emerald)
                .clickable(onClick = action)
                .padding(horizontal = 17.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            /*  سطران لا سطرٌ واحد.
             *
             *  أطولُ عنوانٍ في اليوم «سورة الكهف والصلاة على النبي ﷺ»،
             *  ومعه «ابدأ» و«اقرأ على مهل» لا يتّسع في ٣٩٠ نقطة. وسطرٌ
             *  واحدٌ يعني قطعَه بنقاطٍ في منتصفه — واقتطاعُ عنوانِ عملٍ
             *  شرعيّ ليس خياراً. فيلتفّ الزرُّ ويعلو، وهو أهونُ. */
            Text(
                cta,
                style = RafiqType.titleM,
                fontSize = 16.sp,
                color = rc.onEmerald,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (unhurried) {
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.hub_cta_hint),
                    style = RafiqType.meta,
                    color = rc.onEmerald.copy(alpha = 0.85f),
                    maxLines = 1,
                )
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
    topStart = 15.dp, topEnd = 15.dp, bottomEnd = 15.dp, bottomStart = 26.dp,
)

/** سطحُ بطاقة الفعل — نفسُ التوقيع بمقاسٍ أكبر: زاويةٌ عريضةٌ واحدة. */
private val ActShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 32.dp,
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
        /*  ═══ شرائحُ المحطّات ═══
         *
         *  مرَّ هذا الصفُّ بثلاثة أطوار: تسعةُ أسماءٍ في سطرٍ واحدٍ تحت
         *  الحاضر منها خطّ؛ ثمّ نقطةٌ لكلٍّ على قضيبٍ يمتلئ. وسقط
         *  كلاهما لعلّةٍ واحدة: **ثمانيةُ أسماءٍ لا تتّسع في عرض شاشة.**
         *  فتتزاحم حتّى تلتصق، ويصغر حرفُها حتّى يُقرأ بمشقّة، ولا يبقى
         *  مكانٌ لوقتٍ بجانب اسم.
         *
         *  والحلُّ أن يُترك العرضُ يقرّر: صفٌّ يتمرّر، كلُّ محطّةٍ فيه
         *  شريحةٌ تأخذ ما تحتاجه — اسمُها ووقتُها. فتتنفّس الأسماء،
         *  ويظهر الوقتُ الذي لم يكن له مكانٌ قطّ.
         *
         *  والحاضرةُ شريحةٌ **مصمتة** — أظهرُ شيءٍ في الورقة كلِّها —
         *  ويُزحَف إليها تلقائياً عند الفتح فتُرى بلا أن يبحث عنها أحد.
         */
        val chips = rememberLazyListState()
        LaunchedEffect(nowIdx, names.size) {
            //  تُترك واحدةٌ قبلها في المشهد: يُقرأ «من أين جئت» مع «أين أنت»
            if (nowIdx >= 0) chips.animateScrollToItem((nowIdx - 1).coerceAtLeast(0))
        }
        LazyRow(
            state = chips,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(names) { i, short ->
                val isNow = i == nowIdx
                /*  ثلاثُ حالاتٍ لا اثنتان.
                 *
                 *  التطبيقُ يعرف أنّ الوقت مضى، ولا يعرف أنّ صاحبَه صلّى.
                 *  فما مرَّ وقتُه يبقى كما هو، ولا يصير ذهبيّاً إلّا إن
                 *  سجّله هو. وكتابةُ «تمّت» لانقضاء الوقت وحده شهادةٌ له
                 *  بعبادةٍ لم يفعلها — وهي أسوأُ من كلِّ عدّاد. */
                val st = stations.getOrNull(i)
                StationChip(
                    name = stringResource(short),
                    time = st?.startMillis?.let { formatClock(it, ar) },
                    isNow = isNow,
                    isDone = !waiting && st?.id in doneIds,
                    onClick = onOpen,
                )
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
 * شريحةُ محطّة — اسمٌ ووقتُ بدايتها.
 *
 * والحاضرةُ مصمتةٌ بلون الهويّة وحرفٍ ثقيل، والمسجَّلةُ محاطةٌ بذهبٍ
 * وأمامها نقطة، وما عداهما هادئ. ولا لونَ رابعاً: الموضعُ في الصفّ
 * يقول ما مضى وما بقي بلا صبغة.
 *
 * والارتفاعُ اثنتان وأربعون نقطةً حدّاً أدنى — الشريحةُ مقصودةٌ باللمس
 * لا زخرفةً تُقرأ، ومساحةُ الإصبع لا تُقاس بحجم الحرف.
 */
@Composable
private fun StationChip(
    name: String,
    time: String?,
    isNow: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val bg by animateColorAsState(
        if (isNow) rc.emeraldFill else Color.Transparent,
        progressSpec(500), label = "chipBg",
    )
    val border = when {
        isNow -> rc.emeraldFill
        isDone -> rc.gold.copy(alpha = 0.40f)
        else -> rc.divider
    }
    /*  `inkMed` لا `inkLight`.
     *
     *  النموذجُ رسمها بحبرٍ فاتحٍ جدّاً، وقاعدةُ اللوحة في هذا المشروع
     *  صريحة: `inkLight` للأيقونات والعناصر الخاملة، و**النصُّ الخافت
     *  يستعمل `inkMed`** — وهو ٤٫٥٤:١ على الورق، أي على حدّ المقروئيّة
     *  بالضبط ولا يُنزَل عنه. */
    val fg = when {
        isNow -> rc.onEmeraldFill
        isDone -> rc.gold
        else -> rc.inkMed
    }
    Row(
        Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 42.dp)
            .padding(horizontal = if (isNow) 17.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isDone) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(rc.gold))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            name,
            fontSize = 14.sp,
            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
            color = fg,
            maxLines = 1,
        )
        if (time != null) {
            Spacer(Modifier.width(6.dp))
            /*  الوقتُ يتميّز **بالمقاس لا بالشفافيّة**: خفضُ ألفا
             *  الحبرِ الخافت إلى ٧٥٪ يُسقط تباينَه تحت ٤٫٥:١، وهو نصٌّ
             *  صغيرٌ أصلاً. فبقي لونُه كاملاً وصغر حرفُه. */
            Text(
                time,
                fontSize = 11.5.sp,
                color = fg,
                maxLines = 1,
            )
        }
    }
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
    val rc = LocalRafiqColors.current
    /*  الأبوابُ الثلاثة — وهي ليست في النموذج.
     *
     *  والنموذجُ يحذفها لأنّه معاينة؛ والتطبيقُ لا يقدر: `PrayerTimes`
     *  **لا بابَ لها سواها** في التطبيق كلِّه، فحذفُها يُيتِّم الشاشة.
     *
     *  فالحلُّ ليس الحذفَ بل **إدخالَها في لغة النموذج**: كانت ثلاثَ
     *  حبّاتٍ محاطةٍ بارتفاع خمسين نقطةً وخطِّ عنوانٍ ثمانيَ عشرة — لوحاً
     *  غريباً يُنادي على نفسه أسفلَ الورق. وهي الآن بلغة الشريط السفليِّ
     *  نفسِها: رمزٌ بجانب اسمٍ بالكوفيّ العُشْرِ والنصف، وخطٌّ فاصلٌ
     *  فوقها. فصارت **ذيلَ الصفحة** لا بطاقةً ثالثة، ووفّرت ثلاثين نقطة.
     *
     *  والمساحةُ المقصودةُ باللمس ثمانٍ وأربعون نقطةً لكلّ بابٍ رغم
     *  صِغَر ما يُرى — الإصبعُ لا يقيس بحجم الحرف. */
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(1.dp)
            .background(rc.divider),
    )
    Row(
        Modifier.fillMaxWidth().padding(top = 3.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Door(stringResource(R.string.tasbeeh_title), onTasbeeh) { IcoMisbaha(12.dp, it) }
        Door(stringResource(R.string.qibla_title), onQibla) { IcoCompass(12.dp, it) }
        Door(stringResource(R.string.prayer_times_title), onTimes) { IcoClock(12.dp, it) }
    }
}

/**
 * «المناسبة القادمة — بداية رمضان · بعد ١٢٣ يوماً».
 *
 * سطرٌ يُحوّل الوقتَ من شيءٍ يمرّ إلى شيءٍ **يُنتظَر**: من عرف أنّ بينه
 * وبين رمضان أربعةَ أشهرٍ استعدّ، ومن لم يعرف فوجئ به.
 *
 * وموضعُه **ذيلُ الصفحة** لا رأسُها: هو خبرٌ عن بعيدٍ، وفعلُ اليوم أولى
 * بالعين. ولا يُعرض شيءٌ إن تعذّر الحسابُ — سطرٌ خاطئٌ أسوأُ من غيابه.
 */
@Composable
private fun MunasabaLine(ar: Boolean) {
    val rc = LocalRafiqColors.current
    val ctx = LocalContext.current
    //  يُحسب مرّةً لكلّ يوم: التقويمُ لا يتبدّل بين إعادتَي تركيب.
    val today = remember { System.currentTimeMillis() / 86_400_000L }
    val m = remember(today) { nextMunasaba(ctx) } ?: return

    val away = when (m.daysAway) {
        0 -> stringResource(R.string.munasaba_today)
        1 -> stringResource(R.string.munasaba_tomorrow)
        else -> pluralStringResource(R.plurals.days, m.daysAway, m.daysAway)
            .localizedDigits(ar)
    }
    Row(
        Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.munasaba_next), style = RafiqType.metaS, color = rc.inkLight)
        Spacer(Modifier.width(7.dp))
        Text(
            stringResource(m.name),
            style = RafiqType.metaS,
            fontWeight = FontWeight.SemiBold,
            color = rc.gold,
        )
        Spacer(Modifier.width(7.dp))
        Text("·", style = RafiqType.metaS, color = rc.inkLight)
        Spacer(Modifier.width(7.dp))
        Text(away, style = RafiqType.metaS, color = rc.inkMed)
    }
}

@Composable
private fun Door(label: String, onClick: () -> Unit, icon: @Composable (Color) -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon(rc.emerald)
        Spacer(Modifier.width(5.dp))
        Text(label, style = RafiqType.metaS, color = rc.emerald, maxLines = 1)
    }
}

/**
 * سطرُ الطقس: «غائمٌ جزئياً · ٢٨°».
 *
 * وأسماءُ الأحوال من ترميز WMO — وهو معيارٌ عالميّ، فالرمزُ ٦١ مطرٌ
 * خفيفٌ في كلّ خدمةِ أرصادٍ في الدنيا. وتُجمَع الرموزُ المتقاربة في
 * اسمٍ واحد: لا فائدةَ لصاحب الهاتف من الفرق بين «رذاذٍ خفيف» و«رذاذٍ
 * متوسّط»، وهو يريد أن يعرف هل يحمل مظلّة.
 */
@Composable
private fun weatherLine(w: app.rafiqaldhikr.ui.sky.SkyWeather, ar: Boolean): String {
    val name = stringResource(
        when (w.code) {
            0 -> R.string.wx_clear
            1, 2 -> R.string.wx_partly
            3 -> R.string.wx_overcast
            45, 48 -> R.string.wx_fog
            in 51..57 -> R.string.wx_drizzle
            in 61..67, in 80..82 -> R.string.wx_rain
            in 71..77, 85, 86 -> R.string.wx_snow
            in 95..99 -> R.string.wx_thunder
            else -> R.string.wx_clear
        },
    )
    if (w.tempC.isNaN()) return name
    val deg = kotlin.math.round(w.tempC).toInt().toString().localizedDigits(ar)
    return "$name · $deg°"
}
