package app.rafiqaldhikr.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiq.domain.model.RafiqResult
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.usecase.GetPrayerTimesUseCase
import app.rafiqaldhikr.ui.theme.LocalMeeqat
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.litBy
import app.rafiqaldhikr.ui.theme.meeqatOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * يحسب مواقيت اليوم مرّة واحدة لكل التطبيق.
 *
 * الصفحة الرئيسية تحسبها أصلاً لعرضها، لكن شريط الميقات يظهر في كل
 * شاشة — بما فيها ما لا يعرض مواقيت — فيحتاج مصدراً مستقلاً.
 * الحساب محلّي بالكامل (بلا شبكة) ورخيص، فلا ازدواج تكلفة يُذكر.
 */
class MeeqatViewModel(
    private val prefsRepo:      PrefsRepository,
    private val getPrayerTimes: GetPrayerTimesUseCase,
) : ViewModel() {

    private val _times = MutableStateFlow<PrayerTimesResult?>(null)
    val times: StateFlow<PrayerTimesResult?> = _times.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val prefs = prefsRepo.getPrefs().first()
        val res = getPrayerTimes(
            lat           = prefs.lastKnownLat,
            lng           = prefs.lastKnownLng,
            method        = prefs.prayerMethod,
            elevation     = prefs.elevation,
            madhab        = prefs.madhab,
            fajrOffset    = prefs.fajrOffset,
            dhuhrOffset   = prefs.dhuhrOffset,
            asrOffset     = prefs.asrOffset,
            maghribOffset = prefs.maghribOffset,
            ishaOffset    = prefs.ishaOffset,
        )
        _times.value = (res as? RafiqResult.Success)?.data
    }
}

/**
 * يوفّر [LocalMeeqat] لكل ما بداخله، ويصبغ لوحة الألوان بضوء الوقت.
 *
 * النبضة كل 30 ثانية لا كل ثانية: موضع العلامة على شريط بعرض الشاشة
 * لا يتحرّك بمقدار بكسل واحد في أقلّ من ذلك، فتحديثه أسرع إهدارٌ
 * للبطارية بلا فرق مرئي.
 */
@Composable
fun ProvideMeeqat(
    vm: MeeqatViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val times by vm.times.collectAsStateWithLifecycle()
    val base  = LocalRafiqColors.current

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(30_000)
        }
    }

    val meeqat = remember(times, nowMs, base) {
        times?.let { meeqatOf(it, nowMs, base) } ?: app.rafiqaldhikr.ui.theme.UnresolvedMeeqat
    }
    val lit: RafiqPalette = remember(base, meeqat) { base.litBy(meeqat) }

    CompositionLocalProvider(
        LocalMeeqat      provides meeqat,
        LocalRafiqColors provides lit,
        content          = content,
    )
}
