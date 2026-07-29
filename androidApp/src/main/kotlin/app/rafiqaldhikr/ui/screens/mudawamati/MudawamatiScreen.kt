package app.rafiqaldhikr.ui.screens.mudawamati

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.IcoFlame
import app.rafiqaldhikr.ui.components.IcoMisbaha
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.IcoTrophy
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.Shamsa
import app.rafiqaldhikr.ui.components.ShamsaState
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.calculateWirdProgress
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.WIRD_MINIMUM
import org.koin.androidx.compose.koinViewModel

/**
 * مُداوَمَتي — الشاشة الموحّدة التي تحلّ محلّ الإحصائيات والتقرير الأسبوعي والإنجازات،
 * على أساس «أحبُّ الأعمالِ إلى اللهِ أدْوَمُها وإنْ قَلّ» (متفق عليه): الاستمرار لا الكمّية.
 */
@Composable
fun MudawamatiScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val persisted by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val arabic = LocalArabicNumerals.current

    val week = state.weekProgress
    val totalQuran   = week.sumOf { it.quranPages }
    val totalTasbeeh = week.sumOf { it.tasbeehCount }
    val totalPrayers = week.sumOf { it.prayersLogged }
    val morningDays  = week.count { it.morningDone }
    val eveningDays  = week.count { it.eveningDone }

    val wirdCurrent = calculateWirdProgress(state.todayProgress)

    val milestones = listOf(3L to "streak_3", 7L to "streak_7", 30L to "streak_30", 100L to "streak_100")
    val firstNotDoneIndex = milestones.indexOfFirst { (threshold, key) ->
        !(state.streak.longest >= threshold || key in persisted)
    }

    LaunchedEffect(state.streak.longest, persisted) {
        val newlyUnlocked = milestones
            .filter { (threshold, key) -> state.streak.longest >= threshold && key !in persisted }
            .map { it.second }
        if (newlyUnlocked.isNotEmpty()) viewModel.persistUnlocked(newlyUnlocked)
    }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RafiqTopBar(
                title    = "مُداوَمَتي",
                subtitle = "«أحبُّ الأعمالِ إلى اللهِ أدْوَمُها وإنْ قَلّ» — متفق عليه",
                onBack   = { navController.popBackStack() },
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                // ═══ مِيقاتُ أُسبوعِك ═══
                SectionLabel("مِيقاتُ أُسبوعِك", rc)
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        week.takeLast(7).forEach { day ->
                            val score = (if (day.morningDone) 1 else 0) +
                                (if (day.eveningDone) 1 else 0) +
                                day.prayersLogged.toInt().coerceAtMost(5)
                            val dayLabel = if (day.date.length >= 10) day.date.substring(8, 10) else "—"
                            WeekBar(score = score, label = dayLabel.localizedDigits(arabic), rc = rc)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = rc.gold.copy(alpha = 0.08f))
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StreakStat(Modifier.weight(1f), { s, c -> IcoFlame(s, c) }, rc.error, "السلسلة الحالية", state.streak.current, rc)
                        StreakStat(Modifier.weight(1f), { s, c -> IcoTrophy(s, c) }, rc.gold, "أطول سلسلة", state.streak.longest, rc)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ═══ وِردُ اليوم ═══
                SectionLabel("وِردُ اليوم", rc)
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.emerald.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${wirdCurrent.localized(arabic)} / ${1000.localized(arabic)}",
                            style = NumbersStyle, fontSize = 18.sp, color = rc.emerald
                        )
                        Text(
                            "الحدّ الأدنى: ${WIRD_MINIMUM.localized(arabic)}",
                            fontSize = 12.sp, color = rc.inkMed
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    WirdBar(current = wirdCurrent, total = 1000, minimum = WIRD_MINIMUM, rc = rc)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (wirdCurrent >= WIRD_MINIMUM) "بلغتَ الحدّ الأدنى — المداومة لا تُقاس بالكثرة"
                        else "خطوة صغيرة كافية اليوم لتبلغ الحدّ الأدنى",
                        fontSize = 12.sp, color = rc.inkMed
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ═══ هذا الأسبوع ═══
                SectionLabel("هذا الأسبوع", rc)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeekStat(Modifier.weight(1f), { RafiqIcon(RIcon.Book, 26.dp, rc.emerald) }, "${totalQuran.localized(arabic)}", "صفحات قرآن", rc)
                    WeekStat(Modifier.weight(1f), { IcoMisbaha(26.dp, rc.emerald) }, "${totalTasbeeh.localized(arabic)}", "تسبيح", rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeekStat(Modifier.weight(1f), { IcoMosque(26.dp, rc.emerald) }, "${totalPrayers.localized(arabic)}", "صلوات", rc)
                    WeekStat(Modifier.weight(1f), { RafiqIcon(RIcon.Sunrise, 26.dp, rc.gold) }, "${morningDays.localized(arabic)} / ٧", "أذكار صباح", rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WeekStat(Modifier.weight(1f), { RafiqIcon(RIcon.Sunset, 26.dp, rc.eveningRing) }, "${eveningDays.localized(arabic)} / ٧", "أذكار مساء", rc)
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                // ═══ مَعالِمُ المُداوَمة ═══
                SectionLabel("مَعالِمُ المُداوَمة", rc)
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    milestones.forEachIndexed { index, (threshold, key) ->
                        val done = state.streak.longest >= threshold || key in persisted
                        val shamsaState = when {
                            done -> ShamsaState.Done
                            index == firstNotDoneIndex -> ShamsaState.Now
                            else -> ShamsaState.Upcoming
                        }
                        MilestoneItem(threshold, shamsaState, state.streak.current, arabic, rc)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ═══ تفاصيلُ الأيّام ═══
                SectionLabel("تفاصيلُ الأيّام", rc)
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    week.reversed().forEachIndexed { index, day ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(day.date, fontSize = 14.sp, color = rc.inkMed)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (day.morningDone) RafiqIcon(RIcon.Sunrise, 16.dp, rc.gold)
                                if (day.eveningDone) RafiqIcon(RIcon.Sunset, 16.dp, rc.eveningRing)
                                if (day.quranPages > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    RafiqIcon(RIcon.Book, 14.dp, rc.emerald)
                                    Text(day.quranPages.toString().localizedDigits(arabic), fontSize = 14.sp, color = rc.ink)
                                }
                                if (day.prayersLogged > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    IcoMosque(14.dp, rc.emerald)
                                    Text(day.prayersLogged.toString().localizedDigits(arabic), fontSize = 14.sp, color = rc.ink)
                                }
                            }
                        }
                        if (index < week.size - 1) HorizontalDivider(color = rc.gold.copy(alpha = 0.06f))
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, rc: RafiqPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(16.dp).background(rc.gold, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = rc.ink)
    }
}

@Composable
private fun WeekBar(score: Int, label: String, rc: RafiqPalette) {
    val fraction = (score / 7f).coerceIn(0f, 1f)
    val barColor = when {
        score >= 5 -> rc.emerald
        score >= 2 -> rc.gold
        else -> rc.divider
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .width(10.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(rc.divider.copy(alpha = 0.25f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction.coerceAtLeast(0.06f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }
        Text(label, fontSize = 11.sp, color = rc.inkMed)
    }
}

@Composable
private fun StreakStat(
    modifier: Modifier,
    icon: @Composable (androidx.compose.ui.unit.Dp, Color) -> Unit,
    iconColor: Color,
    title: String,
    value: Long,
    rc: RafiqPalette,
) {
    val arabic = LocalArabicNumerals.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        icon(22.dp, iconColor)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value.localized(arabic), style = NumbersStyle, fontSize = 16.sp, color = rc.ink)
            Text(title, fontSize = 11.sp, color = rc.inkMed)
        }
    }
}

@Composable
private fun WeekStat(modifier: Modifier, icon: @Composable () -> Unit, value: String, label: String, rc: RafiqPalette) {
    Column(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, color = rc.inkMed)
    }
}

@Composable
private fun WirdBar(current: Int, total: Int, minimum: Int, rc: RafiqPalette) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(14.dp)) {
        val trackWidth = maxWidth
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(7.dp))
                .background(rc.divider.copy(alpha = 0.3f))
        )
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth((current.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                .clip(RoundedCornerShape(7.dp))
                .background(Brush.horizontalGradient(listOf(rc.emerald, rc.gold)))
        )
        Box(
            Modifier
                .offset(x = trackWidth * (minimum.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                .width(2.dp)
                .fillMaxHeight()
                .background(rc.ink.copy(alpha = 0.35f))
        )
    }
}

@Composable
private fun MilestoneItem(
    threshold: Long,
    shamsaState: ShamsaState,
    current: Long,
    arabic: Boolean,
    rc: RafiqPalette,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Shamsa(state = shamsaState, size = 40.dp)
        Text("${threshold.localized(arabic)} يوم", fontSize = 11.sp, color = rc.inkMed)
        if (shamsaState == ShamsaState.Now) {
            Text("${current.localized(arabic)}/${threshold.localized(arabic)}", fontSize = 10.sp, color = rc.emerald)
        }
    }
}
