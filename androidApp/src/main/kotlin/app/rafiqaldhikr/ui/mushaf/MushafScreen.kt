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

/* ══════════════════════════════════════════════════════════════
   شاشةُ المصحف — أربعةُ أنماطٍ في مكانٍ واحد

   الصفحةُ هي الوحدة لا السورة: ٦٠٤ صفحةً بترقيم المصحف المدنيّ،
   والانتقالُ بينها سحبٌ أفقيٌّ كما تُقلَب الورقة.

   والأنماطُ الأربعة تختلف في الرسم لا في النصّ: النصُّ واحدٌ من ملفّ
   التطبيق نفسِه، والصفحةُ واحدة، والتفسيرُ واحد. وما يشتريه النمطُ
   المصحفيُّ بالتنزيل شيءٌ واحد: مواضعُ قطع الأسطر.
══════════════════════════════════════════════════════════════ */

@Composable
fun MushafScreen(navController: NavHostController) {
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
    var selected by remember { mutableStateOf<String?>(null) }
    var ready by remember { mutableStateOf(layout?.let { fonts.isReady(it) } ?: false) }
    var progress by remember { mutableStateOf<MushafDownloader.Progress?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    var offer by remember { mutableStateOf(false) }

    /*  الصفحةُ المصحفية هي المقصودُ من الشاشة، لا خيارٌ في ورقة إعدادات.
     *  فيُعرض طلبُ التنزيل أوّلَ فتحٍ مرّةً واحدة — ولا يُلحّ بعدها. */
    LaunchedEffect(Unit) {
        if (!ready && !prefs.askedOnce && layout != null) {
            offer = true
            prefs.askedOnce = true
        }
    }

    val pager = rememberPagerState(initialPage = prefs.lastPage - 1, pageCount = { 604 })
    LaunchedEffect(pager.currentPage) { prefs.lastPage = pager.currentPage + 1 }

    // النمطُ المصحفيُّ بلا خطوطٍ يسقط إلى المضبوطة بدل صفحةٍ فارغة
    val effective = if (mode.needsFonts && !ready) MushafMode.PAGE else mode
    val night = effective == MushafMode.MUSHAF_NIGHT
    val paper = if (night) Color(0xFF15130E) else rc.bg
    val ink = if (night) Color(0xFFE8E1CF) else rc.ink

    Column(
        Modifier.fillMaxSize().background(paper).statusBarsPadding(),
    ) {
        MushafTopBar(
            page = pager.currentPage + 1,
            ink = ink,
            onBack = { navController.popBackStack() },
            onSettings = { sheet = true },
            onList = { navController.navigate(RafiqRoute.QuranList.route) },
        )

        if (mode.needsFonts && !ready && progress == null) {
            MushafBanner(
                onDownload = { offer = true },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            )
        }
        if (progress != null) {
            DownloadStrip(progress!!, Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
        }

        Box(Modifier.weight(1f)) {
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,   // تُقلَب الورقةُ يميناً كما في المصحف
            ) { index ->
                val pageNo = index + 1
                val data = layout?.page(pageNo)
                val family = if (effective.needsFonts) layout?.let { fonts.familyFor(it, pageNo) } else null

                if (effective.needsFonts && data != null && family != null) {
                    MushafPageView(
                        page = data,
                        family = family,
                        fontSize = fontSize.sp,
                        selectedVerse = selected,
                        onVerseClick = { selected = if (selected == it) null else it },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    TextPage(pageNo, fontSize, ink, classic = effective == MushafMode.CLASSIC)
                }
            }
        }

        MushafFooter(pager.currentPage + 1, ar, ink)
    }

    if (offer) {
        OfferDialog(
            onYes = {
                offer = false
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

/* ── الشريطان ──────────────────────────────────────────────────── */

@Composable
private fun MushafTopBar(
    page: Int, ink: Color,
    onBack: () -> Unit, onSettings: () -> Unit, onList: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) { RafiqIcon(RIcon.ChevronLeft, 18.dp, ink.copy(alpha = 0.75f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onList),
                contentAlignment = Alignment.Center,
            ) { RafiqIcon(RIcon.Book, 18.dp, ink.copy(alpha = 0.75f)) }
            Box(
                Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onSettings),
                contentAlignment = Alignment.Center,
            ) { RafiqIcon(RIcon.Settings, 18.dp, ink.copy(alpha = 0.75f)) }
        }
    }
}

@Composable
private fun MushafFooter(page: Int, ar: Boolean, ink: Color) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .clip(CircleShape)
                .background(ink.copy(alpha = 0.06f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(page.localized(ar), style = RafiqType.titleM, color = ink)
        }
    }
}

/* ── ورقةُ الإعدادات ───────────────────────────────────────────── */

@Composable
private fun SettingsSheet(
    mode: MushafMode,
    fontSize: Int,
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
                            "نحو ٨٩ م.ب، مرّةً واحدة. وما دونها يعمل دون إنترنت.",
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
private fun OfferDialog(onYes: () -> Unit, onNo: () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier.fillMaxSize().background(Color(0x88101A14)).clickable(onClick = onNo),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.88f)
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
            Spacer(Modifier.height(12.dp))
            Text(
                "تحتاج خطوطَ المصحف: نحو ٨٩ م.ب تُنزَّل مرّةً واحدة، ثمّ تعمل دون إنترنت للأبد.",
                style = RafiqType.bodyS, color = rc.inkMed,
            )
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier
                    .fillMaxWidth().heightIn(min = 54.dp)
                    .clip(RoundedCornerShape(6.dp, 6.dp, 20.dp, 6.dp))
                    .background(rc.emerald)
                    .clickable(onClick = onYes),
                contentAlignment = Alignment.Center,
            ) { Text("نزِّلْها الآن", style = RafiqType.titleM, color = rc.onEmerald) }
            Spacer(Modifier.height(9.dp))
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
            Text("نزِّلْ خطوطَ المصحف لتراها كما في الورقة", style = RafiqType.caption, color = rc.inkMed)
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
