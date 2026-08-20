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
import app.rafiqaldhikr.ui.components.CityPickerSheet
import app.rafiqaldhikr.ui.components.NeedsLocation
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar

@Composable
fun PrayerTimesScreen(
    navController: NavHostController,
    viewModel: PrayerTimesViewModel = koinViewModel(),
    locationVm: app.rafiqaldhikr.ui.components.LocationRequestViewModel = koinViewModel(),
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
            // قبل هذا كانت تُعرض هنا مواقيت السليمانية لكل من لم يمنح الإذن
            state.needsLocation -> NeedsLocation(
                message = "مواقيت الصلاة تُحسب من موقعك، ولا يصحّ حسابها بموقع غيرك."
            )
            state.error != null -> ErrorState(
                message = state.error!!,
                onRetry = { viewModel.refresh() }
            )
            else -> PrayerTimesContent(
                state = state,
                onMarkPrayed = { name, prayed -> viewModel.markPrayed(name, prayed) },
                onRefresh = { viewModel.refresh() },
                onPickCity = { locationVm.saveCity(it) },
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
    onPickCity: (app.rafiq.domain.model.City) -> Unit,
    navController: NavHostController,
    rc: RafiqPalette,
    modifier: Modifier = Modifier
) {
    val times = state.times ?: return
    var pickingCity by remember { mutableStateOf(false) }

    if (pickingCity) {
        CityPickerSheet(
            onDismiss = { pickingCity = false },
            onPick    = { city -> onPickCity(city); pickingCity = false },
        )
    }

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
        RafiqTopBar(
            title  = stringResource(R.string.prayer_times_title),
            onBack = {navController.popBackStack()},
        )

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
                    // المدينة قابلة للنقر: من يسافر، أو يختار مدينة خاطئة، كان
                    // يبقى عليها بلا مخرج. والاسم معروضٌ دائماً — فيرى المستخدم
                    // على أي أساس حُسِبت أوقاته بدل أن يُخمَّن له.
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RafiqShape.chip)
                            .clickable { pickingCity = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IcoPin(14.dp, rc.inkMed)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            state.city.ifEmpty { "حدّد مدينتك" },
                            color = rc.inkMed, style = RafiqType.caption,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("تغيير", color = rc.emerald, style = RafiqType.micro)
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
                        .clip(RafiqShape.item)
                        .background(rc.emerald)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الصلوات المؤداة: $prayedCount / 5",
                            fontWeight = FontWeight.Bold,
                            color = rc.onEmerald, style = RafiqType.bodyS)
                        Text(
                            methodLabel(state.method) + " · " + madhabLabel(state.madhab),
                            color = Color.White.copy(alpha = 0.8f), style = RafiqType.caption,
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
                Text(stringResource(R.string.times_approximate),
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 4.dp), style = RafiqType.caption)
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
            .clip(RafiqShape.card)
            .background(bgColor)
            .border(
                1.dp,
                if (isNext) rc.emerald else rc.gold.copy(alpha = BorderIdle),
                RafiqShape.card
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
                        Text(name,
                            fontWeight = FontWeight.Bold,
                            color = rc.ink, style = RafiqType.titleM)
                        if (isNext) {
                            Text("الصلاة التالية",
                                color = rc.emerald, style = RafiqType.caption)
                        } else if (isPrayed) {
                            Text("تم الأداء ✓",
                                color = rc.emerald, style = RafiqType.caption)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        formatTime(timeMs, app.rafiqaldhikr.ui.utils.LocalArabicNumerals.current),
                        style = app.rafiqaldhikr.ui.theme.NumbersStyle,
                        fontSize = 19.sp,
                        color = if (isNext) rc.emerald else rc.ink
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

// الدالتان صارتا @Composable لتتمكّنا من stringResource: كانتا تُرجعان
// نصّاً عربياً ثابتاً فيظهر بالعربية حتى في الواجهة الإنجليزية.
@Composable
private fun madhabLabel(madhab: String): String =
    stringResource(if (madhab == "hanafi") R.string.madhab_hanafi else R.string.madhab_shafi)

@Composable
private fun methodLabel(method: String): String {
    // فرع else يعيد المفتاح الخام لطريقة غير معروفة، وهو نصّ لا معرّف
    // مورد — فلا يصحّ حشره داخل stringResource مع البقية.
    val res = when (method) {
        "umm_al_qura" -> R.string.method_umm_al_qura
        "egyptian"    -> R.string.method_egyptian
        "karachi"     -> R.string.method_karachi
        "mwl"         -> R.string.method_mwl
        "isna"        -> R.string.method_isna
        "turkey"      -> R.string.method_turkey
        else          -> null
    }
    return if (res != null) stringResource(res) else method
}


