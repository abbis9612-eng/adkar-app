package app.rafiqaldhikr.ui.screens.quran

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.QuranFamily
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.toEasternArabic
import org.koin.androidx.compose.koinViewModel

/**
 * شاشة قراءة بأسلوب المصحف: نص متصل مجرى بالعدل (Justify)،
 * فواصل آيات ذهبية بأرقام عربية، وصفحات حقيقية —
 * والضغط على أي آية يفتح خياراتها (تفسير، علامة، نسخ، مشاركة).
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = state.surah?.nameAr ?: "سورة",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = rc.emerald
                    )
                    state.surah?.let { s ->
                        Text(
                            text = (if (s.revelation == "meccan") "مكية" else "مدنية") +
                                " · ${s.ayahCount.toEasternArabic()} آية",
                            fontSize = 12.sp,
                            color = rc.inkMed
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FontSizeButton("-") { settingsVM.setFontScale((fontScale - 0.1f).coerceAtLeast(0.8f)) }
                    FontSizeButton("+") { settingsVM.setFontScale((fontScale + 0.1f).coerceAtMost(1.5f)) }
                    RafiqBackButton(onClick = { navController.popBackStack() })
                }
            }

            when {
                state.isLoading -> LoadingState()
                else -> {
                    val pages = remember(state.ayahs) {
                        state.ayahs.groupBy { it.page }.toSortedMap()
                    }
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // البسملة (ما عدا الفاتحة والتوبة)
                        if (surahNumber != 1 && surahNumber != 9) {
                            item {
                                Text(
                                    text      = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize  = 30.sp,
                                    fontFamily = QuranFamily,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier  = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                    color     = rc.emerald
                                )
                            }
                        }

                        pages.forEach { (page, pageAyahs) ->
                            item(key = "page_$page") {
                                // ═══ صفحة مصحف بإطار مذهّب مزدوج ═══
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                        .shadow(3.dp, RoundedCornerShape(24.dp))
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(rc.card)
                                        .border(2.dp, rc.gold.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                                        .padding(5.dp)
                                        .border(1.dp, rc.gold.copy(alpha = 0.30f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 16.dp, vertical = 18.dp)
                                ) {
                                    MushafText(
                                        ayahs        = pageAyahs,
                                        bookmarks    = state.bookmarks,
                                        selectedAyah = selectedAyah?.ayahNumber,
                                        fontScale    = fontScale,
                                        onAyahTap    = { selectedAyah = it }
                                    )
                                }

                                // ═══ رقم الصفحة داخل حلية ═══
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                                            .padding(horizontal = 14.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            "${page.toEasternArabic()} · جزء ${pageAyahs.first().juz.toEasternArabic()}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = rc.accentGold
                                        )
                                    }
                                    Box(Modifier.weight(1f).height(1.dp).background(rc.gold.copy(alpha = 0.2f)))
                                }
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // ═══ خيارات الآية المختارة ═══
    selectedAyah?.let { ayah ->
        AyahActionsSheet(
            ayah        = ayah,
            isBookmarked = ayah.ayahNumber in state.bookmarks,
            onTafsir    = { viewModel.showTafsir(ayah); selectedAyah = null },
            onBookmark  = {
                viewModel.toggleBookmark(surahNumber, ayah.ayahNumber, ayah.page)
                selectedAyah = null
            },
            onDismiss   = { selectedAyah = null }
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

    // Save position on leaving
    DisposableEffect(Unit) {
        onDispose {
            val s = state
            if (s.ayahs.isNotEmpty()) {
                viewModel.savePosition(surahNumber, 1, s.ayahs.firstOrNull()?.page ?: 1, 0f)
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   نص المصحف المتصل — آيات مجراة بعلامات ذهبية وضغط لكل آية
══════════════════════════════════════════════════════════════ */

@Composable
private fun MushafText(
    ayahs:        List<AyahInfo>,
    bookmarks:    Set<Int>,
    selectedAyah: Int?,
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
                    androidx.compose.ui.text.SpanStyle(
                        color = rc.gold,
                        fontSize = (22 * fontScale).sp,
                    )
                ) {
                    append(" ﴿${ayah.ayahNumber.toEasternArabic()}﴾ ")
                }
                val end = length
                addStringAnnotation("ayah", ayah.ayahNumber.toString(), start, end)
                when {
                    ayah.ayahNumber == selectedAyah ->
                        addStyle(SpanStyle(background = rc.gold.copy(alpha = 0.20f)), start, end)
                    ayah.ayahNumber in bookmarks ->
                        addStyle(SpanStyle(background = rc.emeraldPastel.copy(alpha = 0.55f)), start, end)
                }
            }
        }
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        fontSize = (27 * fontScale).sp,
        fontFamily = QuranFamily,
        fontWeight = FontWeight.Medium,
        lineHeight = (56 * fontScale).sp,
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
                            ayahs.firstOrNull { it.ayahNumber.toString() == ann.item }
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
                AyahAction("تفسير", Icons.AutoMirrored.Filled.MenuBook, rc.gold, onTafsir)
                AyahAction(
                    if (isBookmarked) "إزالة العلامة" else "علامة",
                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    rc.emerald, onBookmark
                )
                AyahAction("نسخ", Icons.Default.ContentCopy, rc.inkMed) {
                    clipboard.setText(AnnotatedString(shareText))
                    onDismiss()
                }
                AyahAction("مشاركة", Icons.Default.Share, rc.inkMed) {
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
    icon:    androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
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
