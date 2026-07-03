package app.rafiqaldhikr.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.theme.RafiqPalette
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.RafiqBackButton

data class Achievement(
    val key:         String,
    val emoji:       String,
    val title:       String,
    val description: String,
    val unlocked:    Boolean
)

@Composable
fun AchievementsScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val persisted by viewModel.unlockedAchievements.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    // شرط اليوم يفتح الإنجاز — والمفتوح سابقاً يبقى مفتوحاً للأبد
    val achievements = buildAchievements(
        currentStreak = state.streak.current,
        longestStreak = state.streak.longest,
        todayQuran    = state.todayProgress?.quranPages ?: 0,
        todayTasbeeh  = state.todayProgress?.tasbeehCount ?: 0
    ).map { it.copy(unlocked = it.unlocked || it.key in persisted) }

    LaunchedEffect(achievements) {
        val newlyUnlocked = achievements.filter { it.unlocked && it.key !in persisted }.map { it.key }
        if (newlyUnlocked.isNotEmpty()) viewModel.persistUnlocked(newlyUnlocked)
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
                    text = "الإنجازات",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                RafiqBackButton(onClick = { navController.popBackStack() })
            }

            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val unlocked = achievements.count { it.unlocked }
                    // Progress summary card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(rc.emerald)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$unlocked / ${achievements.size}".localizedDigits(LocalArabicNumerals.current),
                                style = NumbersStyle,
                                fontSize = 32.sp,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            Text(
                                "إنجاز مفتوح",
                                fontSize = 14.sp,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { unlocked / achievements.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = androidx.compose.ui.graphics.Color.White,
                                trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                items(achievements) { ach ->
                    AchievementCard(ach, rc)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, rc: RafiqPalette) {
    val isUnlocked = achievement.unlocked
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isUnlocked) 3.dp else 1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) rc.card else rc.bg)
            .border(
                1.dp,
                if (isUnlocked) rc.gold.copy(alpha = 0.15f) else rc.divider,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isUnlocked) achievement.emoji else "🔒",
                fontSize = 36.sp
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) rc.ink else rc.inkLight
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    achievement.description,
                    fontSize = 13.sp,
                    color = if (isUnlocked) rc.inkMed else rc.inkLight
                )
            }
            if (isUnlocked) {
                Text("✓", fontSize = 18.sp, color = rc.emerald, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun buildAchievements(
    currentStreak: Long, longestStreak: Long,
    todayQuran: Long, todayTasbeeh: Long
) = listOf(
    Achievement("first_day",     "🌱", "البداية", "أكمل أول يوم", currentStreak >= 1),
    Achievement("streak_3",      "🔥", "الثبات", "حافظ على سلسلة 3 أيام", longestStreak >= 3),
    Achievement("streak_7",      "⭐", "النجم", "حافظ على سلسلة 7 أيام", longestStreak >= 7),
    Achievement("streak_30",     "🏅", "المثابر", "حافظ على سلسلة 30 يوماً", longestStreak >= 30),
    Achievement("streak_100",    "👑", "الملتزم", "حافظ على سلسلة 100 يوم", longestStreak >= 100),
    Achievement("quran_1",       "📖", "قارئ القرآن", "اقرأ صفحة واحدة في يوم", todayQuran >= 1),
    Achievement("quran_5",       "📚", "حافظ الورد", "اقرأ 5 صفحات في يوم", todayQuran >= 5),
    Achievement("quran_20",      "📕", "ختمة الجزء", "اقرأ 20 صفحة في يوم", todayQuran >= 20),
    Achievement("tasbeeh_100",   "📿", "المسبّح", "سبّح 100 مرة في يوم", todayTasbeeh >= 100),
    Achievement("tasbeeh_1000",  "🌟", "المسبّح الدائم", "سبّح 1000 مرة في يوم", todayTasbeeh >= 1000),
)
