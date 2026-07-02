package app.rafiqaldhikr.ui.screens.daycompanion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.StationIcon
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.toEasternArabic
import org.koin.androidx.compose.koinViewModel

@Composable
fun DayCompanionScreen(
    navController: NavHostController,
    viewModel: DayCompanionViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // ═══ HEADER ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "رفيق اليوم",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = rc.emerald,
                    )
                    Text(
                        "«أحبُّ الأعمال إلى الله أدومُها وإن قلّ» — متفق عليه",
                        fontSize = 11.sp,
                        color = rc.inkMed,
                    )
                }
                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // ═══ ملخص التقدم ═══
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(rc.emerald)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "محطات يومك",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    "${state.doneCount.toEasternArabic()} من ${state.stations.size.toEasternArabic()}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (state.stations.isNotEmpty()) {
                                LinearProgressIndicator(
                                    progress = { state.doneCount / state.stations.size.toFloat() },
                                    modifier = Modifier.width(90.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = rc.goldLight,
                                    trackColor = Color.White.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                items(state.stations) { station ->
                    StationCard(
                        station = station,
                        isLast  = station.id == state.stations.lastOrNull()?.id,
                        onStart = { station.route?.let { navController.navigate(it) } },
                        onDone  = { viewModel.completeStation(station.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: DayCompanionViewModel.StationUi,
    isLast:  Boolean,
    onStart: () -> Unit,
    onDone:  () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val active = station.status == StationStatus.ACTIVE
    val done   = station.status == StationStatus.DONE

    Row(Modifier.height(IntrinsicSize.Min)) {
        // ═══ خط الزمن ═══
        Column(
            Modifier.width(28.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done   -> rc.emerald
                            active -> rc.gold
                            else   -> rc.divider
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) Text("✓", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (done) rc.emerald.copy(alpha = 0.4f) else rc.divider)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // ═══ بطاقة المحطة ═══
        Column(
            Modifier
                .weight(1f)
                .padding(bottom = 14.dp)
                .shadow(if (active) 4.dp else 1.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(rc.card)
                .border(
                    width = if (active) 1.5.dp else 1.dp,
                    color = when {
                        active -> rc.gold.copy(alpha = 0.5f)
                        done   -> rc.emerald.copy(alpha = 0.25f)
                        else   -> rc.divider
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (done) rc.emeraldPastel.copy(alpha = 0.5f) else rc.emeraldPastel),
                    contentAlignment = Alignment.Center
                ) { StationIcon(station.id, 24.dp) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        station.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (done) rc.inkMed else rc.ink
                    )
                    Text(station.timeLabel, fontSize = 11.sp, color = rc.inkLight)
                }
                if (active) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(rc.gold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("الآن", fontSize = 11.sp, color = rc.gold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!done) {
                Spacer(Modifier.height(8.dp))
                Text(station.description, fontSize = 13.sp, lineHeight = 22.sp, color = rc.inkMed)
                Spacer(Modifier.height(6.dp))
                Text(station.virtue, fontSize = 11.sp, lineHeight = 19.sp, color = rc.gold.copy(alpha = 0.9f))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (station.route != null) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(rc.emerald)
                                .clickable(onClick = onStart)
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text("ابدأ", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(rc.emeraldPastel)
                            .clickable(onClick = onDone)
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text("تمّ ✓", fontSize = 13.sp, color = rc.emerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
