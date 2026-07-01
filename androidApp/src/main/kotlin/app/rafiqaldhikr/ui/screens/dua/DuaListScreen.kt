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
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel

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
            // ═══ HEADER ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = category,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                // Back Button
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width
                        val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f)
                            lineTo(w * 0.70f, h * 0.50f)
                            lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }
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
                                fontSize  = 18.sp,
                                lineHeight = 32.sp,
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
                                    Icon(
                                        if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "مفضلة",
                                        tint     = rc.emerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
