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
import app.rafiqaldhikr.ui.utils.toEasternArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RIcon

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
                message = stringResource(R.string.prayer_needs_location)
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

    /*  الشروقُ صفٌّ لا صلاة: حدٌّ حقيقيٌّ في اليوم (آخرُ وقت الفجر وأوّلُ
        الضحى)، ويُعرض ولا يُسجَّل — فلا مربّعَ تسجيلٍ بجانبه. */
    val rows = listOf(
        PrayerRowData("fajr",    stringResource(R.string.fajr),    times.fajr,    LightKey.NIGHT, true),
        PrayerRowData("sunrise", stringResource(R.string.sunrise),                          times.sunrise, LightKey.DAWN,  false),
        PrayerRowData("dhuhr",   stringResource(R.string.dhuhr),   times.dhuhr,   LightKey.DAY,   true),
        PrayerRowData("asr",     stringResource(R.string.asr),     times.asr,     LightKey.WARM,  true),
        PrayerRowData("maghrib", stringResource(R.string.maghrib), times.maghrib, LightKey.DUSK,  true),
        PrayerRowData("isha",    stringResource(R.string.isha),    times.isha,    LightKey.NIGHT, true),
    )

    /*  «الصلاةُ التالية» كانت تُحسب داخل الحلقة: `!isPrayed && timeMs > now`
        — أي كلُّ صلاةٍ لم يحن وقتُها، فتُوسَم بها اثنتان أو ثلاث معاً.
        وهي واحدةٌ بالتعريف: أوّلُ ما هو آتٍ. */
    val nowMs = Clock.System.now().toEpochMilliseconds()
    val nextKey = rows.firstOrNull { it.loggable && it.timeMs > nowMs }?.key

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
                            state.city.ifEmpty { stringResource(R.string.hub_set_city) },
                            color = rc.inkMed, style = RafiqType.caption,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.action_change), color = rc.emerald, style = RafiqType.micro)
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
                        .background(rc.emeraldFill)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            /*  «٥» كانت مكتوبةً بالأرقام العربية مهما اختار
                                المستخدم لغةَ الأرقام — فيقرأ «3 / ٥».  */
                            stringResource(
                                R.string.prayer_logged_count,
                                prayedCount.localized(LocalArabicNumerals.current),
                                5.localized(LocalArabicNumerals.current),
                            ),
                            fontWeight = FontWeight.Bold,
                            color = rc.onEmeraldFill, style = RafiqType.bodyS)
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
                rows.forEach { row ->
                    PrayerCard(
                        row      = row,
                        isPrayed = row.loggable &&
                            (state.prayerLogs.find { it.prayerName == row.key }?.prayed ?: false),
                        isNext   = row.key == nextKey,
                        isPast   = row.timeMs <= nowMs,
                        onToggle = { current -> onMarkPrayed(row.key, !current) },
                        rc = rc,
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

/* ── صفُّ الصلاة ─────────────────────────────────────────────────

   كانت خمسُ بطاقاتٍ فيها أيقونةُ مسجدٍ واحدةٌ مكرَّرةٌ خمسَ مرّات — لا
   تميّز شيئاً. ولوحةُ التطبيق فيها سلَّمُ ضوءٍ مبنيٌّ لهذا بالضبط:
   أسماءُ الصلوات كلُّها أسماءُ حالات ضوء. فصار لكلِّ صلاةٍ مربّعُ ضوء
   وقتها — نيليٌّ للفجر والعشاء، ذهبيٌّ للظهر، نحاسيٌّ للعصر والمغرب.
──────────────────────────────────────────────────────────────── */

enum class LightKey { NIGHT, DAWN, DAY, WARM, DUSK }

data class PrayerRowData(
    val key: String,
    val name: String,
    val timeMs: Long,
    val light: LightKey,
    /** الشروقُ ليس صلاةً فلا يُسجَّل — ولا مربّعَ تسجيلٍ بجانبه. */
    val loggable: Boolean,
)

@Composable
private fun PrayerCard(
    row: PrayerRowData,
    isPrayed: Boolean,
    isNext: Boolean,
    isPast: Boolean,
    onToggle: (Boolean) -> Unit,
    rc: RafiqPalette,
) {
    val ar = LocalArabicNumerals.current
    val tint = when (row.light) {
        LightKey.NIGHT -> rc.lightNight
        LightKey.DAWN  -> rc.lightDusk
        LightKey.DAY   -> rc.goldLight
        LightKey.WARM  -> rc.lightDusk
        LightKey.DUSK  -> rc.lightDusk
    }
    val wash = when (row.light) {
        LightKey.NIGHT -> rc.tintNight
        LightKey.DAY   -> rc.tintGold
        else           -> rc.tintDusk
    }
    val bg by animateColorAsState(
        if (isNext) rc.emeraldPastel else rc.card, tween(300), label = "prayerRowBg",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .background(bg)
            .border(1.dp, if (isNext) rc.emerald.copy(alpha = 0.30f) else rc.divider, RafiqShape.card)
            .then(if (row.loggable) Modifier.clickable { onToggle(isPrayed) } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(wash),
            contentAlignment = Alignment.Center,
        ) {
            RafiqIcon(
                when (row.light) {
                    LightKey.NIGHT -> RIcon.Moon
                    LightKey.DAY   -> RIcon.Sun
                    else           -> RIcon.Sunset
                },
                20.dp, tint,
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(row.name, style = RafiqType.titleM,
                color = if (isNext) rc.emerald else rc.ink)
            Text(
                when {
                    isPrayed -> stringResource(R.string.prayer_logged)
                    isNext   -> stringResource(R.string.next_prayer)
                    !row.loggable && isPast -> stringResource(R.string.prayer_past)
                    !row.loggable -> stringResource(R.string.prayer_fajr_edge)
                    isPast   -> stringResource(R.string.prayer_time_passed)
                    else     -> stringResource(R.string.action_later)
                },
                style = RafiqType.caption,
                color = if (isPrayed || isNext) rc.emerald else rc.inkMed,
            )
        }
        Text(
            formatTime(row.timeMs, ar),
            style = NumbersStyle,
            fontSize = 18.sp,
            color = if (isNext) rc.emerald else if (isPast) rc.inkMed else rc.ink,
        )
        if (row.loggable) {
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .size(27.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isPrayed) rc.emerald else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (isPrayed) rc.emerald else rc.divider,
                        RoundedCornerShape(9.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isPrayed) RafiqIcon(RIcon.Check, 15.dp, rc.onEmerald)
            }
        }
    }
}


/**
 * كان `SimpleDateFormat` مع `Locale("ar")` — وهو لا يعطي أرقاماً عربيةً
 * شرقية على أندرويد الحديث: ICU يختار اللاتينية لـ`ar` ما لم يُطلب
 * `ar-u-nu-arab` صراحةً. فكان المستخدم يقرأ «03:59 ص» في تطبيقٍ كلُّه
 * عربيّ. الآن تُنسَّق بالإنكليزية ثمّ تُحوَّل الأرقامُ يدوياً، والعلامةُ
 * «ص/م» تُكتب صراحةً لا تُترك للنظام.
 */
private fun formatTime(epochMs: Long, arabic: Boolean): String {
    val sdf = SimpleDateFormat("hh:mm", Locale.US)
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
    val pm = cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.PM
    val body = sdf.format(Date(epochMs))
    return if (arabic) "${body.toEasternArabicNumerals()} ${if (pm) "م" else "ص"}"
    else "$body ${if (pm) "PM" else "AM"}"
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


