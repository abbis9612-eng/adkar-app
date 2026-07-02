package app.rafiqaldhikr.ui.screens.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun GardenScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    // Garden items grow based on daily activities
    val plantScore = calculatePlantScore(
        morningDone  = state.todayProgress?.morningDone ?: false,
        eveningDone  = state.todayProgress?.eveningDone ?: false,
        quranPages   = state.todayProgress?.quranPages ?: 0,
        tasbeehCount = state.todayProgress?.tasbeehCount ?: 0,
        prayers      = state.todayProgress?.prayersLogged ?: 0
    )

    val weekPlants = state.weekProgress.map { day ->
        GardenPlant(
            date  = day.date,
            stage = calculatePlantScore(
                day.morningDone, day.eveningDone,
                day.quranPages, day.tasbeehCount, day.prayersLogged
            )
        )
    }

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
                    "الحديقة الروحية",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Today's plant
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier            = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            plantEmoji(plantScore),
                            fontSize = 72.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            plantLabel(plantScore),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = rc.emerald
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "نبتة اليوم تنمو بأعمالك",
                            fontSize = 14.sp,
                            color = rc.inkMed
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "حديقة الأسبوع",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink
                )
                Spacer(Modifier.height(12.dp))

                // Week garden grid
                LazyVerticalGrid(
                    columns                = GridCells.Fixed(4),
                    contentPadding         = PaddingValues(4.dp),
                    horizontalArrangement  = Arrangement.spacedBy(8.dp),
                    verticalArrangement    = Arrangement.spacedBy(8.dp),
                    modifier               = Modifier.weight(1f)
                ) {
                    items(weekPlants) { plant ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(rc.card)
                                .border(1.dp, rc.gold.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier            = Modifier.fillMaxSize().padding(8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(plantEmoji(plant.stage), fontSize = 32.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    plant.date.takeLast(2),
                                    fontSize = 12.sp,
                                    color = rc.inkLight
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Legend
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("دليل النمو", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "🌱 بذرة — ابدأ يومك",
                            "🌿 نبتة — أذكار أو قرآن",
                            "🌳 شجرة — أذكار + قرآن + صلاة",
                            "🌺 زهرة — يوم مكتمل!"
                        ).forEach { line ->
                            Text(line, fontSize = 12.sp, color = rc.inkMed, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

private data class GardenPlant(val date: String, val stage: Int)

private fun calculatePlantScore(
    morningDone: Boolean, eveningDone: Boolean,
    quranPages: Long, tasbeehCount: Long, prayers: Long
): Int {
    var score = 0
    if (morningDone) score++
    if (eveningDone) score++
    if (quranPages > 0) score++
    if (tasbeehCount > 0) score++
    if (prayers >= 3) score++
    return score.coerceAtMost(4)
}

private fun plantEmoji(stage: Int) = when (stage) {
    0    -> "🌰"
    1    -> "🌱"
    2    -> "🌿"
    3    -> "🌳"
    else -> "🌺"
}

private fun plantLabel(stage: Int) = when (stage) {
    0    -> "بذرة"
    1    -> "نبتة صغيرة"
    2    -> "نبتة نامية"
    3    -> "شجرة"
    else -> "زهرة مكتملة!"
}
