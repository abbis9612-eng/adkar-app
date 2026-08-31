package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.graphics.Brush
import app.rafiqaldhikr.ui.theme.NaskhFamily
import app.rafiqaldhikr.ui.utils.localizedDigits

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

    val layout = remember { runCatching { MushafLayout.load(ctx) }.getOrNull() }
    val fonts = remember { MushafFonts(ctx) }

    var mode by remember { mutableStateOf(prefs.mode) }
    var fontSize by remember { mutableIntStateOf(prefs.fontSize) }
    var sheet by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(openVerse.takeIf { it.isNotBlank() }) }
    var ready by remember { mutableStateOf(layout?.let { fonts.isReady(it) } ?: false) }
    var progress by remember { mutableStateOf<MushafDownloader.Progress?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var offer by remember { mutableStateOf(false) }
    /** يُبدَّل كلّما وصل خطٌّ جديد فتُعاد قراءةُ الخطوط. */
    var fontTick by remember { mutableIntStateOf(0) }
    var fetching by remember { mutableStateOf(false) }
    var allowed by remember { mutableStateOf(prefs.fontsAllowed) }

    /*  الصفحةُ المصحفية هي المقصودُ من الشاشة، لا خيارٌ في ورقة إعدادات.
     *  فيُعرض طلبُ التنزيل أوّلَ فتحٍ مرّةً واحدة — ولا يُلحّ بعدها. */
    LaunchedEffect(Unit) {
        if (!ready && !prefs.askedOnce && layout != null) {
            offer = true
            prefs.askedOnce = true
        }
    }

    // صفر يعني: افتح على آخر ما قرأ. وما عداه صفحةٌ طُلبت من القائمة أو البحث.
    val startPage = if (openPage in 1..604) openPage else prefs.lastPage
    val pager = rememberPagerState(initialPage = startPage - 1, pageCount = { 604 })
    LaunchedEffect(pager.currentPage) { prefs.lastPage = pager.currentPage + 1 }

    /*  رأسُ الصفحة يقول أين أنت: السورةُ والجزءُ والحزب. وكانت الشاشةُ
     *  تعرض رقمَ الصفحة وحده — ورقمٌ بلا سورةٍ لا يقول شيئاً لمن يقرأ. */
    /*  خطُّ الصفحة الحاضرة يُجلَب وحدَه — مليونا بايتٍ في ثوانٍ بدل
     *  تسعين ميغابايت. فتظهر الصفحةُ المصحفية من أوّل فتحٍ
     *  لمن أذِن، ويمتلئ الباقي كلّما قلَب ورقة. */
    LaunchedEffect(pager.currentPage, mode, fontTick, allowed) {
        val l = layout ?: return@LaunchedEffect
        if (!mode.needsFonts || !allowed) return@LaunchedEffect
        val need = l.fontsNeeded(pager.currentPage + 1).filterNot { fonts.fileFor(it).exists() }
        if (need.isEmpty()) return@LaunchedEffect
        fetching = true
        val dl = MushafDownloader(ctx)
        val got = need.map { dl.fetchOne(fonts, it) }.all { it }
        fetching = false
        if (got) { fontTick++; ready = fonts.isReady(l) }
    }

    val ctxVm: MushafPageViewModel = org.koin.androidx.compose.koinViewModel()
    val pageAyat by ctxVm.pageFlow(pager.currentPage + 1).collectAsState(initial = emptyList())
    val head = pageAyat.firstOrNull()
    val suraName = head?.let { SurahNames.of(ctx, it.surah) } ?: ""


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
    val night = effective == MushafMode.MUSHAF_NIGHT
    val paper = if (night) Color(0xFF15130E) else rc.bg
    val ink = if (night) Color(0xFFE8E1CF) else rc.ink

    /*  الأدواتُ تذوب.

        ليس في المصحف الورقيّ شريطٌ علويٌّ ولا سفليّ — واسمُ السورة
        والجزءُ ورقمُ الصفحة في الهامش حيث موضعُها. فالشاشةُ ورقةٌ
        خالصة، ولمسةٌ واحدةٌ في متنها تُظهر الأدواتِ وتُخفيها.  */
    var toolsOn by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf(!prefs.hintSeen) }

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
                    .statusBarsPadding()
                    .navigationBarsPadding()
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
                        TextPage(pageNo, fontSize, ink, classic = effective == MushafMode.CLASSIC)
                    }
                }
                PageFoot(pageNo, ar, ink)
            }
        }

        /*  ما لا يُخفى: البلاغُ حين ينقص خطٌّ، وشريطُ التنزيل حين يجري.
            هذان حالُ النظام لا زينتُه، فيبقيان ظاهرين. */
        Column(Modifier.align(Alignment.TopCenter).statusBarsPadding()) {
            if (hint) {
                HintBar(
                    ink = ink,
                    onDismiss = { hint = false; prefs.hintSeen = true },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
            if (mode.needsFonts && !pageFontReady && progress == null && !fetching) {
                MushafBanner(
                    onDownload = { allowed = true; prefs.fontsAllowed = true },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                )
            }
            if (progress != null) {
                DownloadStrip(progress!!, Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }

        ToolBar(
            visible = toolsOn,
            paper = paper,
            ink = ink,
            onBack = { navController.popBackStack() },
            onList = { navController.navigate(RafiqRoute.QuranList.route) },
            onSearch = { navController.navigate(RafiqRoute.QuranSearch.route) },
            onMarks = { navController.navigate(RafiqRoute.QuranBookmarks.route) },
            onSettings = { sheet = true },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        AyahSheet(
            verse = selected,
            page = pager.currentPage + 1,
            night = night,
            onDismiss = { selected = null },
        )
    }

    if (offer) {
        OfferDialog(
            onNow = {
                offer = false
                allowed = true
                prefs.fontsAllowed = true
            },
            onAll = {
                offer = false
                allowed = true
                prefs.fontsAllowed = true
                error = null
                scope.launch {
                    val l = layout ?: return@launch
                    error = MushafDownloader(ctx).download(l, fonts) { progress = it }
                    ready = fonts.isReady(l)
                    progress = null
                }
            },
            onNo = { offer = false },
        )
    }

    if (sheet) {
        SettingsSheet(
            mode = mode,
            fontSize = fontSize,
            sizeApplies = !effective.needsFonts,
            ready = ready,
            downloaded = layout?.let { fonts.downloadedCount(it) } ?: 0,
            totalFonts = layout?.fonts?.size ?: 0,
            sizeMb = fonts.sizeMb(),
            progress = progress,
            error = error,
            onMode = { mode = it; prefs.mode = it },
            onSize = { fontSize = it; prefs.fontSize = it },
            onDownload = {
                error = null
                scope.launch {
                    val l = layout ?: return@launch
                    error = MushafDownloader(ctx).download(l, fonts) { progress = it }
                    ready = fonts.isReady(l)
                    progress = null
                }
            },
            onClear = { fonts.clear(); ready = false },
            onDismiss = { sheet = false },
        )
    }
}

/* ── نمطان بلا تنزيل ───────────────────────────────────────────

   [classic] آيةٌ في فقرةٍ برقمها — أسهلُ للّمس والتفسير والنسخ.
   وغيرُه نصٌّ متّصلٌ مضبوطُ الطرفين كالورقة، والأرقامُ في متنه.
──────────────────────────────────────────────────────────────── */

@Composable
private fun TextPage(page: Int, fontSize: Int, ink: Color, classic: Boolean) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val vm: MushafPageViewModel = org.koin.androidx.compose.koinViewModel()
    val ayat by vm.pageFlow(page).collectAsState(initial = emptyList())

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
    ) {
        Spacer(Modifier.height(10.dp))
        if (classic) {
            ayat.forEach { a ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 7.dp),
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
            val body = buildAnnotatedString {
                ayat.forEach { a ->
                    append(a.textUthmani)
                    withStyle(SpanStyle(color = rc.goldLight)) {
                        append(" \u06DD${a.ayahNumber.localized(true)} ")
                    }
                }
            }
            Text(
                body,
                fontFamily = QuranFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 2.5f).sp,
                color = ink,
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

/* ── هامشُ الورقة ────────────────────────────────────────────────

   في المصحف المطبوع يحمل الهامشُ اسمَ السورة والجزءَ في أعلاه ورقمَ
   الصفحة في أسفل وسطه — بحبرٍ أخفَّ من المتن، فيُقرأ حين يُطلب لا حين
   يُقرأ القرآن. وخطُّه نسخٌ لا خطُّ الواجهة: هو من عالم الكتاب.

   والهامشُ يميل إلى الطرف الخارجيّ للورقة كما في الكتاب المجلَّد،
   والفرديّةُ يسارَ الفتحة فخارجُها عن يسارها.
──────────────────────────────────────────────────────────────── */

private val JUZ_NAMES = listOf(
    "", "الأوّل", "الثاني", "الثالث", "الرابع", "الخامس", "السادس", "السابع",
    "الثامن", "التاسع", "العاشر", "الحادي عشر", "الثاني عشر", "الثالث عشر",
    "الرابع عشر", "الخامس عشر", "السادس عشر", "السابع عشر", "الثامن عشر",
    "التاسع عشر", "العشرون", "الحادي والعشرون", "الثاني والعشرون",
    "الثالث والعشرون", "الرابع والعشرون", "الخامس والعشرون", "السادس والعشرون",
    "السابع والعشرون", "الثامن والعشرون", "التاسع والعشرون", "الثلاثون",
)

@Composable
private fun PageMargin(surah: String, juz: Int, odd: Boolean, ink: Color) {
    val faint = ink.copy(alpha = 0.52f)
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
            if (surah.isBlank()) "" else "سُورَةُ $surah",
            fontFamily = NaskhFamily,
            fontSize = 13.sp,
            color = faint,
            maxLines = 1,
        )
        Text(
            JUZ_NAMES.getOrNull(juz)?.takeIf { it.isNotEmpty() }?.let { "الجُزْءُ $it" }.orEmpty(),
            fontFamily = NaskhFamily,
            fontSize = 13.sp,
            color = ink.copy(alpha = 0.40f),
            maxLines = 1,
        )
    }
}

@Composable
private fun PageFoot(page: Int, ar: Boolean, ink: Color) {
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
        Text(
            "$page".localizedDigits(ar),
            fontFamily = NaskhFamily,
            fontSize = 13.sp,
            color = ink.copy(alpha = 0.52f),
        )
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
            "المسِ الورقةَ لتظهر الأدوات · واضغط كلمةً مطوّلاً لتفتح آيتَها",
            style = RafiqType.caption,
            color = ink.copy(alpha = 0.82f),
            modifier = Modifier.weight(1f),
        )
        Text("فهمت", style = RafiqType.label, color = rc.emerald)
    }
}

/* ── الأدواتُ التي تذوب ─────────────────────────────────────────── */

@Composable
private fun ToolBar(
    visible: Boolean,
    paper: Color,
    ink: Color,
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
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(paper, paper.copy(alpha = 0f))))
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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

/* ── الشريطان ──────────────────────────────────────────────────── */

@Composable
private fun MushafTopBar(
    page: Int, surah: String, juz: Int, ink: Color,
    onBack: () -> Unit, onSettings: () -> Unit, onList: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    Row(
        Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Chip(surah.ifEmpty { "المصحف" }, accent = true, ink = ink)
            Spacer(Modifier.width(6.dp))
            IconDot(RIcon.ChevronLeft, ink, onBack)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconDot(RIcon.Book, ink, onList)
            IconDot(RIcon.Settings, ink, onSettings)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Chip(if (juz > 0) "الجزء ${juz.localized(ar)}" else "…", accent = false, ink = ink)
        }
    }
}

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

@Composable
private fun MushafFooter(page: Int, hizb: Int, surah: String, ar: Boolean, ink: Color) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 7.dp).navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(surah, style = RafiqType.caption, color = ink.copy(alpha = 0.62f), maxLines = 1)
        Box(
            Modifier.clip(CircleShape).background(ink.copy(alpha = 0.06f))
                .padding(horizontal = 15.dp, vertical = 5.dp),
        ) { Text(page.localized(ar), style = RafiqType.titleM, color = ink) }
        Text(
            if (hizb > 0) "الحزب ${((hizb - 1) / 4 + 1).localized(ar)}" else "",
            style = RafiqType.caption, color = ink.copy(alpha = 0.62f), maxLines = 1,
        )
    }
}

/* ── ورقةُ الإعدادات ───────────────────────────────────────────── */

@Composable
private fun SettingsSheet(
    mode: MushafMode,
    fontSize: Int,
    /*  الورقةُ المصحفية مقاسُها يُشتقّ من عرض الشاشة لتستوي الأسطرُ كما
        في المصحف — فلا معنى لمقياسٍ يدويٍّ معها. */
    sizeApplies: Boolean,
    ready: Boolean,
    downloaded: Int,
    totalFonts: Int,
    sizeMb: Double,
    progress: MushafDownloader.Progress?,
    error: String?,
    onMode: (MushafMode) -> Unit,
    onSize: (Int) -> Unit,
    onDownload: () -> Unit,
    onClear: () -> Unit,
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
            Text("طريقةُ العرض", style = RafiqType.titleM, color = rc.ink)
            Spacer(Modifier.height(9.dp))

            MushafMode.entries.forEach { m ->
                val locked = m.needsFonts && !ready
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
                        Text(m.label, style = RafiqType.label,
                            color = if (m == mode) rc.emerald else rc.ink)
                        Text(
                            if (locked) "${m.note} — لم تُنزَّل بعد" else m.note,
                            style = RafiqType.caption, color = rc.inkMed,
                        )
                    }
                    if (m == mode) RafiqIcon(RIcon.Check, 17.dp, rc.emerald)
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("مقاسُ الخطّ", style = RafiqType.titleM, color = rc.ink)
            if (!sizeApplies) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "الورقةُ المصحفيةُ تضبط مقاسَها بنفسها لتستويَ الأسطر — والمقياسُ لِعرضِ النصّ.",
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

            /* ── الصفحةُ المصحفية: تنزيلٌ بإذن ── */
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp, 6.dp, 20.dp, 6.dp))
                    .background(rc.card)
                    .border(1.dp, rc.divider, RoundedCornerShape(6.dp, 6.dp, 20.dp, 6.dp))
                    .padding(14.dp),
            ) {
                Text("الصفحةُ المصحفية", style = RafiqType.titleM, color = rc.emerald)
                Spacer(Modifier.height(4.dp))
                Text(
                    "خمسةَ عشرَ سطراً مطابقةً لمصحف المدينة — الكلمةُ في موضعها " +
                        "والسطرُ يقطع حيث يقطع في الورقة. وتحتاج خطوطاً تُنزَّل مرّةً واحدة.",
                    style = RafiqType.bodyS, color = rc.inkMed,
                )
                Spacer(Modifier.height(10.dp))

                when {
                    progress != null -> {
                        Text(
                            "يُنزَّل ${progress.done.localized(ar)} من ${progress.total.localized(ar)} " +
                                "· ${(progress.bytes / 1_048_576).toInt().localized(ar)} م.ب",
                            style = RafiqType.caption, color = rc.inkMed,
                        )
                        Spacer(Modifier.height(7.dp))
                        LinearProgressIndicator(
                            progress = { progress.done.toFloat() / progress.total },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                            color = rc.emerald, trackColor = rc.divider,
                        )
                    }
                    ready -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RafiqIcon(RIcon.Check, 16.dp, rc.emerald)
                            Spacer(Modifier.width(7.dp))
                            Text(
                                "جاهزة · ${sizeMb.toInt().localized(ar)} م.ب على جهازك",
                                style = RafiqType.bodyS, color = rc.emerald,
                            )
                            Spacer(Modifier.weight(1f))
                            Text("حذف", style = RafiqType.bodyS, color = rc.error,
                                modifier = Modifier.clickable(onClick = onClear))
                        }
                    }
                    else -> {
                        Box(
                            Modifier
                                .fillMaxWidth().heightIn(min = 50.dp)
                                .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
                                .background(rc.emerald)
                                .clickable(onClick = onDownload),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                if (downloaded > 0)
                                    "أكمِلِ التنزيل — ${downloaded.localized(ar)} من ${totalFonts.localized(ar)}"
                                else "نزِّلْ خطوطَ المصحف",
                                style = RafiqType.titleM, color = rc.onEmerald,
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Text(
                            "نحو ٩٠ م.ب، مرّةً واحدة. وما دونها يعمل دون إنترنت.",
                            style = RafiqType.caption, color = rc.inkMed,
                        )
                    }
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, style = RafiqType.bodyS, color = rc.error)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

/* ── طلبُ التنزيل — مرّةً واحدةً أوّلَ فتح ───────────────────────

   الصفحةُ المصحفية هي المقصودُ من هذه الشاشة، فلا يُخبَّأ ما يجعلها
   تعمل في ورقة إعدادات. ويُسأل مرّةً: من قَبِل عمل المصحفُ كما في
   الورقة، ومن أبى بقيت الصفحةُ المضبوطةُ تعمل دون إنترنت.
──────────────────────────────────────────────────────────────── */

@Composable
private fun OfferDialog(onNow: () -> Unit, onAll: () -> Unit, onNo: () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier.fillMaxSize().background(Color(0x88101A14)).clickable(onClick = onNo),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(8.dp, 8.dp, 30.dp, 8.dp))
                .background(rc.card)
                .border(1.dp, rc.cardBorder, RoundedCornerShape(8.dp, 8.dp, 30.dp, 8.dp))
                .clickable(enabled = false) {}
                .padding(20.dp),
        ) {
            Text("الصفحةُ المصحفية", style = RafiqType.hero, color = rc.emerald)
            Spacer(Modifier.height(8.dp))
            Text(
                "خمسةَ عشرَ سطراً مطابقةً لمصحف المدينة — الكلمةُ في موضعها " +
                    "والسطرُ يقطع حيث يقطع في الورقة.",
                style = RafiqType.body, color = rc.inkMed,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth().heightIn(min = 54.dp)
                    .clip(RoundedCornerShape(6.dp, 6.dp, 20.dp, 6.dp))
                    .background(rc.emerald)
                    .clickable(onClick = onNow),
                contentAlignment = Alignment.Center,
            ) { Text("اعرِضْها الآن", style = RafiqType.titleM, color = rc.onEmerald) }
            Spacer(Modifier.height(6.dp))
            Text(
                "يُجلَب خطُّ ما تقرؤه وحدَه — نحو مليونَي بايت، ثوانٍ. " +
                    "ويمتلئ الباقي كلّما قلبتَ ورقة.",
                style = RafiqType.caption, color = rc.inkMed,
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth().heightIn(min = 50.dp)
                    .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
                    .border(1.dp, rc.divider, RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
                    .clickable(onClick = onAll),
                contentAlignment = Alignment.Center,
            ) {
                Text("نزِّلِ المصحفَ كلَّه — ٩٠ م.ب", style = RafiqType.label, color = rc.emerald)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "لقراءةٍ كاملةٍ دون إنترنت",
                style = RafiqType.caption, color = rc.inkMed,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "لاحقاً — واقرأ بالصفحة المضبوطة",
                style = RafiqType.bodyS, color = rc.inkMed,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onNo).padding(vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun MushafBanner(onDownload: () -> Unit, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
            .background(rc.emeraldPastel)
            .border(1.dp, rc.emerald.copy(alpha = 0.22f), RoundedCornerShape(6.dp, 6.dp, 18.dp, 6.dp))
            .clickable(onClick = onDownload)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("هذه الصفحةُ المضبوطة", style = RafiqType.label, color = rc.emerald)
            Text("اضغط لتظهر الصفحةُ كما في الورقة", style = RafiqType.caption, color = rc.inkMed)
        }
        RafiqIcon(RIcon.ChevronLeft, 17.dp, rc.emerald)
    }
}

@Composable
private fun DownloadStrip(p: MushafDownloader.Progress, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    Column(modifier.fillMaxWidth()) {
        Text(
            "يُنزَّل المصحف · ${p.done.localized(ar)} من ${p.total.localized(ar)} " +
                "· ${(p.bytes / 1_048_576).toInt().localized(ar)} م.ب",
            style = RafiqType.caption, color = rc.inkMed,
        )
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { p.done.toFloat() / p.total },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = rc.emerald, trackColor = rc.divider,
        )
    }
}
