package app.rafiqaldhikr.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun WeeklyReportScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val week = state.weekProgress
    val rc = LocalRafiqColors.current

    val totalQuran    = week.sumOf { it.quranPages }
    val totalTasbeeh  = week.sumOf { it.tasbeehCount }
    val totalPrayers  = week.sumOf { it.prayersLogged }
    val morningDays   = week.count { it.morningDone }
    val eveningDays   = week.count { it.eveningDone }
    val activeDays    = week.count { it.morningDone || it.eveningDone || it.prayersLogged > 0 }

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
            // u2550u2550u2550 HEADER u2550u2550u2550
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RafiqBackButton(onClick = { navController.popBackStack() })

                Text(
                    text = "التقرير الأسبوعي 📊",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.emerald.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌙", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("$activeDays / 7 أيام نشطة", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.emerald)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (activeDays >= 6) "أداء ممتاز! بارك الله فيك" else
                        if (activeDays >= 4) "أداء جيد، استمر في المداومة" else
                        "حاول المداومة أكثر هذا الأسبوع",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = rc.inkMed
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), "📖", "$totalQuran", "صفحات قرآن", rc)
                    ReportStatCard(Modifier.weight(1f), "📿", "$totalTasbeeh", "تسبيح", rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), "🕌", "$totalPrayers", "صلوات", rc)
                    ReportStatCard(Modifier.weight(1f), "🌅", "$morningDays / 7", "أذكار صباح", rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), "🌆", "$eveningDays / 7", "أذكار مساء", rc)
                    ReportStatCard(Modifier.weight(1f), "🔥", "${state.streak.current}", "سلسلة حالية", rc)
                }

                Spacer(Modifier.height(32.dp))

                // Day-by-day
                Text("التفاصيل اليومية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    week.reversed().forEachIndexed { index, day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day.date, fontSize = 14.sp, color = rc.inkMed)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (day.morningDone) Text("🌅", fontSize = 16.sp)
                                if (day.eveningDone) Text("🌆", fontSize = 16.sp)
                                if (day.quranPages > 0) Text("📖${day.quranPages}", fontSize = 14.sp, color = rc.ink)
                                if (day.prayersLogged > 0) Text("🕌${day.prayersLogged}", fontSize = 14.sp, color = rc.ink)
                            }
                        }
                        if (index < week.size - 1) {
                            HorizontalDivider(color = rc.gold.copy(alpha = 0.06f))
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ReportStatCard(modifier: Modifier, emoji: String, value: String, label: String, rc: RafiqPalette) {
    Column(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = rc.inkMed)
    }
}
