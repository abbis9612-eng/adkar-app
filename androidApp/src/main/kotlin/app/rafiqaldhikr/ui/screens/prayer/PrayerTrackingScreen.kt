package app.rafiqaldhikr.ui.screens.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun PrayerTrackingScreen(
    navController: NavHostController,
    viewModel: PrayerTimesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val prayers = listOf("fajr" to "الفجر", "dhuhr" to "الظهر", "asr" to "العصر", "maghrib" to "المغرب", "isha" to "العشاء")

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
                    text = "متابعة الصلاة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Today's summary
                val prayedCount = state.prayerLogs.count { it.prayed }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.emerald)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "$prayedCount / 5", 
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "صلوات اليوم", 
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { prayedCount / 5f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Prayer toggles
                prayers.forEach { (key, name) ->
                    val loggedPrayer = state.prayerLogs.find { it.prayerName == key }
                    val isPrayed = loggedPrayer?.prayed ?: false
                    val inMasjid = loggedPrayer?.inMasjid ?: false

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isPrayed) rc.emerald.copy(alpha = 0.05f) else rc.card)
                            .border(
                                1.dp,
                                if (isPrayed) rc.emerald else rc.gold.copy(alpha = 0.1f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.markPrayed(key, !isPrayed) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    name, 
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = rc.ink
                                )
                                if (isPrayed && inMasjid) {
                                    Text(
                                        "🕌 في المسجد", 
                                        fontSize = 12.sp, 
                                        color = rc.emerald
                                    )
                                }
                            }
                            Row {
                                Checkbox(
                                    checked = isPrayed,
                                    onCheckedChange = null, // handled by clickable
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = rc.emerald,
                                        uncheckedColor = rc.inkLight
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "سجّل صلواتك يومياً لمتابعة مداومتك",
                    fontSize = 14.sp,
                    color = rc.inkLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
