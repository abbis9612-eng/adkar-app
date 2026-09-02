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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.IcoMisbaha
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.util.weekdayIndex
import app.rafiqaldhikr.util.weekdayLetters
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.model.isActiveDay
import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

@Composable
fun WeeklyReportScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    /*  الشاشةُ كانت تفتح على «٠ / ٧ أيّام نشطة · حاول المداومة أكثر»
     *  قبل وصول البيانات، فتوبّخ من كان أسبوعُه كاملاً. و`isLoading`
     *  محسوبةٌ في `ProfileViewModel` منذ البداية ولا يقرؤها أحد. */
    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(rc.bg)) { LoadingState() }
        return
    }

    /*  سبعةُ صفوفٍ دائماً — لا ما وُجد في القاعدة.
     *
     *  كانت القائمةُ `weekProgress` كما جاءت: ثلاثةُ أيّامٍ مسجَّلةٍ ←
     *  ثلاثةُ صفوف، فيقرأ صاحبُ الأسبوع «التفاصيل اليومية» ويرى ثلاثةَ
     *  أيّامٍ فيظنّ أنّ أربعةً ضاعت. والغائبُ يومٌ فارغٌ لا يومٌ ناقص. */
    val today  = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val byDate = state.weekProgress.associateBy { it.date }
    val week: List<Pair<String, DailyProgressInfo?>> = (0..6).map { back ->
        val d = today.minus(back, DateTimeUnit.DAY).toString()
        d to byDate[d]
    }
    val rows = week.mapNotNull { it.second }

    val totalQuran    = rows.sumOf { it.quranPages }
    val totalTasbeeh  = rows.sumOf { it.tasbeehCount }
    val totalPrayers  = rows.sumOf { it.prayersLogged }
    val morningDays   = rows.count { it.morningDone }
    val eveningDays   = rows.count { it.eveningDone }
    /*  كان العدُّ للأذكار والصلوات فقط: فيومٌ قرأتَ فيه عشرين صفحةً
     *  «غيرُ نشط» — والبطاقةُ التي تعلوه تعرض تلك الصفحات بعينها.
     *  و`isActiveDay` المشتركة تعدّ الخمسةَ كلَّها. */
    val activeDays    = rows.count { isActiveDay(it) }

    val letters = weekdayLetters()
    fun dayLabel(iso: String): String = runCatching {
        val d = LocalDate.parse(iso)
        // «الأحد ٣١» لا «2026-08-31» خاماً.
        "${letters[weekdayIndex(d)]} ${d.dayOfMonth}".localizedDigits(ar)
    }.getOrDefault(iso)

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
            RafiqTopBar(
                title  = stringResource(R.string.report_title),
                onBack = {navController.popBackStack()},
            )

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
                        .clip(RafiqShape.card)
                        .background(rc.card)
                        .border(1.dp, rc.emerald.copy(alpha = 0.2f), RafiqShape.card)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RafiqIcon(RIcon.Moon, 44.dp, rc.emerald)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.report_active_days, activeDays).localizedDigits(ar),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.emerald,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(
                            when {
                                activeDays >= 6 -> R.string.report_praise_high
                                activeDays >= 4 -> R.string.report_praise_mid
                                else            -> R.string.report_praise_low
                            }
                        ),
                        textAlign = TextAlign.Center,
                        color = rc.inkMed, style = RafiqType.bodyS,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Stats
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), { RafiqIcon(RIcon.Book, 26.dp, rc.emerald) }, "$totalQuran", stringResource(R.string.report_quran_pages), rc)
                    ReportStatCard(Modifier.weight(1f), { IcoMisbaha(26.dp, rc.emerald) }, "$totalTasbeeh", stringResource(R.string.report_tasbeeh), rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), { IcoMosque(26.dp, rc.emerald) }, "$totalPrayers", stringResource(R.string.report_prayers), rc)
                    ReportStatCard(Modifier.weight(1f), { RafiqIcon(RIcon.Sunrise, 26.dp, rc.gold) }, "$morningDays / 7", stringResource(R.string.cat_morning), rc)
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(Modifier.weight(1f), { RafiqIcon(RIcon.Sunset, 26.dp, rc.lightDusk) }, "$eveningDays / 7", stringResource(R.string.cat_evening), rc)
                    ReportStatCard(Modifier.weight(1f), { RafiqIcon(RIcon.Flame, 26.dp, rc.lightDusk) }, "${state.streak.current}", stringResource(R.string.report_streak), rc)
                }

                Spacer(Modifier.height(32.dp))

                // Day-by-day
                Text(stringResource(R.string.report_daily), fontWeight = FontWeight.Bold, color = rc.ink, style = RafiqType.titleM)
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .rafiqCard()
                        .padding(16.dp)
                ) {
                    week.forEachIndexed { index, (date, day) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                dayLabel(date),
                                color = if (index == 0) rc.gold else rc.inkMed,
                                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                                style = RafiqType.bodyS,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (day == null) {
                                    // يومٌ فارغٌ يُقال إنّه فارغ — لا يُحذف صفُّه.
                                    Text("—", color = rc.inkLight, style = RafiqType.bodyS)
                                } else {
                                    if (day.morningDone) RafiqIcon(RIcon.Sunrise, 16.dp, rc.gold)
                                    if (day.eveningDone) RafiqIcon(RIcon.Sunset, 16.dp, rc.lightDusk)
                                    if (day.quranPages > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        RafiqIcon(RIcon.Book, 14.dp, rc.emerald)
                                        Text("${day.quranPages}".localizedDigits(ar), color = rc.ink, style = RafiqType.bodyS)
                                    }
                                    if (day.tasbeehCount > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        IcoMisbaha(14.dp, rc.emerald)
                                        Text("${day.tasbeehCount}".localizedDigits(ar), color = rc.ink, style = RafiqType.bodyS)
                                    }
                                    if (day.prayersLogged > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        IcoMosque(14.dp, rc.emerald)
                                        Text("${day.prayersLogged}".localizedDigits(ar), color = rc.ink, style = RafiqType.bodyS)
                                    }
                                    if (!day.morningDone && !day.eveningDone && day.quranPages == 0L &&
                                        day.tasbeehCount == 0L && day.prayersLogged == 0L) {
                                        Text("—", color = rc.inkLight, style = RafiqType.bodyS)
                                    }
                                }
                            }
                        }
                        if (index < week.lastIndex) {
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
private fun ReportStatCard(modifier: Modifier, icon: @Composable () -> Unit, value: String, label: String, rc: RafiqPalette) {
    Column(
        modifier = modifier
            .rafiqCard()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(Modifier.height(8.dp))
        Text(value.localizedDigits(LocalArabicNumerals.current), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = rc.ink)
        Spacer(Modifier.height(4.dp))
        Text(label, color = rc.inkMed, style = RafiqType.caption)
    }
}
