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
import app.rafiqaldhikr.ui.utils.localizedDigits
import androidx.compose.runtime.saveable.rememberSaveable
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RIcon

/* ═══════════════════════════════════════════════════════════════════
   مواقيتُ الصلاة

 *  التوقيع: لوحةُ المسجد — نوافذُ تتقلّب أرقامُها، وعند الأذان تُجاب جملةً جملة

   اللوحةُ المعلّقة في كلّ مسجد، والنافذةُ هي الزرّ: «صلّيتُ» ثمّ «في
   المسجد». وعند الأذان تنفتح تحتها إجابةُ المؤذّن بلفظ حديث عمر في صحيح
   مسلم — انظر `MosqueBoard.kt` و`Ijaba.kt`.
═══════════════════════════════════════════════════════════════════ */

/** نافذةُ «حان الأذان»: من دخول الوقت اثنتا عشرةَ دقيقة. */
private const val ADHAN_WINDOW_MS = 12 * 60_000L

@Composable
fun PrayerTimesScreen(
    navController: NavHostController,
    viewModel: PrayerTimesViewModel = koinViewModel(),
    locationVm: app.rafiqaldhikr.ui.components.LocationRequestViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // التاريخُ الهجريُّ من حسابه الواحد في الرئيسية — بإزاحة صاحبه
    val homeVm: app.rafiqaldhikr.ui.screens.home.HomeViewModel = koinViewModel()
    val hijri = homeVm.uiState.collectAsStateWithLifecycle().value.hijriDate
        .localizedDigits(LocalArabicNumerals.current)
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
                onCycle = { viewModel.cycle(it) },
                hijri = hijri,
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
    onCycle: (String) -> Unit,
    hijri: String,
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
            }

            /*  اللوحة — وعند الأذان إجابةُ المؤذّن تحتها.
             *
             *  «حان الأذانُ» = اثنتا عشرةَ دقيقةً من دخول الوقت: التطبيقُ
             *  لا يسمع الأذان، يعرف وقتَه المحسوب، وأذانُ المسجد قد يتأخّر
             *  دقائق — فتبقى الإجابةُ مفتوحةً ويمشي صاحبُها على سرعته. */
            var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); nowMs = System.currentTimeMillis() } }
            val five = rows.filter { it.loggable }
            val adhan = five.lastOrNull { nowMs >= it.timeMs && nowMs - it.timeMs < ADHAN_WINDOW_MS }
            val next = five.firstOrNull { it.timeMs > nowMs }
            val nextMs = next?.timeMs ?: (times.fajr + 86_400_000L)
            val ctx = androidx.compose.ui.platform.LocalContext.current
            var ijaba by remember { mutableStateOf<Ijaba?>(null) }
            LaunchedEffect(Unit) { ijaba = IjabaStore.load(ctx) }
            var step by rememberSaveable(adhan?.key, adhan?.timeMs) { mutableIntStateOf(0) }

            MosqueBoard(
                windows = rows.map { BoardWindow(it.key, it.name, it.timeMs, it.loggable) },
                logs = state.prayerLogs,
                hijri = hijri,
                subtitle = methodLabel(state.method) + " · " + madhabLabel(state.madhab),
                nextKey = next?.key,
                nextName = next?.name ?: stringResource(R.string.fajr),
                leftSec = (nextMs - nowMs) / 1000,
                adhanKey = adhan?.key,
                adhanName = adhan?.name.orEmpty(),
                onWindow = onCycle,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                stringResource(R.string.board_legend),
                style = RafiqType.metaS, color = rc.inkMed,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            val ij = ijaba
            if (adhan != null && ij != null) {
                Text(stringResource(R.string.ijaba_title, adhan.name), style = RafiqType.titleL, color = rc.ink,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp))
                IjabaSteps(ij, step, onStep = { step++ })
            }
            Spacer(Modifier.height(14.dp))
            MasjidWeek(state.masjidWeek)

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.times_approximate),
                    color = rc.inkMed,
                    modifier = Modifier.padding(horizontal = 4.dp), style = RafiqType.caption)

                /*  الجملةُ الوحيدةُ النافعة من «إعدادات الودجت» المحذوفة.
                 *
                 *  تلك الشاشةُ حُذفت لأنّ مفاتيحَها الثلاثة `remember` لا
                 *  تُحفظ، واثنين منها لودجتَين لم تُكتبا قطّ. لكنّ
                 *  `PrayerWidget` **موجودٌ ويعمل**، وبحذفها لم يبقَ في
                 *  التطبيق كلِّه ما يقول للمستخدم إنّ له ودجتَ مواقيت.
                 *
                 *  والتطبيقُ لا يملك إضافتَه بنفسه — الودجتَ يضيفه صاحبُ
                 *  الجهاز من الشاشة الرئيسية. فكلُّ ما يصحّ هنا: أن نَدُلّ. */
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.times_widget_hint),
                    color = rc.inkLight,
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


