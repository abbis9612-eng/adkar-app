package app.rafiqaldhikr.ui.screens.dua

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
import app.rafiqaldhikr.ui.components.EmptyState
import app.rafiqaldhikr.ui.components.IcoHeart
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqTopBar

@Composable
fun DuaListScreen(
    category:      String,
    navController: NavHostController,
    viewModel:     DuaViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    LaunchedEffect(category) { viewModel.loadCategory(category) }

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
            RafiqTopBar(
                title  = duaCategoryLabel(category),
                onBack = { navController.popBackStack() },
            )

            if (!state.isLoading && state.duas.isEmpty()) {
                EmptyState(message = "لا توجد أدعية في هذا القسم بعد")
                return@Column
            }

            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.duas) { dua ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(rc.card)
                            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text      = dua.textAr,
                                fontSize  = 19.sp,
                                fontFamily = app.rafiqaldhikr.ui.theme.NaskhFamily,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 38.sp,
                                textAlign = TextAlign.End,
                                color     = rc.ink,
                                modifier  = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${dua.source} (${dua.sourceGrade})",
                                    fontSize = 12.sp,
                                    color    = rc.inkLight,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleFavorite(dua.id, dua.isFavorite) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    IcoHeart(20.dp, rc.emerald, filled = dua.isFavorite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
