package app.rafiqaldhikr.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun QuranReadingScreen(
    surahNumber:   Int,
    navController: NavHostController,
    viewModel:     QuranReadingViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

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
                Text(
                    text = state.surah?.nameAr ?: "سورة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            when {
                state.isLoading -> LoadingState()
                else -> LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Bismillah (skip for Surah 1 and 9)
                    if (surahNumber != 1 && surahNumber != 9) {
                        item {
                            Text(
                                text      = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                fontSize  = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                color     = rc.emerald
                            )
                            Divider(color = rc.divider, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }

                    items(state.ayahs) { ayah ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text      = ayah.textUthmani + " ﴿${ayah.ayahNumber}﴾",
                                fontSize  = 22.sp,
                                lineHeight = 40.sp,
                                textAlign = TextAlign.End,
                                color     = rc.ink,
                                modifier  = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier              = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text(
                                    "ص ${ayah.page} | جزء ${ayah.juz}",
                                    fontSize = 12.sp,
                                    color = rc.inkLight
                                )
                                IconButton(
                                    onClick  = { viewModel.toggleBookmark(surahNumber, ayah.ayahNumber, ayah.page) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        if (ayah.ayahNumber in state.bookmarks) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "علامة",
                                        modifier = Modifier.size(20.dp),
                                        tint = rc.emerald
                                    )
                                }
                            }
                            Divider(color = rc.divider, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
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
