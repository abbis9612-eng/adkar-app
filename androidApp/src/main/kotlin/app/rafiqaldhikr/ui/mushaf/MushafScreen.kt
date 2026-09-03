package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.QuranFamily
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import app.rafiqaldhikr.ui.theme.NaskhFamily
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/* ══════════════════════════════════════════════════════════════
   شاشةُ المصحف — أربعةُ أنماطٍ في مكانٍ واحد

   الصفحةُ هي الوحدة لا السورة: ٦٠٤ صفحةً بترقيم المصحف المدنيّ،
   والانتقالُ بينها سحبٌ أفقيٌّ كما تُقلَب الورقة.

   والأنماطُ الأربعة تختلف في الرسم لا في النصّ: النصُّ واحدٌ من ملفّ
   التطبيق نفسِه، والصفحةُ واحدة، والتفسيرُ واحد. وما يشتريه النمطُ
   المصحفيُّ بالتنزيل شيءٌ واحد: مواضعُ قطع الأسطر.
══════════════════════════════════════════════════════════════ */

@Composable
fun MushafScreen(
    navController: NavHostController,
    openPage: Int = 0,
    /** آيةٌ تُبرَز عند الفتح — يأتي بها البحثُ أو العلامات. */
    openVerse: String = "",
) {
    val ctx = LocalContext.current
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val prefs = rememberMushafPrefs()
    val scope = rememberCoroutineScope()

    /*  التخطيطُ يُحمَّل على خيطٍ خلفيّ، لا داخل التأليف.
     *
     *  كان `remember { MushafLayout.load(ctx) }` — أي قراءةُ ١٫٢ ميغابايت
     *  من الأصول وتحليلُها إلى ٦٠٤ صفحةٍ و٨٤٬١٠٩ عنصرٍ **على الخيط
     *  الرئيسي وداخل أوّل تركيب**. وهو سببُ «المصحف يتجمّد عند فتحه»:
     *  الشاشةُ لا تُرسم حتى ينتهي التحليل كلُّه.
     *
     *  والآن تُرسم الورقةُ فارغةً بحالة تحميلٍ ثمّ تمتلئ.  */
    var layout by remember { mutableStateOf<MushafLayout?>(null) }
    var layoutLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        layout = withContext(Dispatchers.IO) {
            runCatching { MushafLayout.load(ctx) }.getOrNull()
        }
        layoutLoading = false
    }
    val fonts = remember { MushafFonts(ctx) }

    var mode by remember { mutableStateOf(prefs.mode) }
    var fontSize by remember { mutableIntStateOf(prefs.fontSize) }
    var sheet by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(openVerse.takeIf { it.isNotBlank() }) }
    // `isReady` يفحص وجودَ ٤٨ ملفاً — عملُ قرصٍ لا يقع في التأليف.
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    /** يُبدَّل كلّما وصل خطٌّ جديد فتُعاد قراءةُ الخطوط. */
    var fontTick by remember { mutableIntStateOf(0) }
    /*  الجلبُ مأذونٌ به على الواي‑فاي بلا سؤال، ومحظورٌ على بيانات
     *  الجوّال حتى يأذن صاحبُها صراحةً من ورقة الإعدادات.
     *
     *  فمن فتح المصحفَ في بيته وجد الصفحةَ المصحفية بعد ثوانٍ ولم
     *  يُسأل شيئاً؛ ومن فتحه في الطريق قرأ بالخطّ المشحون ولم يُنقص
     *  رصيدُه بايتاً واحداً بلا علمه. */
    var allowed by remember {
        mutableStateOf(prefs.fontsAllowed || ctx.isUnmetered())
    }

    /*  الصفحةُ المصحفية هي المقصودُ من الشاشة، لا خيارٌ في ورقة إعدادات.
     *  فيُعرض طلبُ التنزيل أوّلَ فتحٍ مرّةً واحدة — ولا يُلحّ بعدها.
     *  ويُنتظر التخطيطُ أوّلاً: كان مربوطاً بـ`Unit` فيقع قبل أن يُحمَّل. */
    /*  لا سؤالَ عند أوّل فتح — الصفحةُ تظهر وكفى.
     *
     *  كان يُعرض حوارٌ يغطّي الشاشة يشرح الفرقَ بين نمطَي عرضٍ لم يرَ
     *  المستخدمُ واحداً منهما بعد، ويعرض تنزيلَ تسعين ميغابايت. وفوقه
     *  بطاقةٌ ثانيةٌ وشريطُ تلميحٍ ثالث — ثلاثةُ نداءاتٍ قبل أن تُقرأ
     *  آية. ومن فتح المصحفَ أراد أن يقرأ، لا أن يقرّر.
     *
     *  والمصحفُ يُقرأ فوراً بخطّ Scheherazade المشحون في التطبيق —
     *  خطُّ نسخٍ قرآنيٌّ كاملُ التشكيل — ثمّ **يرقى من نفسه** إلى
     *  الصفحة المصحفية كلّما جاء خطُّها. فالفرقُ يُرى ولا يُشرح. */
    LaunchedEffect(layout) {
        val l = layout ?: return@LaunchedEffect
        ready = withContext(Dispatchers.IO) { fonts.isReady(l) }
    }

    // صفر يعني: افتح على آخر ما قرأ. وما عداه صفحةٌ طُلبت من القائمة أو البحث.
    val startPage = if (openPage in 1..604) openPage else prefs.lastPage
    val pager = rememberPagerState(initialPage = startPage - 1, pageCount = { 604 })
    LaunchedEffect(pager.currentPage) { prefs.lastPage = pager.currentPage + 1 }

    /*  والموضعُ يُحفظ في القاعدة أيضاً لا في التفضيلات وحدَها: تلك لا
     *  تخرج من هذه الشاشة، وهذه تراها الرئيسيةُ وتدخل في التصدير. */
    val positionVm: MushafPageViewModel = org.koin.androidx.compose.koinViewModel()
    LaunchedEffect(pager.currentPage, layout) {
        val l = layout ?: return@LaunchedEffect
        val no = pager.currentPage + 1
        val surah = l.page(no)?.firstSurah ?: return@LaunchedEffect
        if (surah > 0) positionVm.rememberPosition(surah, 1, no)
    }

    /*  رأسُ الصفحة يقول أين أنت: السورةُ والجزءُ والحزب. وكانت الشاشةُ
     *  تعرض رقمَ الصفحة وحده — ورقمٌ بلا سورةٍ لا يقول شيئاً لمن يقرأ. */
    /*  خطُّ الصفحة الحاضرة يُجلَب وحدَه — مليونا بايتٍ في ثوانٍ بدل
     *  تسعين ميغابايت. فتظهر الصفحةُ المصحفية من أوّل فتحٍ
     *  لمن أذِن، ويمتلئ الباقي كلّما قلَب ورقة. */
    /**
     * عدّادُ إعادة المحاولة.
     *
     * كان زرُّ «نزّل» يكتب `allowed = true` وهي `true` أصلاً بعد أوّل
     * موافقة — فلا تتغيّر حالةٌ، ولا يُعاد تشغيلُ هذا الأثر، ولا يقع شيء.
     * أي أنّ الزرَّ كان ميّتاً تماماً على كل تشغيلٍ بعد الأوّل: يضغطه من
     * فشل تنزيلُه فلا يحدث شيء ولا رسالة.
     */

    /*  حُذف من هنا جلبُ خطّ الصفحة من الإنترنت.
     *
     *  كان يُنزّل خطَّ الصفحة الحاضرة عند كل قلبِ ورقة — مليونَي بايتٍ
     *  في ثوانٍ — لأنّ الخطوطَ لم تكن في التطبيق. وقد صارت فيه، فسقط
     *  الجلبُ ومعه لافتتُه وشريطُ تقدّمه وحوارُ إذنه وعدّادُ إعادة
     *  محاولته: خمسُ حالاتٍ كانت تتعقّب شيئاً لم يعد يقع. */

    //  و`layout` هنا كذلك، للسبب نفسِه: التهيئةُ المسبقة كانت تنسحب
    //  على `null` ولا تعود، فيقع بناءُ الخطّ في منتصف السحب كما كان.
    LaunchedEffect(layout, pager.currentPage, fontTick) {
        val l = layout ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val now = pager.currentPage + 1
            for (p in (now - 1)..(now + 1)) {
                if (p in 1..604) runCatching { fonts.pageFonts(l, p) }
            }
        }
    }

    /*  حُذف من هنا استعلامُ صفحةٍ كامل نتيجتُه لا تُستعمل: كان يجلب آياتِ
     *  الصفحة ليحسب `suraName` ثمّ لا يقرؤه أحد — `PageMargin` يأخذ اسمَ
     *  السورة من `data.firstSurah`. استعلامٌ ومستمعُ قاعدةٍ عند كل قلبِ
     *  صفحةٍ بلا مقابل.  */

    // النمطُ المصحفيُّ بلا خطوطٍ يسقط إلى المضبوطة بدل صفحةٍ فارغة
    /*  كان السقوطُ إلى المضبوطة مشروطاً بتمام الخطوط كلِّها — فتبقى
     *  الصفحةُ بسيطةً وإن كان خطُّها حاضراً. الآن الشرطُ خطُّ هذه
     *  الصفحة وحدَه. */
    val pageFontReady = layout?.let { l ->
        /*  ليس خطَّ المتن وحدَه: لو نقص خطُّ اللوح رُسم اسمُ السورة
            بخطّ الصفحة فخرج كلمةً أخرى — فالنقصُ نصٌّ خاطئٌ لا فراغ. */
        fonts.isPageReady(l, pager.currentPage + 1)
    } ?: false
    val effective = if (mode.needsFonts && !pageFontReady) MushafMode.PAGE else mode

    /*  الليلُ يتبع اختيار صاحبِه لا حضورَ الخطّ.
     *
     *  كان `night = effective == MUSHAF_NIGHT`، و`effective` تسقط إلى
     *  `PAGE` الفاتحة حين تنقص الخطوط — فمن اختار «المصحفيةُ ليلاً» ولم
     *  يُنزّل بعد، فُتحت له ورقةٌ كريميّةٌ ساطعةٌ في الظلام. والاختيارُ
     *  اختيارُ لونٍ لا اختيارُ رسم، فلا يسقط بسقوطه.  */
    val night = mode == MushafMode.MUSHAF_NIGHT
    val paper = if (night) Color(0xFF15130E) else rc.bg
    val ink = if (night) Color(0xFFE8E1CF) else rc.ink

    /*  الأدواتُ تذوب.

        ليس في المصحف الورقيّ شريطٌ علويٌّ ولا سفليّ — واسمُ السورة
        والجزءُ ورقمُ الصفحة في الهامش حيث موضعُها. فالشاشةُ ورقةٌ
        خالصة، ولمسةٌ واحدةٌ في متنها تُظهر الأدواتِ وتُخفيها.  */
    var toolsOn by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf(!prefs.hintSeen) }
    var jump by remember { mutableStateOf(false) }

    /*  الشريطُ السفليُّ يختفي مع الأدوات ويعود معها — فتصير الورقةُ ورقةً
     *  كما يقول تصميمُ الشاشة، ولا يُحبَس القارئُ فيها: ضغطةٌ واحدةٌ على
     *  المتن تُرجع الأدواتِ والشريطَ معاً.  */
    /*  الورقةُ لا تتغيّر مقاساً أبداً — وهذا أصلُ العطب الذي كان.
     *
     *  كان `immersive` يتبع `toolsOn`: تُظهر الأدواتِ فيظهر معها شريطُ
     *  التطبيق السفليّ، فيطرح الهيكلُ ارتفاعَه (نحو ٨٠ نقطة) من المحتوى.
     *  والصفحةُ المصحفية تحسب مقاسَ خطّها من الارتفاع المتاح لتملأ
     *  الورقةَ بخمسةَ عشرَ سطراً — **فتُعيد الحسابَ وتصغر أمام عينَي
     *  القارئ** كلّما لمس الشاشة. والقارئُ لمَسَ ليرى أداة، لا ليتغيّر
     *  النصُّ الذي يقرؤه.
     *
     *  فالمصحفُ غامرٌ **دائماً**، وأدواتُه تطفو فوق الورقة ولا تأخذ من
     *  ارتفاعها شيئاً. والخروجُ منه بسهم الرجوع في الشريط العلويّ. */
    //  وأشرطةُ النظام تُخفى كذلك — الصفحةُ تُرى كاملةً كما تُطبع.
    app.rafiqaldhikr.ui.navigation.FullBleedReading()

    val immersive = app.rafiqaldhikr.ui.navigation.LocalImmersive.current
    LaunchedEffect(Unit) { immersive.value = true }
    DisposableEffect(Unit) { onDispose { immersive.value = false } }

    Box(Modifier.fillMaxSize().background(paper)) {
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            /*  لا `reverseLayout` هنا.
             *
             *  HorizontalPager يتبع اتّجاهَ التخطيط أصلاً، والتطبيقُ
             *  كلُّه RTL — فهو يقلب الورقةَ كما تُقلَب في المصحف بلا
             *  شيء. وإضافةُ `reverseLayout = true` تعكس المعكوسَ
             *  فيعود إلى اتّجاه الإنكليزية، وهو ما وقع. */
        ) { index ->
            val pageNo = index + 1
            val data = layout?.page(pageNo)
            val pf = if (effective.needsFonts) {
                remember(pageNo, fontTick) { layout?.let { fonts.pageFonts(it, pageNo) } }
            } else {
                null
            }

            Column(
                Modifier
                    .fillMaxSize()
                    /*  حشوةُ القُصاصة لا حشوةُ شريط الحالة: الأشرطةُ مخفيّةٌ
                     *  في هذه الشاشة فحشوتُها صفر، والباقي هو النَّقْر
                     *  (notch) وحدَه — فلا يختفي حرفٌ تحته ولا تُهدر
                     *  نقطةٌ من ارتفاع الورقة. */
                    .displayCutoutPadding()
                    /*  ولا `navigationBarsPadding` هنا: الشريطُ السفليُّ
                        يحملها في `RafiqBottomBar`، والهيكلُ يطرح ارتفاعَه
                        من المحتوى — فإعادتُها تقتطع من ارتفاع الورقة
                        نحوَ ثمانيةٍ وأربعين نقطةً بلا سبب، وهو ما ضيّق
                        الأسطرَ الخمسةَ عشر. */
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { toolsOn = !toolsOn },
            ) {
                PageMargin(
                    surah = data?.let { SurahNames.of(ctx, it.firstSurah) }.orEmpty(),
                    juz = data?.juz ?: 0,
                    odd = pageNo % 2 == 1,
                    ink = ink,
                )
                Box(Modifier.weight(1f)) {
                    if (effective.needsFonts && data != null && pf != null) {
                        MushafPageView(
                            page = data,
                            fonts = pf,
                            ink = ink,
                            accent = if (night) rc.goldLight else rc.gold,
                            marker = if (night) rc.goldLight else rc.goldLight,
                            selectedVerse = selected,
                            onTap = { toolsOn = !toolsOn },
                            onVerseClick = { selected = if (selected == it) null else it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        TextPage(
                            page = pageNo,
                            fontSize = fontSize,
                            ink = ink,
                            classic = effective == MushafMode.CLASSIC,
                            selectedVerse = selected,
                            onTap = { toolsOn = !toolsOn },
                            onVerseClick = { selected = if (selected == it) null else it },
                        )
                    }
                }
                PageFoot(
                    page = pageNo,
                    hizb = data?.rub ?: 0,
                    ar = ar,
                    ink = ink,
                    onJump = { jump = true },
                )
            }
        }

        /*  ما لا يُخفى: البلاغُ حين ينقص خطٌّ، وشريطُ التنزيل حين يجري.
            هذان حالُ النظام لا زينتُه، فيبقيان ظاهرين. */
        /*  نداءٌ واحدٌ في الوقت الواحد.
         *
         *  كانت الثلاثةُ تظهر معاً — تلميحُ اللمس، وبطاقةُ الصفحة
         *  المصحفية، وحوارٌ يغطّي الشاشة — فوق آيةٍ لم تُقرأ بعد.
         *
         *  فالتلميحُ أوّلاً لأنّه يُقرأ في ثانيةٍ ويُطوى؛ وبطاقةُ الخطّ
         *  تنتظر دورَها. ولا تُعرض البطاقةُ أصلاً إن كان الجلبُ مأذوناً
         *  به (واي‑فاي): الخطُّ آتٍ من نفسه، فلا معنى لطلبه. */
        Column(Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
            if (hint) {
                HintBar(
                    ink = ink,
                    onDismiss = { hint = false; prefs.hintSeen = true },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }

        ToolBar(
            visible = toolsOn,
            paper = paper,
            ink = ink,
            accent = if (night) rc.goldLight else rc.gold,
            onBack = { navController.popBackStack() },
            onList = { navController.navigate(RafiqRoute.QuranList.route) },
            onSearch = { navController.navigate(RafiqRoute.QuranSearch.route) },
            onMarks = { navController.navigate(RafiqRoute.QuranBookmarks.route) },
            onSettings = { sheet = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        /*  شريطُ الموضع — بنيةُ المصحف لا «صفحة ٣ من ٢٠».
         *
         *  السورةُ والجزءُ والحزبُ هي ما يعرف به القارئُ مكانَه في
         *  المصحف، فهي ما يُعرض. والمنزلقُ علامتُه ۞ — رمزُ ربع الحزب
         *  في المصحف المطبوع، لا نقطةً عامّة. */
        PageRail(
            visible = toolsOn,
            page = pager.currentPage + 1,
            surah = layout?.page(pager.currentPage + 1)
                ?.let { SurahNames.of(ctx, it.firstSurah) }.orEmpty(),
            juz = layout?.page(pager.currentPage + 1)?.juz ?: 0,
            hizb = layout?.page(pager.currentPage + 1)?.rub ?: 0,
            paper = paper,
            ink = ink,
            accent = if (night) rc.goldLight else rc.gold,
            ar = ar,
            onGo = { scope.launch { pager.scrollToPage(it - 1) } },
            onJump = { jump = true },
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        AyahSheet(
            verse = selected,
            page = pager.currentPage + 1,
            night = night,
            onDismiss = { selected = null },
        )
    }

    if (jump) {
        JumpSheet(
            ar = ar,
            onGo = { p ->
                jump = false
                scope.launch { pager.scrollToPage(p - 1) }
            },
            onDismiss = { jump = false },
        )
    }

    if (sheet) {
        SettingsSheet(
            mode = mode,
            fontSize = fontSize,
            sizeApplies = !effective.needsFonts,
            onMode = { mode = it; prefs.mode = it },
            onSize = { fontSize = it; prefs.fontSize = it },
            onDismiss = { sheet = false },
        )
    }
}

/* ── نمطان بلا تنزيل ───────────────────────────────────────────

   [classic] آيةٌ في فقرةٍ برقمها — أسهلُ للّمس والتفسير والنسخ.
   وغيرُه نصٌّ متّصلٌ مضبوطُ الطرفين كالورقة، والأرقامُ في متنه.
──────────────────────────────────────────────────────────────── */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TextPage(
    page: Int,
    fontSize: Int,
    ink: Color,
    classic: Boolean,
    selectedVerse: String?,
    onTap: () -> Unit,
    onVerseClick: (String) -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val ctx = LocalContext.current
    val vm: MushafPageViewModel = org.koin.androidx.compose.koinViewModel()

    /*  \u0627\u0644\u0640Flow \u064A\u064F\u062A\u0630\u0643\u064E\u0651\u0631 \u0628\u0627\u0644\u0635\u0641\u062D\u0629.
     *
     *  \u0643\u0627\u0646 `vm.pageFlow(page)` \u064A\u064F\u0646\u0627\u062F\u0649 \u062F\u0627\u062E\u0644 \u0627\u0644\u062A\u0623\u0644\u064A\u0641 \u0641\u064A\u064F\u0646\u0634\u0626 Flow \u062C\u062F\u064A\u062F\u0627\u064B \u0641\u064A \u0643\u0644
     *  \u0625\u0639\u0627\u062F\u0629 \u062A\u0631\u0643\u064A\u0628. \u0648`collectAsState` \u0645\u0641\u062A\u0627\u062D\u064F\u0647 \u0647\u0648\u064A\u0651\u0629\u064F \u0627\u0644\u0640Flow \u2014 \u0641\u064A\u064F\u0644\u063A\u064A
     *  \u0627\u0644\u0627\u0634\u062A\u0631\u0627\u0643\u064E \u0648\u064A\u064F\u0639\u064A\u062F \u062A\u0633\u062C\u064A\u0644 \u0627\u0633\u062A\u0639\u0644\u0627\u0645 SQLDelight \u0648\u0645\u0633\u062A\u0645\u0639\u064E\u0647 \u0641\u064A \u0643\u0644 \u0645\u0631\u0651\u0629.  */
    val ayat by remember(page) { vm.pageFlow(page) }.collectAsState(initial = emptyList())

    /** \u0627\u0644\u0628\u0633\u0645\u0644\u0629 \u0645\u0646 \u0627\u0644\u0642\u0627\u0639\u062F\u0629 (\u0627\u0644\u0641\u0627\u062A\u062D\u0629 \u0661) \u0644\u0627 \u0645\u0643\u062A\u0648\u0628\u0629\u064B \u0641\u064A \u0627\u0644\u0643\u0648\u062F \u2014 \u0646\u0635\u064C\u0651 \u0642\u0631\u0622\u0646\u064A\u0651. */
    val basmala by produceState<String?>(null) { value = vm.basmala() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        if (classic) {
            ayat.forEach { a ->
                if (a.ayahNumber == 1) {
                    SurahOpening(
                        name = SurahNames.of(ctx, a.surah),
                        basmala = basmala.takeIf { needsBasmala(a.surah) },
                        ink = ink,
                        accent = rc.gold,
                        fontSize = fontSize,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        /*  \u0627\u0644\u0646\u0642\u0631\u064F \u0648\u0627\u0644\u0636\u063A\u0637\u064F \u0627\u0644\u0645\u0637\u0648\u064E\u0651\u0644 \u2014 \u0644\u0645 \u064A\u0643\u0648\u0646\u0627 \u0645\u0648\u062C\u0648\u062F\u064E\u064A\u0646 \u0647\u0646\u0627.
                         *
                         *  \u0648\u0647\u0630\u0627 \u0647\u0648 \u0627\u0644\u0646\u0645\u0637\u064F \u0627\u0644\u0630\u064A \u064A\u0639\u0645\u0644 \u0639\u0644\u064A\u0647 \u0643\u0644\u064F\u0651 \u062C\u0647\u0627\u0632\u064D \u062C\u062F\u064A\u062F \u0642\u0628\u0644
                         *  \u062A\u0646\u0632\u064A\u0644 \u0627\u0644\u062E\u0637\u0648\u0637. \u0641\u0643\u0627\u0646 \u0627\u0644\u0642\u0627\u0631\u0626 \u0644\u0627 \u064A\u0633\u062A\u0637\u064A\u0639 \u0641\u062A\u062D\u064E \u062A\u0641\u0633\u064A\u0631\u064D
                         *  \u0648\u0644\u0627 \u0646\u0633\u062E\u064E \u0622\u064A\u0629\u064D \u0648\u0644\u0627 \u0648\u0636\u0639\u064E \u0639\u0644\u0627\u0645\u0629 \u2014 **\u0648\u0644\u0627 \u0633\u0628\u064A\u0644 \u0644\u0625\u0646\u0634\u0627\u0621
                         *  \u0639\u0644\u0627\u0645\u0629\u064D \u0641\u064A \u0627\u0644\u062A\u0637\u0628\u064A\u0642 \u0643\u0644\u0650\u0651\u0647**\u060C \u0641\u062A\u0628\u0642\u0649 \u0634\u0627\u0634\u0629\u064F \u0627\u0644\u0639\u0644\u0627\u0645\u0627\u062A
                         *  \u0641\u0627\u0631\u063A\u0629\u064B \u0623\u0628\u062F\u0627\u064B. \u0648\u0627\u0644\u062A\u0644\u0645\u064A\u062D\u064F \u0641\u064A \u0623\u0639\u0644\u0649 \u0627\u0644\u0634\u0627\u0634\u0629 \u064A\u0642\u0648\u0644 \u0644\u0647
                         *  \u00AB\u0627\u0636\u063A\u0637 \u0643\u0644\u0645\u0629\u064B \u0645\u0637\u0648\u0651\u0644\u0627\u064B\u00BB \u0644\u0625\u064A\u0645\u0627\u0621\u0629\u064D \u063A\u064A\u0631\u0650 \u0645\u0648\u0635\u0648\u0644\u0629.  */
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTap,
                            onLongClick = { onVerseClick("${a.surah}:${a.ayahNumber}") },
                        )
                        .background(
                            if (selectedVerse == "${a.surah}:${a.ayahNumber}")
                                rc.gold.copy(alpha = 0.10f) else Color.Transparent
                        )
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape)
                            .border(1.dp, rc.goldLight.copy(alpha = 0.55f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(a.ayahNumber.localized(ar), style = RafiqType.caption, color = rc.gold)
                    }
                    Spacer(Modifier.width(11.dp))
                    Text(
                        a.textUthmani,
                        fontFamily = QuranFamily,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 2.3f).sp,
                        color = ink,
                        modifier = Modifier.weight(1f),
                    )
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
            }
        } else {
            /*  \u0627\u0644\u0646\u0635\u064F\u0651 \u0627\u0644\u0645\u062A\u0651\u0635\u0644: \u0645\u062F\u0649 \u0643\u0644\u0650\u0651 \u0622\u064A\u0629\u064D \u064A\u064F\u0633\u062C\u064E\u0651\u0644 \u0623\u062B\u0646\u0627\u0621 \u0627\u0644\u0628\u0646\u0627\u0621\u060C \u0641\u064A\u064F\u0639\u0631\u0641 \u0645\u0646
             *  \u0645\u0648\u0636\u0639 \u0627\u0644\u0644\u0645\u0633 \u0623\u064A\u064F\u0651 \u0622\u064A\u0629\u064D \u0644\u064F\u0645\u0633\u062A.  */
            val ranges = remember(ayat) { mutableListOf<Triple<Int, Int, String>>() }
            val body = remember(ayat, selectedVerse, ar) {
                ranges.clear()
                buildAnnotatedString {
                    ayat.forEach { a ->
                        val key = "${a.surah}:${a.ayahNumber}"
                        val start = length
                        if (a.ayahNumber == 1 && needsBasmala(a.surah) && basmala != null) {
                            withStyle(SpanStyle(color = rc.gold)) { append("\n$basmala\n") }
                        }
                        if (key == selectedVerse) {
                            withStyle(SpanStyle(background = rc.gold.copy(alpha = 0.16f))) {
                                append(a.textUthmani)
                            }
                        } else {
                            append(a.textUthmani)
                        }
                        withStyle(SpanStyle(color = rc.goldLight)) {
                            append(" \u06DD${a.ayahNumber.localized(true)} ")
                        }
                        ranges += Triple(start, length, key)
                    }
                }
            }

            var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
            Text(
                body,
                fontFamily = QuranFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 2.5f).sp,
                color = ink,
                textAlign = TextAlign.Justify,
                onTextLayout = { layout = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(ranges.size) {
                        detectTapGestures(
                            onTap = { onTap() },
                            onLongPress = { pos ->
                                val offset = layout?.getOffsetForPosition(pos) ?: return@detectTapGestures
                                ranges.firstOrNull { offset in it.first until it.second }
                                    ?.let { onVerseClick(it.third) }
                            },
                        )
                    },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * \u0627\u0644\u0628\u0633\u0645\u0644\u0629\u064F \u062A\u064F\u0631\u0633\u0645 \u0644\u0643\u0644 \u0633\u0648\u0631\u0629\u064D \u0625\u0644\u0651\u0627 \u0627\u062B\u0646\u062A\u064A\u0646.
 *
 * \u0627\u0644\u0641\u0627\u062A\u062D\u0629 \u0628\u0633\u0645\u0644\u062A\u064F\u0647\u0627 \u0622\u064A\u062A\u064F\u0647\u0627 \u0627\u0644\u0623\u0648\u0644\u0649 \u0641\u062A\u064F\u0631\u0633\u0645 \u0645\u0639 \u0627\u0644\u0645\u062A\u0646\u060C \u0648\u0627\u0644\u062A\u0648\u0628\u0629 \u0628\u0644\u0627 \u0628\u0633\u0645\u0644\u0629.
 */
private fun needsBasmala(surah: Int): Boolean = surah != 1 && surah != 9

/** \u0644\u0648\u062D\u064F \u0627\u0644\u0633\u0648\u0631\u0629 \u0641\u064A \u0646\u0645\u0637\u064E\u064A \u0627\u0644\u0646\u0635\u0651 \u2014 \u0627\u0633\u0645\u064C \u062B\u0645\u0651 \u0628\u0633\u0645\u0644\u0629\u060C \u0643\u0645\u0627 \u0641\u064A \u0627\u0644\u0648\u0631\u0642\u0629. */
@Composable
private fun SurahOpening(
    name: String,
    basmala: String?,
    ink: Color,
    accent: Color,
    fontSize: Int,
) {
    Spacer(Modifier.height(18.dp))
    Text(
        name,
        fontFamily = NaskhFamily,
        fontSize = (fontSize + 2).sp,
        color = accent,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    if (basmala != null) {
        Spacer(Modifier.height(6.dp))
        Text(
            basmala,
            fontFamily = QuranFamily,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 2f).sp,
            color = ink.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(12.dp))
}

/* ── هامشُ الورقة ────────────────────────────────────────────────

   في المصحف المطبوع يحمل الهامشُ اسمَ السورة والجزءَ في أعلاه ورقمَ
   الصفحة في أسفل وسطه — بحبرٍ أخفَّ من المتن، فيُقرأ حين يُطلب لا حين
   يُقرأ القرآن. وخطُّه نسخٌ لا خطُّ الواجهة: هو من عالم الكتاب.

   والهامشُ يميل إلى الطرف الخارجيّ للورقة كما في الكتاب المجلَّد،
   والفرديّةُ يسارَ الفتحة فخارجُها عن يسارها.
──────────────────────────────────────────────────────────────── */

/*  أسماءُ الأجزاء الثلاثين نُقلت إلى `arrays.xml`.
 *
 *  العربيةُ تقول «الجُزْءُ الحادي والعشرون» بترتيبٍ منطوق، والإنجليزيةُ
 *  تقول «Juz 21» برقم. والقائمةُ المكتوبةُ بالكود لا تعرف إلّا واحدةً
 *  منهما.
 */

@Composable
private fun PageMargin(surah: String, juz: Int, odd: Boolean, ink: Color) {
    val faint = ink.copy(alpha = 0.52f)
    val juzNames = androidx.compose.ui.res.stringArrayResource(R.array.juz_names)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = if (odd) 16.dp else 28.dp,
                end = if (odd) 28.dp else 16.dp,
                top = 12.dp,
                bottom = 2.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (surah.isBlank()) "" else stringResource(R.string.mushaf_surah_label, surah),
            fontFamily = NaskhFamily,
            fontSize = 13.sp,
            color = faint,
            maxLines = 1,
        )
        Text(
            juzNames.getOrNull(juz)?.takeIf { it.isNotEmpty() }
                ?.let { stringResource(R.string.mushaf_juz_label, it) }.orEmpty(),
            fontFamily = NaskhFamily,
            fontSize = 13.sp,
            color = ink.copy(alpha = 0.40f),
            maxLines = 1,
        )
    }
}

@Composable
private fun PageFoot(page: Int, hizb: Int, ar: Boolean, ink: Color, onJump: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .width(34.dp)
                .height(1.dp)
                .background(ink.copy(alpha = 0.18f)),
        )
        Spacer(Modifier.height(7.dp))
        Row(
            Modifier
                .clip(CircleShape)
                // رقمُ الصفحة صار مدخلاً: ضغطةٌ عليه تفتح الانتقال.
                .clickable(onClick = onJump)
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "$page".localizedDigits(ar),
                fontFamily = NaskhFamily,
                fontSize = 13.sp,
                color = ink.copy(alpha = 0.52f),
            )
            /*  الحزبُ لم يكن يُعرض في التطبيق قطّ.
             *
             *  كان `MushafFooter` يعرضه — وهي دالّةٌ مكتوبةٌ كاملةً لا
             *  يناديها أحد. والحزبُ في التخطيط صحيحٌ (١–٢٤٠) ومقروءٌ منذ
             *  البداية ولا يُقرأ.  */
            if (hizb > 0) {
                Text(
                    stringResource(R.string.mushaf_hizb, ((hizb - 1) / 4 + 1).localized(ar)),
                    fontFamily = NaskhFamily,
                    fontSize = 13.sp,
                    color = ink.copy(alpha = 0.38f),
                )
            }
        }
    }
}

/* ── تلميحُ اللمس ────────────────────────────────────────────────

   الضغطةُ المطوّلةُ لا يكتشفها أحدٌ بلا قول، والنقرةُ صارت للأدوات لأنّ
   الورقةَ كلَّها كلماتٌ قابلةٌ للمس — فلو فتحت النقرةُ الآيةَ لما بقي في
   الصفحة موضعٌ يُلمس. فيُقال مرّةً واحدة، ثمّ لا يعود.
──────────────────────────────────────────────────────────────── */

@Composable
private fun HintBar(ink: Color, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
            .background(rc.tintGold)
            .clickable(onClick = onDismiss)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.mushaf_hint),
            style = RafiqType.caption,
            color = ink.copy(alpha = 0.82f),
            modifier = Modifier.weight(1f),
        )
        Text(stringResource(R.string.action_got_it), style = RafiqType.label, color = rc.emerald)
    }
}

/* ── الأدواتُ التي تذوب ─────────────────────────────────────────── */

@Composable
private fun ToolBar(
    visible: Boolean,
    paper: Color,
    ink: Color,
    accent: Color,
    onBack: () -> Unit,
    onList: () -> Unit,
    onSearch: () -> Unit,
    onMarks: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -it / 3 },
        exit = fadeOut() + slideOutVertically { -it / 3 },
        modifier = modifier,
    ) {
        /*  مِسطرةٌ تطفو فوق الورقة، لا شريطٌ يقتطع منها.
         *
         *  كان يملأ العرضَ بخلفيّةٍ صمّاء تلتصق بحافّة الشاشة، فيبدو
         *  جزءاً من النافذة لا ضيفاً على الصفحة. وصار لوحاً مستديراً
         *  بحافّةٍ ذهبيةٍ خفيفة يترك الورقةَ تُرى من حوله — فيُقرأ
         *  «شيءٌ وُضع على المصحف» ويُرفع.
         *
         *  ولونُه لونُ الورقة نفسِها، فيتبع الليلَ والنهار ولا يأتي
         *  ببياضِ نظامٍ غريبٍ فوق ورقةٍ داكنة. */
        Box(Modifier.displayCutoutPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    //  ظلٌّ خفيف: ورقةٌ وُضعت على الصفحة، لا بطاقةٌ تطفو في فراغ.
                    .shadow(6.dp, RoundedCornerShape(26.dp), clip = false)
                    .clip(RoundedCornerShape(26.dp))
                    .background(paper)
                    .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconDot(RIcon.ArrowRight, ink, onBack)
                Spacer(Modifier.weight(1f))
                IconDot(RIcon.Book, ink, onList)
                IconDot(RIcon.Search, ink, onSearch)
                IconDot(RIcon.Bookmark, ink, onMarks)
                IconDot(RIcon.Settings, ink, onSettings)
            }
        }
    }
}

/* ── الشريطان ──────────────────────────────────────────────────── */


@Composable
private fun Chip(text: String, accent: Boolean, ink: Color) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .heightIn(min = 34.dp)
            .clip(CircleShape)
            .background(ink.copy(alpha = if (accent) 0.07f else 0.05f))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (accent) RafiqType.label else RafiqType.bodyS,
            color = if (accent) ink else ink.copy(alpha = 0.78f),
            maxLines = 1,
        )
    }
}

@Composable
private fun IconDot(icon: RIcon, ink: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { RafiqIcon(icon, 18.dp, ink.copy(alpha = 0.72f)) }
}


/* ── ورقةُ الإعدادات ───────────────────────────────────────────── */

@Composable
private fun SettingsSheet(
    mode: MushafMode,
    fontSize: Int,
    /*  الورقةُ المصحفية مقاسُها يُشتقّ من عرض الشاشة لتستوي الأسطرُ كما
        في المصحف — فلا معنى لمقياسٍ يدويٍّ معها. */
    sizeApplies: Boolean,
    onMode: (MushafMode) -> Unit,
    onSize: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    Box(
        Modifier.fillMaxSize().background(Color(0x66101A14)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(rc.bg)
                .clickable(enabled = false) {}
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                Modifier.padding(top = 10.dp, bottom = 12.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(34.dp).height(4.dp)
                    .clip(RoundedCornerShape(4.dp)).background(rc.divider),
            )
            Text(stringResource(R.string.mushaf_mode), style = RafiqType.titleM, color = rc.ink)
            Spacer(Modifier.height(9.dp))

            //  لا نمطَ مقفولاً بعد اليوم: الخطوطُ مشحونةٌ فكلُّها جاهز.
            MushafMode.entries.forEach { m ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 7.dp)
                        .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
                        .background(if (m == mode) rc.emeraldPastel else rc.card)
                        .border(
                            1.dp,
                            if (m == mode) rc.emerald.copy(alpha = 0.30f) else rc.divider,
                            RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp),
                        )
                        .clickable { onMode(m) }
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(m.label), style = RafiqType.label,
                            color = if (m == mode) rc.emerald else rc.ink)
                        Text(
                            stringResource(m.note),
                            style = RafiqType.caption, color = rc.inkMed,
                        )
                    }
                    if (m == mode) RafiqIcon(RIcon.Check, 17.dp, rc.emerald)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.mushaf_font_size), style = RafiqType.titleM, color = rc.ink)
            if (!sizeApplies) {
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.mushaf_size_note),
                    style = RafiqType.caption, color = rc.inkMed,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(18, 22, 26, 30).forEach { s ->
                    Box(
                        Modifier
                            .weight(1f).heightIn(min = 46.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(if (s == fontSize) rc.emerald else Color.Transparent)
                            .border(1.dp, if (s == fontSize) rc.emerald else rc.divider, RoundedCornerShape(11.dp))
                            .clickable { onSize(s) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.localized(ar), style = RafiqType.label,
                            color = if (s == fontSize) rc.onEmerald else rc.ink)
                    }
                }
            }

            /*  حُذف من هنا قسمُ التنزيل كلُّه — شريطُ التقدّم وزرُّ
             *  «نزّل» وسطرُ «مُنزَّل ٩٠ م.ب» وزرُّ الحذف.
             *
             *  الخطوطُ في الحزمة، فلا شيءَ يُنزَّل ولا شيءَ يُحذف. وإعدادٌ
             *  يعرض حالةَ شيءٍ لا يقع أسوأُ من إعدادٍ ناقص. */
            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ── طلبُ التنزيل — مرّةً واحدةً أوّلَ فتح ───────────────────────

   الصفحةُ المصحفية هي المقصودُ من هذه الشاشة، فلا يُخبَّأ ما يجعلها
   تعمل في ورقة إعدادات. ويُسأل مرّةً: من قَبِل عمل المصحفُ كما في
   الورقة، ومن أبى بقيت الصفحةُ المضبوطةُ تعمل دون إنترنت.
──────────────────────────────────────────────────────────────── */

/* ── الانتقالُ إلى صفحة ─────────────────────────────────────────

   لم يكن في المصحف سبيلٌ إلى صفحةٍ بعينها: ٦٠٤ صفحةً، والوصولُ إلى
   الخمسمئة من الأولى أربعُ مئةٍ وتسعٌ وتسعون سحبة. والقائمةُ تفتح على
   أوّل السورة لا على صفحةٍ يذكرها القارئ.

   ثلاثةُ مداخل: رقمُ الصفحة، والجزءُ، والحزب — وهي ما يذكره الحافظ.
──────────────────────────────────────────────────────────────── */

@Composable
private fun JumpSheet(
    ar: Boolean,
    onGo: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    var text by remember { mutableStateOf("") }

    /** أوّلُ صفحةٍ من كل جزء في المصحف المدنيّ. */
    val juzFirstPage = remember {
        listOf(1, 22, 42, 62, 82, 102, 121, 142, 162, 182, 201, 222, 242, 262, 282,
               302, 322, 342, 362, 382, 402, 422, 442, 462, 482, 502, 522, 542, 562, 582)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onDismiss() },
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(rc.bg)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { /* يبتلع النقر فلا يُغلق */ }
                .padding(20.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                stringResource(R.string.mushaf_jump_title),
                style = RafiqType.titleM,
                color = rc.ink,
            )
            Spacer(Modifier.height(14.dp))

            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() }.take(3) },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.mushaf_jump_hint), color = rc.inkMed) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))

            val target = text.toIntOrNull()
            val valid = target != null && target in 1..604
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (valid) rc.emerald else rc.divider)
                    .clickable(enabled = valid) { onGo(target!!) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.mushaf_jump_go),
                    color = if (valid) rc.onEmerald else rc.inkMed,
                    style = RafiqType.body,
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.mushaf_jump_juz),
                style = RafiqType.caption,
                color = rc.inkMed,
            )
            Spacer(Modifier.height(8.dp))
            /*  ثلاثون جزءاً في شبكةٍ من ستّة أعمدة — تُدرَك بلمحة، وأسرعُ
                من كتابة رقمٍ لمن يعرف جزأه.  */
            juzFirstPage.chunked(6).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { first ->
                        val juz = juzFirstPage.indexOf(first) + 1
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(rc.card)
                                .clickable { onGo(first) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(juz.localized(ar), style = RafiqType.bodyS, color = rc.ink)
                        }
                    }
                    repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

/* ── شريطُ الموضع ───────────────────────────────────────────────────

   المرجعُ الذي أراني إيّاه صاحبُ التطبيق يضع هنا شريطَ أرقامٍ ودائرتَي
   صوتٍ وتمرير. أخذتُ منه المبدأ — أدواتٌ تطفو ولا تُزحزح الورقة —
   وتركتُ زرَّيه: لا مشغّلَ صوتٍ في هذا التطبيق، ووعدٌ بما لا وجود له
   أسوأُ من نقصٍ صريح.

   وما يُعرض بنيةُ المصحف لا ترقيمٌ عامّ: السورةُ والجزءُ والحزب — بها
   يعرف القارئ مكانَه. والمنزلقُ علامتُه ۞، رمزُ ربع الحزب في المصحف
   المطبوع، لا نقطةً من مكتبة.
──────────────────────────────────────────────────────────────────── */

@Composable
private fun PageRail(
    visible: Boolean,
    page: Int,
    surah: String,
    juz: Int,
    hizb: Int,
    paper: Color,
    ink: Color,
    accent: Color,
    ar: Boolean,
    onGo: (Int) -> Unit,
    onJump: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 3 },
        exit = fadeOut() + slideOutVertically { it / 3 },
        modifier = modifier,
    ) {
        Column(
            Modifier
                .displayCutoutPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                .clip(RoundedCornerShape(24.dp))
                .background(paper)
                .border(1.dp, accent.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    surah,
                    fontFamily = NaskhFamily,
                    fontSize = 15.sp,
                    // العربيةُ بلا حروفٍ كبيرة، فتفقد ثقلَها البصريّ —
                    // والسطرُ الواحد يحتاج ارتفاعاً ١٫٧ لا ١٫٢.
                    lineHeight = 26.sp,
                    color = ink.copy(alpha = 0.92f),
                )
                Spacer(Modifier.weight(1f))
                if (juz > 0) {
                    Text(
                        stringResource(R.string.mushaf_juz, juz.localized(ar)),
                        fontFamily = NaskhFamily,
                        fontSize = 13.sp,
                        lineHeight = 23.sp,
                        //  ٠٫٥ تعطي ٣٫٢٦:١ على ورق النهار — دون ٤٫٥
                        //  المطلوبة لهذا المقاس. و٠٫٦٢ تعطي ٤٫٧٠:١.
                        color = ink.copy(alpha = 0.62f),
                    )
                }
                if (hizb > 0) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.mushaf_hizb, ((hizb - 1) / 4 + 1).localized(ar)),
                        fontFamily = NaskhFamily,
                        fontSize = 13.sp,
                        lineHeight = 23.sp,
                        color = ink.copy(alpha = 0.62f),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            PageSlider(page = page, ink = ink, accent = accent, onGo = onGo)
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth().clip(CircleShape).clickable(onClick = onJump),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.mushaf_page_of, page.localized(ar), 604.localized(ar)),
                    fontFamily = NaskhFamily,
                    fontSize = 13.sp,
                    lineHeight = 23.sp,
                    color = accent,
                )
            }
        }
    }
}

/**
 * منزلقُ الصفحات — ۞ يمشي على خيط.
 *
 * ولا `Slider` من Material: ذاك يأتي بلونه ومقاسه وحلقته، ولا يعرف أنّ
 * هذه ورقةُ مصحف. وهنا خيطٌ رفيعٌ وعلامةُ ربعِ الحزب، ولا شيءَ غيرهما.
 *
 * والاتّجاه: الورقةُ تُقلب من اليمين، فالصفحةُ الأولى عند اليمين — وهو
 * ما يفعله [LayoutDirection.Rtl] تلقائياً في `offset`، لكنّ حسابَ
 * الكسر من إحداثيّ اللمس يحتاج القلبَ صراحةً.
 */
/**
 * ‏`U+06DE ARABIC START OF RUB EL HIZB` — العلامةُ التي تُوضع في هامش
 * المصحف كلَّ ربعِ حزب.
 *
 * وتُكتب بنقطتها الرمزية لا بحرفها: هي **رمزٌ** لا نصُّ واجهةٍ يُترجَم،
 * فلا محلَّ لها في `strings.xml` ولا في عدّ النصوص العربية.
 */
private const val RUB_EL_HIZB = "\u06DE"

@Composable
private fun PageSlider(page: Int, ink: Color, accent: Color, onGo: (Int) -> Unit) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var dragging by remember { mutableStateOf<Float?>(null) }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            // ٤٨ نقطةً هدفَ لمسٍ مهما رقّ الخيط المرسوم.
            .height(44.dp),
    ) {
        //  يُلتقط من نطاق BoxWithConstraints مرّةً: داخل `with(Density)`
        //  يضيع المستقبِلُ الضمنيّ فلا يُقرأ `maxWidth`.
        val railWidth = maxWidth
        val widthPx = with(LocalDensity.current) { railWidth.toPx() }
        fun pageAt(x: Float): Int {
            val f = (x / widthPx).coerceIn(0f, 1f)
            val fraction = if (rtl) 1f - f else f
            return (fraction * 603f).toInt() + 1
        }

        val shown = dragging?.let { pageAt(it) } ?: page
        val fraction = (shown - 1) / 603f

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(widthPx, rtl) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragging = it.x },
                        onDragEnd = { dragging?.let { x -> onGo(pageAt(x)) }; dragging = null },
                        onDragCancel = { dragging = null },
                    ) { change, _ -> dragging = change.position.x }
                },
            contentAlignment = Alignment.Center,
        ) {
            // الخيط
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .clip(CircleShape)
                    .background(ink.copy(alpha = 0.14f)),
            )
            // الجزءُ المقطوع — من أوّل المصحف إلى موضعك
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0.004f, 1f))
                    .height(1.5.dp)
                    .align(if (rtl) Alignment.CenterEnd else Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.55f)),
            )
            // ۞ — علامةُ ربع الحزب، وهي هنا مؤشّرُ الموضع
            Text(
                RUB_EL_HIZB,
                fontFamily = NaskhFamily,
                fontSize = 20.sp,
                color = accent,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (railWidth - 20.dp) * if (rtl) 1f - fraction else fraction),
            )
        }
    }
}
