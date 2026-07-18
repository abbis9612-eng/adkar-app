package app.rafiqaldhikr.ui.screens.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.AyahInfo
import app.rafiqaldhikr.ui.components.IcoBook
import app.rafiqaldhikr.ui.components.IcoBookmark
import app.rafiqaldhikr.ui.components.IcoCopy
import app.rafiqaldhikr.ui.components.IcoShare
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.QuranFamily
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.toEasternArabic
import org.koin.androidx.compose.koinViewModel

private const val BISMILLAH = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"

/**
 * المصحف — يُقلب صفحة صفحة (604 صفحات) مثل المصحف المطبوع:
 * كل صفحة ورقة بإطار مذهّب مزدوج، رؤوس سور مزخرفة عند بداية كل سورة،
 * والسحب يمين/يسار يقلب الورق عبر كامل المصحف.
 * الضغط على أي آية يفتح خياراتها (تفسير، علامة، نسخ، مشاركة).
 */
@Composable
fun QuranReadingScreen(
    surahNumber:   Int,
    navController: NavHostController,
    viewModel:     QuranReadingViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsVM: app.rafiqaldhikr.ui.screens.settings.SettingsViewModel = koinViewModel()
    val fontScale by settingsVM.fontScale.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    var selectedAyah by remember { mutableStateOf<AyahInfo?>(null) }

    LaunchedEffect(surahNumber) { viewModel.loadSurah(surahNumber) }

    val startPage = state.surah?.pageStart ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        if (state.isLoading || startPage == 0) {
            LoadingState()
        } else {
            val pagerState = rememberPagerState(
                initialPage = startPage - 1,
                pageCount = { QuranReadingViewModel.TOTAL_PAGES }
            )
            val currentPage = pagerState.currentPage + 1
            val currentAyahs = state.pages[currentPage]

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                // ═══ الترويسة تتبع الصفحة الحالية ═══
                val headerSurah = currentAyahs?.firstOrNull()?.let { state.surahByNumber[it.surah] }
                RafiqTopBar(
                    title    = headerSurah?.nameAr ?: state.surah?.nameAr ?: "المصحف",
                    subtitle = currentAyahs?.firstOrNull()?.let {
                        "الجزء ${it.juz.localized(LocalArabicNumerals.current)} · صفحة ${currentPage.localized(LocalArabicNumerals.current)}"
                    },
                    onBack   = { navController.popBackStack() },
                    actions  = {
                        FontSizeButton("-") { settingsVM.setFontScale((fontScale - 0.1f).coerceAtLeast(0.8f)) }
                        FontSizeButton("+") { settingsVM.setFontScale((fontScale + 0.1f).coerceAtMost(1.5f)) }
                    },
                )

                // ═══ صفحات المصحف ═══
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    beyondViewportPageCount = 1,
                ) { pageIndex ->
                    val page = pageIndex + 1
                    LaunchedEffect(page) { viewModel.loadPage(page) }
                    MushafPage(
                        page          = page,
                        ayahs         = state.pages[page],
                        surahByNumber = state.surahByNumber,
                        bookmarks     = state.bookmarks,
                        selectedAyah  = selectedAyah,
                        fontScale     = fontScale,
                        onAyahTap     = { selectedAyah = it },
                    )
                }
            }

            // حفظ آخر موضع قراءة عند المغادرة
            DisposableEffect(Unit) {
                onDispose {
                    val ayahs = state.pages[pagerState.currentPage + 1]
                    val firstAyah = ayahs?.firstOrNull()
                    viewModel.savePosition(
                        surah = firstAyah?.surah ?: surahNumber,
                        ayah  = firstAyah?.ayahNumber ?: 1,
                        page  = pagerState.currentPage + 1,
                        scrollY = 0f
                    )
                }
            }
        }
    }

    // ═══ خيارات الآية المختارة ═══
    selectedAyah?.let { ayah ->
        AyahActionsSheet(
            ayah         = ayah,
            isBookmarked = (ayah.surah * 1000 + ayah.ayahNumber).toLong() in state.bookmarks,
            onTafsir     = { viewModel.showTafsir(ayah); selectedAyah = null },
            onBookmark   = {
                viewModel.toggleBookmark(ayah.surah, ayah.ayahNumber, ayah.page)
                selectedAyah = null
            },
            onDismiss    = { selectedAyah = null }
        )
    }

    state.tafsir?.let { tafsir ->
        TafsirSheet(
            surahNumber = tafsir.ayah.surah,
            ayahNumber  = tafsir.ayah.ayahNumber,
            ayahText    = tafsir.ayah.textUthmani,
            tafsirText  = tafsir.tafsirText,
            onDismiss   = { viewModel.dismissTafsir() }
        )
    }
}

/* ══════════════════════════════════════════════════════════════
   ورقة مصحف واحدة — إطار مذهّب، رؤوس سور، نص مجرى، رقم صفحة
══════════════════════════════════════════════════════════════ */

@Composable
private fun MushafPage(
    page:          Int,
    ayahs:         List<AyahInfo>?,
    surahByNumber: Map<Int, app.rafiq.domain.model.SurahInfo>,
    bookmarks:     Set<Long>,
    selectedAyah:  AyahInfo?,
    fontScale:     Float,
    onAyahTap:     (AyahInfo) -> Unit,
) {
    val rc = LocalRafiqColors.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(3.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(rc.card)
            .border(2.dp, rc.gold.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(5.dp)
            .border(1.dp, rc.gold.copy(alpha = 0.30f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (ayahs == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = rc.gold, strokeWidth = 2.dp)
            }
            return@Column
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // تقسيم آيات الصفحة إلى مقاطع عند بداية كل سورة
            val segments = remember(ayahs) { segmentBySurahStart(ayahs) }
            segments.forEach { segment ->
                val first = segment.first()
                if (first.ayahNumber == 1) {
                    SurahHeaderBand(surahByNumber[first.surah]?.nameAr ?: "")
                    if (first.surah != 1 && first.surah != 9) {
                        Text(
                            text       = BISMILLAH,
                            fontSize   = (24 * fontScale).sp,
                            fontFamily = QuranFamily,
                            fontWeight = FontWeight.Medium,
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            color      = rc.emerald
                        )
                    }
                }
                MushafText(
                    ayahs        = segment,
                    bookmarks    = bookmarks,
                    selectedAyah = selectedAyah,
                    fontScale    = fontScale,
                    onAyahTap    = onAyahTap
                )
            }
        }

        // ═══ رقم الصفحة في حلية أسفل الورقة ═══
        Row(
            Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(rc.gold.copy(alpha = 0.2f)))
            Box(
                Modifier
                    .padding(horizontal = 10.dp)
                    .clip(CircleShape)
                    .background(rc.accentGoldBg)
                    .border(1.dp, rc.gold.copy(alpha = 0.4f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 3.dp)
            ) {
                Text(
                    page.toEasternArabic(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.accentGold
                )
            }
            Box(Modifier.weight(1f).height(1.dp).background(rc.gold.copy(alpha = 0.2f)))
        }
    }
}

/** يقسم آيات الصفحة إلى مقاطع تبدأ كل منها ببداية سورة (إن وُجدت) */
private fun segmentBySurahStart(ayahs: List<AyahInfo>): List<List<AyahInfo>> {
    val segments = mutableListOf<MutableList<AyahInfo>>()
    ayahs.forEach { ayah ->
        if (segments.isEmpty() || ayah.ayahNumber == 1) {
            segments.add(mutableListOf(ayah))
        } else {
            segments.last().add(ayah)
        }
    }
    return segments
}

/* شريط اسم السورة المزخرف — مثل رأس السورة في المصحف المطبوع */
@Composable
private fun SurahHeaderBand(name: String) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rc.emeraldPastel)
            .border(1.dp, rc.gold.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("۞", fontSize = 16.sp, color = rc.gold)
            Text(
                "سُورَةُ $name",
                fontSize = 18.sp,
                fontFamily = QuranFamily,
                fontWeight = FontWeight.Bold,
                color = rc.emerald
            )
            Text("۞", fontSize = 16.sp, color = rc.gold)
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   نص المصحف المتصل — آيات مجراة بعلامات ذهبية وضغط لكل آية
══════════════════════════════════════════════════════════════ */

@Composable
private fun MushafText(
    ayahs:        List<AyahInfo>,
    bookmarks:    Set<Long>,
    selectedAyah: AyahInfo?,
    fontScale:    Float,
    onAyahTap:    (AyahInfo) -> Unit,
) {
    val rc = LocalRafiqColors.current

    val annotated: AnnotatedString = remember(ayahs, bookmarks, selectedAyah, rc, fontScale) {
        buildAnnotatedString {
            ayahs.forEach { ayah ->
                val start = length
                append(ayah.textUthmani)
                withStyle(
                    SpanStyle(
                        color = rc.gold,
                        fontSize = (20 * fontScale).sp,
                    )
                ) {
                    append(" ﴿${ayah.ayahNumber.toEasternArabic()}﴾ ")
                }
                val end = length
                addStringAnnotation("ayah", "${ayah.surah}:${ayah.ayahNumber}", start, end)
                val bookId = (ayah.surah * 1000 + ayah.ayahNumber).toLong()
                when {
                    selectedAyah?.surah == ayah.surah && selectedAyah.ayahNumber == ayah.ayahNumber ->
                        addStyle(SpanStyle(background = rc.gold.copy(alpha = 0.20f)), start, end)
                    bookId in bookmarks ->
                        addStyle(SpanStyle(background = rc.emeraldPastel.copy(alpha = 0.55f)), start, end)
                }
            }
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        fontSize = (24 * fontScale).sp,
        fontFamily = QuranFamily,
        fontWeight = FontWeight.Medium,
        lineHeight = (50 * fontScale).sp,
        textAlign = TextAlign.Justify,
        color = rc.ink,
        onTextLayout = { layout = it },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(ayahs, bookmarks) {
                detectTapGestures { pos ->
                    val l = layout ?: return@detectTapGestures
                    val offset = l.getOffsetForPosition(pos)
                    annotated.getStringAnnotations("ayah", offset, offset)
                        .firstOrNull()
                        ?.let { ann ->
                            val (s, a) = ann.item.split(":").map { it.toInt() }
                            ayahs.firstOrNull { it.surah == s && it.ayahNumber == a }
                                ?.let(onAyahTap)
                        }
                }
            }
    )
}

/* ══════════════════════════════════════════════════════════════
   ورقة خيارات الآية — تفسير · علامة · نسخ · مشاركة
══════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahActionsSheet(
    ayah:         AyahInfo,
    isBookmarked: Boolean,
    onTafsir:     () -> Unit,
    onBookmark:   () -> Unit,
    onDismiss:    () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val shareText = "${ayah.textUthmani} ﴿${ayah.ayahNumber.toEasternArabic()}﴾"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor   = rc.bg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = rc.inkLight) }
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(
                shareText,
                fontSize = 21.sp,
                fontFamily = QuranFamily,
                fontWeight = FontWeight.Medium,
                lineHeight = 42.sp,
                color = rc.ink,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AyahAction("تفسير", { s, c -> IcoBook(s, c) }, rc.gold, onTafsir)
                AyahAction(
                    if (isBookmarked) "إزالة العلامة" else "علامة",
                    { s, c -> IcoBookmark(s, c, filled = isBookmarked) },
                    rc.emerald, onBookmark
                )
                AyahAction("نسخ", { s, c -> IcoCopy(s, c) }, rc.inkMed) {
                    clipboard.setText(AnnotatedString(shareText))
                    onDismiss()
                }
                AyahAction("مشاركة", { s, c -> IcoShare(s, c) }, rc.inkMed) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$shareText\n\nعبر تطبيق رفيق الذكر 🌙")
                    }
                    context.startActivity(Intent.createChooser(intent, "مشاركة الآية"))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun AyahAction(
    label:   String,
    icon:    @Composable (androidx.compose.ui.unit.Dp, Color) -> Unit,
    tint:    Color,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(rc.card)
                .border(1.dp, tint.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon(22.dp, tint)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 11.sp, color = rc.inkMed)
    }
}

@Composable
private fun FontSizeButton(symbol: String, onClick: () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
    }
}
