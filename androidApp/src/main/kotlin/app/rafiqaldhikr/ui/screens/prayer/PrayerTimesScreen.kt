package app.rafiqaldhikr.ui.screens.prayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.ErrorState
import app.rafiqaldhikr.ui.components.IcoCheck
import app.rafiqaldhikr.ui.components.IcoCompass
import app.rafiqaldhikr.ui.components.IcoMosque
import app.rafiqaldhikr.ui.components.IcoPin
import app.rafiqaldhikr.ui.components.IcoRefresh
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.rafiqaldhikr.ui.components.RafiqBackButton

@Composable
fun PrayerTimesScreen(
    navController: NavHostController,
    viewModel: PrayerTimesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = { viewModel.refresh() }
            )
            else -> PrayerTimesContent(
                state = state,
                onMarkPrayed = { name, prayed -> viewModel.markPrayed(name, prayed) },
                onRefresh = { viewModel.refresh() },
                navController = navController,
                rc = rc
            )
        }
    }
}

@Composable
private fun PrayerTimesContent(
    state: PrayerTimesViewModel.UiState,
    onMarkPrayed: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    navController: NavHostController,
    rc: RafiqPalette,
    modifier: Modifier = Modifier
) {
    val times = state.times ?: return

    val prayers = listOf(
        Triple("fajr",    stringResource(R.string.fajr),    times.fajr),
        Triple("dhuhr",   stringResource(R.string.dhuhr),   times.dhuhr),
        Triple("asr",     stringResource(R.string.asr),     times.asr),
        Triple("maghrib", stringResource(R.string.maghrib), times.maghrib),
        Triple("isha",    stringResource(R.string.isha),    times.isha)
    )

    val prayedCount = state.prayerLogs.count { it.prayed }

    Column(
        modifier = modifier
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
                text = stringResource(R.string.prayer_times_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = rc.emerald
            )

            RafiqBackButton(onClick = { navController.popBackStack() })
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (state.city.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IcoPin(14.dp, rc.inkMed)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    state.city,
                                    fontSize = 12.sp,
                                    color = rc.inkMed
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onRefresh) {
                            IcoRefresh(22.dp, rc.emerald)
                        }
                        IconButton(onClick = { navController.navigate(RafiqRoute.Qibla.route) }) {
                            IcoCompass(22.dp, rc.emerald)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Prayer progress badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(rc.emerald)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "الصلوات المؤداة: $prayedCount / 5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            methodLabel(state.method),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Prayer cards
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                prayers.forEach { (key, name, timeMs) ->
                    val loggedPrayer = state.prayerLogs.find { it.prayerName == key }
                    val isPrayed = loggedPrayer?.prayed ?: false
                    val currentTimeMs = Clock.System.now().toEpochMilliseconds()
                    val isNext = !isPrayed && timeMs > currentTimeMs

                    PrayerCard(
                        name = name,
                        timeMs = timeMs,
                        isPrayed = isPrayed,
                        isNext = isNext,
                        onToggle = { onMarkPrayed(key, !isPrayed) },
                        rc = rc
                    )
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.times_approximate),
                    fontSize = 12.sp,
                    color = rc.inkLight,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PrayerCard(
    name:    String,
    timeMs:  Long,
    isPrayed: Boolean,
    isNext:   Boolean,
    onToggle: () -> Unit,
    rc: RafiqPalette
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isPrayed -> rc.cardPrayed
            isNext   -> rc.card
            else     -> rc.card
        },
        animationSpec = tween(300),
        label = "prayerCardBg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isNext) 4.dp else 2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isNext) rc.emerald else rc.gold.copy(alpha = 0.1f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onToggle() }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Prayer icon circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPrayed) rc.emerald.copy(alpha = 0.1f)
                                else if (isNext) rc.emerald.copy(alpha = 0.15f)
                                else rc.divider
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconTint = if (isPrayed || isNext) rc.emerald else rc.inkLight
                        if (isPrayed) IcoCheck(22.dp, iconTint) else IcoMosque(22.dp, iconTint)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = rc.ink
                        )
                        if (isNext) {
                            Text(
                                "الصلاة التالية",
                                fontSize = 12.sp,
                                color = rc.emerald
                            )
                        } else if (isPrayed) {
                            Text(
                                "تم الأداء ✓",
                                fontSize = 12.sp,
                                color = rc.emerald
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(timeMs, app.rafiqaldhikr.ui.utils.LocalArabicNumerals.current),
                        style = app.rafiqaldhikr.ui.theme.NumbersStyle,
                        fontSize = 18.sp,
                        color = if (isNext) rc.emerald
                                else rc.inkMed
                    )
                    Spacer(Modifier.width(12.dp))
                    Checkbox(
                        checked = isPrayed,
                        onCheckedChange = null, // handled by clickable parent
                        colors = CheckboxDefaults.colors(
                            checkedColor = rc.emerald,
                            uncheckedColor = rc.inkLight
                        )
                    )
                }
            }

            // Highlight bar for next prayer
            if (isNext) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(rc.emerald)
                )
            }
        }
    }
}

private fun formatTime(epochMs: Long, arabic: Boolean): String {
    val sdf = SimpleDateFormat("hh:mm a", if (arabic) Locale("ar") else Locale.US)
    return sdf.format(Date(epochMs))
}

private fun methodLabel(method: String): String = when (method) {
    "umm_al_qura" -> "طريقة أم القرى"
    "egyptian"    -> "الطريقة المصرية"
    "karachi"     -> "كراتشي"
    "mwl"         -> "رابطة العالم الإسلامي"
    "isna"        -> "أمريكا الشمالية"
    "turkey"      -> "تركيا"
    else          -> method
}


