package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.BorderActive

@Composable
fun QuranBookmarksScreen(navController: NavHostController) {
    val repository = koinInject<QuranRepository>()
    val bookmarks by repository.getBookmarks().collectAsStateWithLifecycle(emptyList())
    val rc = LocalRafiqColors.current

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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "علامات القرآن",
                    style = RafiqType.titleL,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            if (bookmarks.isEmpty()) {
                EmptyState(
                    message  = "لا توجد علامات مرجعية بعد\nأضف علامات أثناء القراءة",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        BookmarkCard(
                            bookmark  = bookmark,
                            onClick   = {
                                navController.navigate(RafiqRoute.QuranReading.withSurah(bookmark.surah))
                            },
                            rc = rc
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkCard(
    bookmark: QuranBookmark,
    onClick:  () -> Unit,
    rc: RafiqPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.card)
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
                    "سورة ${bookmark.surah} — آية ${bookmark.ayah}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.ink
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "صفحة ${bookmark.page}".localizedDigits(LocalArabicNumerals.current),
                    fontSize = 14.sp,
                    color = rc.inkMed
                )
                if (bookmark.createdAt > 0) {
                    Spacer(Modifier.height(8.dp))
                    val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        .format(Date(bookmark.createdAt))
                        .localizedDigits(LocalArabicNumerals.current)
                    Text(
                        dateStr,
                        fontSize = 12.sp,
                        color = rc.inkMed
                    )
                }
            }
        }
    }
}
