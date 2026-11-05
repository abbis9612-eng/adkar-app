package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqIconButton
import app.rafiqaldhikr.ui.components.RIcon
import androidx.navigation.NavHostController
import app.rafiq.domain.model.QuranBookmark
import app.rafiq.domain.repository.QuranRepository
import app.rafiqaldhikr.ui.components.EmptyState
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.mushaf.SurahNames
import androidx.compose.ui.platform.LocalContext

@Composable
fun QuranBookmarksScreen(navController: NavHostController) {
    val repository = koinInject<QuranRepository>()
    //  `getBookmarks` صارت تُرجع العلاماتِ وحدَها — وموضعُ الوقوف
    //  مجرًى منفصلٌ يُعرض قسماً خاصّاً فوقها، فلا يختلط النوعان.
    val bookmarks by repository.getBookmarks().collectAsStateWithLifecycle(emptyList())
    val stop by repository.stopMark().collectAsStateWithLifecycle(null)
    val rc = LocalRafiqColors.current
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            RafiqTopBar(
                title  = stringResource(R.string.quran_bookmarks),
                onBack = {navController.popBackStack()},
            )

            if (bookmarks.isEmpty() && stop == null) {
                EmptyState(
                    message  = stringResource(R.string.bookmarks_empty),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    stop?.let { st ->
                        item(key = "stop-section") {
                            SectionLabel(stringResource(R.string.bookmarks_stop_section), rc)
                        }
                        item(key = "stop-${st.id}") {
                            BookmarkCard(
                                bookmark = st,
                                onClick = {
                                    navController.navigate(
                                        RafiqRoute.Mushaf.atVerse(st.page, "${st.surah}:${st.ayah}"),
                                    )
                                },
                                onDelete = { scope.launch { repository.clearStop() } },
                                rc = rc,
                            )
                        }
                    }
                    if (bookmarks.isNotEmpty() && stop != null) {
                        item(key = "marks-section") {
                            SectionLabel(stringResource(R.string.bookmarks_marks_section), rc)
                        }
                    }
                    items(bookmarks, key = { it.id }) { bookmark ->
                        BookmarkCard(
                            bookmark  = bookmark,
                            onClick   = {
                                navController.navigate(RafiqRoute.Mushaf.atVerse(bookmark.page, "${bookmark.surah}:${bookmark.ayah}"))
                            },
                            /*  الحذف لم يكن موجوداً: `removeBookmark` مكتوبةٌ
                             *  في المستودع بلا مستدعٍ، والشاشةُ للقراءة فقط.
                             *  فمن وضع علامةً بالخطأ لم يستطع رفعَها إلّا من
                             *  المصحف نفسِه إن اهتدى إلى آيتها.  */
                            onDelete  = {
                                scope.launch { repository.removeBookmark(bookmark.id) }
                            },
                            rc = rc
                        )
                    }
                }
            }
        }
    }
}

/** عنوانُ قسمٍ في القائمة — كوفيٌّ صغيرٌ بحبرٍ خامل. */
@Composable
private fun SectionLabel(text: String, rc: RafiqPalette) {
    Text(text, style = RafiqType.metaS, color = rc.inkLight)
}

@Composable
private fun BookmarkCard(
    bookmark: QuranBookmark,
    onClick:  () -> Unit,
    onDelete: () -> Unit,
    rc: RafiqPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .rafiqCard()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${SurahNames.of(LocalContext.current, bookmark.surah)} · الآية ${bookmark.ayah}"
                        .localizedDigits(LocalArabicNumerals.current),
                    fontWeight = FontWeight.Bold,
                    color = rc.ink, style = RafiqType.titleM)
                Spacer(Modifier.height(4.dp))
                Text("صفحة ${bookmark.page}".localizedDigits(LocalArabicNumerals.current),
                    color = rc.inkMed, style = RafiqType.bodyS)
                val note = bookmark.note
                if (!note.isNullOrBlank()) {
                    //  ملاحظةُ صاحبها — بشريطٍ ذهبيٍّ كما في ورقة الآية،
                    //  فتُقرأ كلامَه لا بياناً من التطبيق.
                    Spacer(Modifier.height(9.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(rc.bg)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            Modifier
                                .width(3.dp)
                                .heightIn(min = 14.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(rc.gold),
                        )
                        Text(note, style = RafiqType.caption, color = rc.inkMed)
                    }
                }
                if (bookmark.createdAt > 0) {
                    Spacer(Modifier.height(8.dp))
                    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        .format(Date(bookmark.createdAt))
                        .localizedDigits(LocalArabicNumerals.current)
                    Text(dateStr,
                        color = rc.inkMed, style = RafiqType.caption)
                }
            }

            RafiqIconButton(
                onClick = onDelete,
                label   = stringResource(R.string.bookmark_remove),
            ) {
                RafiqIcon(RIcon.Trash, 17.dp, rc.inkMed)
            }
        }
    }
}
