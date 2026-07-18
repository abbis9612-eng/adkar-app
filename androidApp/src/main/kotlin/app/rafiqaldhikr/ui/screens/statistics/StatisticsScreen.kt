package app.rafiqaldhikr.ui.screens.statistics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.IcoBook
import app.rafiqaldhikr.ui.components.IcoFlame
import app.rafiqaldhikr.ui.components.IcoMisbaha
import app.rafiqaldhikr.ui.components.IcoMoon
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.IcoSun
import app.rafiqaldhikr.ui.components.IcoTrophy
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun StatisticsScreen(
    navController: NavHostController,
    // ✅ يشارك ProfileViewModel — لا حاجة لـ ViewModel جديد في M1
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
                    text = "الإحصائيات",
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
                // ═══ Streak Section ═══
                Text("السلسلة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier   = Modifier.weight(1f),
                        icon       = { s, c -> IcoFlame(s, c) },
                        iconColor  = rc.error,
                        title      = "السلسلة الحالية",
                        value      = "${state.streak.current}",
                        suffix     = "يوم",
                        rc = rc
                    )
                    StatCard(
                        modifier   = Modifier.weight(1f),
                        icon       = { s, c -> IcoTrophy(s, c) },
                        iconColor  = rc.gold,
                        title      = "أطول سلسلة",
                        value      = "${state.streak.longest}",
                        suffix     = "يوم",
                        rc = rc
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ═══ Today's Progress ═══
                Text("اليوم", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.ink)
                Spacer(Modifier.height(12.dp))

                val p = state.todayProgress
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = { s, c -> IcoBook(s, c) },
                        iconColor = rc.emerald,
                        title     = "صفحات القرآن",
                        value     = "${p?.quranPages ?: 0}",
                        suffix    = "صفحة",
                        rc = rc
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = { s, c -> IcoMisbaha(s, c) },
                        iconColor = rc.emerald,
                        title     = "التسبيح",
                        value     = "${p?.tasbeehCount ?: 0}",
                        suffix    = "مرة",
                        rc = rc
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = { s, c -> IcoSun(s, c) },
                        iconColor = rc.gold,
                        title     = "أذكار الصباح",
                        value     = if (p?.morningDone == true) "✅" else "—",
                        suffix    = "",
                        rc = rc
                    )
                    StatCard(
                        modifier  = Modifier.weight(1f),
                        icon      = { s, c -> IcoMoon(s, c) },
                        iconColor = rc.purpleSleep,
                        title     = "أذكار المساء",
                        value     = if (p?.eveningDone == true) "✅" else "—",
                        suffix    = "",
                        rc = rc
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ═══ Week Summary ═══
                Text("هذا الأسبوع", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = rc.ink)
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
                    Text("المداومة اليومية", fontSize = 12.sp, color = rc.inkMed)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        state.weekProgress.takeLast(7).forEach { day ->
                            val score = ((if (day.morningDone) 1 else 0) +
                                        (if (day.eveningDone) 1 else 0) +
                                        day.prayersLogged.toInt().coerceAtMost(5))
                            val dayLabel = when {
                                day.date.length >= 10 -> day.date.substring(8, 10)
                                else -> "—"
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val emoji = when {
                                    score >= 5 -> "🟢"
                                    score >= 2 -> "🟡"
                                    else       -> "⚪"
                                }
                                Text(emoji, fontSize = 16.sp)
                                Text(dayLabel, fontSize = 12.sp, color = rc.inkMed)
                            }
                        }
                    }

                    if (state.weekProgress.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        val activeDays = state.weekProgress.count {
                            it.morningDone || it.eveningDone || it.prayersLogged > 0
                        }
                        Text(
                            "$activeDays / ${state.weekProgress.size} أيام نشطة",
                            fontSize = 14.sp,
                            color = rc.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ═══ Prayer Stats ═══
                val prayersThisWeek = state.weekProgress.sumOf { it.prayersLogged }.toInt()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.emerald.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IcoMosque(32.dp, rc.emerald)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("صلوات هذا الأسبوع", fontSize = 12.sp, color = rc.inkMed)
                        Text(
                            "$prayersThisWeek صلاة",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = rc.ink
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier:  Modifier = Modifier,
    icon:      @Composable (androidx.compose.ui.unit.Dp, androidx.compose.ui.graphics.Color) -> Unit,
    iconColor: androidx.compose.ui.graphics.Color,
    title:     String,
    value:     String,
    suffix:    String,
    rc: RafiqPalette
) {
    Column(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon(28.dp, iconColor)
        Spacer(Modifier.height(8.dp))
        Text(
            value.localizedDigits(LocalArabicNumerals.current),
            style = NumbersStyle,
            fontSize = 24.sp,
            color = iconColor
        )
        if (suffix.isNotEmpty()) {
            Text(suffix, fontSize = 12.sp, color = rc.inkMed)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 12.sp, color = rc.ink, textAlign = TextAlign.Center)
    }
}
