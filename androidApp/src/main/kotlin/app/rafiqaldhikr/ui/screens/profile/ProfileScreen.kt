package app.rafiqaldhikr.ui.screens.profile

import androidx.compose.ui.res.stringResource
import app.rafiqaldhikr.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.theme.stillableFloat
import app.rafiqaldhikr.ui.components.RafiqIconButton

/* Colors provided by LocalRafiqColors */

/* ألوان الأقسام من سلّم الضوء في RafiqPalette */

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION
══════════════════════════════════════════════════════════════ */

@Composable
private fun GeomDecoration(
    sizeDp: Dp = 160.dp,
    color: Color = LocalRafiqColors.current.gold.copy(alpha = 0.10f),
    spinDuration: Int = 90_000,
    modifier: Modifier = Modifier,
) {
    val rotation by stillableFloat(0f, 360f, spinDuration, LinearEasing, label = "geomRot")
    Canvas(modifier = modifier.size(sizeDp)) {
        val sz = this.size.width; val cx = sz / 2f; val cy = sz / 2f
        rotate(rotation, pivot = Offset(cx, cy)) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (i * 60 - 90) * PI.toFloat() / 180f; val r = sz * 0.43f
                    if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                    else lineTo(cx + r * cos(a), cy + r * sin(a))
                }; close()
            }
            drawPath(hex, color, style = Stroke(1.2f))
            drawCircle(color, sz * 0.44f, Offset(cx, cy), style = Stroke(0.7f))
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   PILL BUTTON
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   PROFILE HERO CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun ProfileHeroCard() {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RafiqShape.card)
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(
                    listOf(rc.heroStart, rc.heroMid, rc.heroEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
        )
        GeomDecoration(
            sizeDp = 200.dp, color = LocalRafiqColors.current.goldLight.copy(alpha = 0.14f), spinDuration = 80_000,
            modifier = Modifier.align(Alignment.TopStart).offset(x = (-50).dp, y = (-50).dp)
                .graphicsLayer { alpha = 0.3f },
        )

        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(2.dp, rc.goldLight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(36.dp)) {
                    val w = size.width; val cx = w / 2f; val cy = w / 2f
                    drawCircle(Color.White, w * 0.22f, Offset(cx, cy - w * 0.10f))
                    drawArc(Color.White, 0f, 180f, true,
                        Offset(cx - w * 0.32f, cy + w * 0.05f), Size(w * 0.64f, w * 0.50f))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.profile_user),
                style = RafiqType.titleL,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.profile_praise),
                fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   STAT CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    iconBg: Color,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    Column(
        modifier
            .rafiqCard()
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(36.dp).clip(RafiqShape.item).background(iconBg),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.height(8.dp))
        Text(value.localizedDigits(LocalArabicNumerals.current),
            style = NumbersStyle, fontSize = 22.sp, color = LocalRafiqColors.current.emerald)
        Text(label, color = LocalRafiqColors.current.inkMed, style = RafiqType.micro)
    }
}

/* ══════════════════════════════════════════════════════════════
   SECTION HEADER
══════════════════════════════════════════════════════════════ */

@Composable
private fun SectionHeader(title: String) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.width(4.dp).height(18.dp).clip(RafiqShape.chip).background(rc.gold))
        Text(title, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark, style = RafiqType.titleM)
    }
}

/* ══════════════════════════════════════════════════════════════
   TODAY ACHIEVEMENT ROW
══════════════════════════════════════════════════════════════ */

@Composable
private fun TodayRow(label: String, value: String, isAchieved: Boolean, isLast: Boolean = false) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = LocalRafiqColors.current.inkDark, style = RafiqType.label)
        if (isAchieved && value == "✓") {
            Box(
                Modifier.size(22.dp).clip(CircleShape).background(rc.emeraldPastel),
                contentAlignment = Alignment.Center,
            ) { RafiqIcon(RIcon.Check, 11.dp, rc.emerald) }
        } else {
            Text(value,
                fontWeight = FontWeight.Bold,
                color = if (isAchieved) rc.emerald else rc.inkMed, style = RafiqType.label)
        }
    }
    if (!isLast) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(rc.gold.copy(alpha = 0.07f)))
    }
}

/* ══════════════════════════════════════════════════════════════
   WEEK DAY CIRCLES
══════════════════════════════════════════════════════════════ */

@Composable
private fun WeekCircles(weekProgress: List<app.rafiq.domain.model.DailyProgressInfo>, todayIdx: Int) {
    val rc = LocalRafiqColors.current

    /*  حرفُ اليوم يُشتقّ من تاريخِ الصفّ لا من موضعه في القائمة.
     *
     *  كانت `days[idx]` — أي الحرفُ الأوّلُ لأوّل صفٍّ مهما كان يومُه.
     *  و`ProgressRepository.getRange` يُرجع الأيّامَ التي لها صفٌّ في
     *  القاعدة وحدَها، فأسبوعٌ فيه ثلاثةُ أيّامٍ مسجَّلة كان يُسمّيها
     *  «السبت والأحد والاثنين» أيّاً كانت. والحسابُ الصحيح موجودٌ أصلاً
     *  في `AwraqViewModel.weekdayIndex` — يُعاد استعماله لا يُكتب ثانية.  */
    val letters = app.rafiqaldhikr.util.weekdayLetters()
    fun letterOf(dateIso: String): String = runCatching {
        letters[app.rafiqaldhikr.util.weekdayIndex(kotlinx.datetime.LocalDate.parse(dateIso))]
    }.getOrDefault("")

    Row(
        Modifier
            .fillMaxWidth()
            .rafiqCard()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val week = weekProgress.takeLast(7)
        week.forEachIndexed { idx, day ->
            val score = ((if (day.morningDone) 1 else 0) +
                    (if (day.eveningDone) 1 else 0) +
                    day.prayersLogged.toInt().coerceAtMost(5))
            val filled = score >= 5
            val isToday = idx == week.lastIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                filled -> rc.emerald
                                score >= 2 -> rc.emeraldPastel
                                else -> rc.bg
                            }
                        )
                        .then(
                            if (isToday) Modifier.border(2.dp, rc.goldLight, CircleShape)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (filled) {
                        RafiqIcon(RIcon.Check, 12.dp, Color.White)
                    } else {
                        Text("$score",
                            fontWeight = FontWeight.Bold,
                            color = if (score >= 2) rc.emerald else rc.inkMed, style = RafiqType.micro)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(letterOf(day.date),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) rc.gold else rc.inkMed, style = RafiqType.micro)
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   QUICK LINK CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun QuickLinkCard(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        modifier
            .fillMaxWidth()
            .rafiqCard()
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(38.dp).clip(RafiqShape.item).background(rc.emeraldPastel),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.SemiBold,
                color = LocalRafiqColors.current.ink, modifier = Modifier.weight(1f), style = RafiqType.label)
            RafiqIcon(RIcon.ChevronLeft, 14.dp, rc.inkLight)
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN PROFILE SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // ═══ TOP BAR ═══
            RafiqTopBar(title = stringResource(R.string.profile_title)) {
                RafiqIconButton(onClick = { navController.navigate(RafiqRoute.Settings.route) }, label = stringResource(R.string.settings)) {
                    RafiqIcon(RIcon.Settings, 18.dp, rc.emerald)
                }
            }

            Spacer(Modifier.height(18.dp))

            // دفتر الأيام — كل يومٍ ورقة، وارتفاع الحبر ما خُطّ منها
            DaysGrid(
                streakCurrent = state.streak.current,
                streakLongest = state.streak.longest,
            )

            Spacer(Modifier.height(24.dp))

            // ═══ PROFILE HERO ═══
            ProfileHeroCard()

            Spacer(Modifier.height(16.dp))

            // ═══ STATS ROW (3 cards) ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    icon = { IcoMosque(20.dp, rc.emerald) },
                    value = "${state.todayProgress?.prayersLogged ?: 0}/5",
                    label = stringResource(R.string.stat_prayers),
                    iconBg = LocalRafiqColors.current.emeraldPastel,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = { RafiqIcon(RIcon.Trophy, 20.dp, rc.goldLight) },
                    value = "${state.streak.longest}",
                    label = stringResource(R.string.stat_longest_streak),
                    iconBg = LocalRafiqColors.current.tintGold,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = { RafiqIcon(RIcon.Flame, 20.dp, LocalRafiqColors.current.lightDusk) },
                    value = "${state.streak.current}",
                    label = stringResource(R.string.stat_current_streak),
                    iconBg = LocalRafiqColors.current.tintDusk,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══ TODAY'S ACHIEVEMENTS ═══
            SectionHeader(stringResource(R.string.today_achievements))
            Spacer(Modifier.height(10.dp))

            val p = state.todayProgress
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .rafiqCard()
            ) {
                TodayRow(stringResource(R.string.cat_morning), if (p?.morningDone == true) "✓" else "—", p?.morningDone == true)
                TodayRow(stringResource(R.string.cat_evening), if (p?.eveningDone == true) "✓" else "—", p?.eveningDone == true)
                TodayRow(stringResource(R.string.stat_quran_pages), "${p?.quranPages ?: 0}", (p?.quranPages ?: 0) > 0)
                TodayRow(stringResource(R.string.stat_tasbeeh), "${p?.tasbeehCount ?: 0}", (p?.tasbeehCount ?: 0) > 0)
                TodayRow(stringResource(R.string.stat_prayers), "${p?.prayersLogged ?: 0} / ٥", (p?.prayersLogged ?: 0) > 0, isLast = true)
            }

            Spacer(Modifier.height(20.dp))

            // ═══ THIS WEEK ═══
            SectionHeader(stringResource(R.string.this_week))
            Spacer(Modifier.height(10.dp))

            Box(Modifier.padding(horizontal = 14.dp)) {
                WeekCircles(
                    weekProgress = state.weekProgress,
                    todayIdx = state.weekProgress.size - 1,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ═══ QUICK LINKS ═══
            // التقرير الأسبوعي والحديقة والمشاركة مؤجَّلة إلى ما بعد V1:
            // الشاشات موجودة (@HiddenInV1) لكن لا مدخل لها — القرار في FINISH_PLAN.md ط٠.
            // «أوراقي» خرجت من التأجيل بطلبٍ صريح، ولها مدخلها أدناه.
            SectionHeader(stringResource(R.string.quick_links))
            Spacer(Modifier.height(10.dp))

            Column(
                Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickLinkCard(
                    icon = {
                        Canvas(Modifier.size(18.dp)) {
                            val w = size.width
                            // Bar chart
                            drawRect(rc.emerald, Offset(w * 0.10f, w * 0.50f), Size(w * 0.20f, w * 0.40f))
                            drawRect(rc.emerald, Offset(w * 0.40f, w * 0.25f), Size(w * 0.20f, w * 0.65f))
                            drawRect(rc.emerald, Offset(w * 0.70f, w * 0.10f), Size(w * 0.20f, w * 0.80f))
                        }
                    },
                    label = stringResource(R.string.stat_detailed),
                ) { navController.navigate(RafiqRoute.Statistics.route) }

                QuickLinkCard(
                    icon = {
                        // شبكةُ «أوراقي» نفسها مصغَّرة: ستُّ خاناتٍ منها أربعٌ ممتلئة
                        Canvas(Modifier.size(18.dp)) {
                            val w = size.width
                            val s = w * 0.26f
                            val g = w * 0.11f
                            val filled = listOf(0, 1, 3, 5)
                            for (i in 0 until 6) {
                                val col = i % 3
                                val row = i / 3
                                drawRoundRect(
                                    if (i in filled) rc.emerald else rc.emerald.copy(alpha = 0.22f),
                                    Offset(col * (s + g), row * (s + g)),
                                    Size(s, s),
                                    cornerRadius = CornerRadius(w * 0.06f),
                                )
                            }
                        }
                    },
                    label = "أوراقي",
                ) { navController.navigate(RafiqRoute.Achievements.route) }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
