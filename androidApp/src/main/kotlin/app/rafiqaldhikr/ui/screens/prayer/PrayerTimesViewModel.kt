package app.rafiqaldhikr.ui.screens.prayer

import androidx.lifecycle.ViewModel
import app.rafiqaldhikr.util.coordsOrNull
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.PrayerEntry
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiq.domain.model.RafiqResult
import app.rafiq.domain.repository.PrayerRepository
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.usecase.GetPrayerTimesUseCase
import app.rafiqaldhikr.service.PrayerAlarmManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class PrayerTimesViewModel(
    private val getPrayerTimes: GetPrayerTimesUseCase,
    private val prayerRepo:     PrayerRepository,
    private val prefsRepo:      PrefsRepository,
    private val progressRepo:   ProgressRepository,
    private val alarmManager:   PrayerAlarmManager
) : ViewModel() {

    data class UiState(
        val times:      PrayerTimesResult? = null,
        val prayerLogs: List<PrayerEntry>  = emptyList(),
        val method:     String             = "mwl",
        val madhab:     String             = "shafi",
        val city:       String             = "",
        val isLoading:  Boolean            = true,
        val error:      String?            = null,
        /** لا إحداثيات محفوظة — الشاشة تطلب الموقع بدل عرض أوقات ليست للمستخدم. */
        val needsLocation: Boolean         = false,
        /** صلواتُ المسجد في الأيّام السبعة الأخيرة — أقدمُها أوّلاً، واليومُ آخرُها. */
        val masjidWeek: List<Pair<kotlinx.datetime.LocalDate, Int>> = emptyList(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    /**
     * مجمِّعا هذه الشاشة.
     *
     * كان `refresh()` ينادي [loadData] بلا إلغاءِ ما قبله، فيتراكم مع كل
     * ضغطةٍ مجمِّعان دائمان على `viewModelScope`. وكلُّ واحدٍ منهما يُعيد
     * جدولةَ التنبيهات عند كل انبعاثٍ للتفضيلات — أي أن الضغط على «تحديث»
     * خمسَ مرّاتٍ يعني خمسَ جدولاتٍ متزامنة.
     */
    private var loadJob: kotlinx.coroutines.Job? = null
    private var logJob:  kotlinx.coroutines.Job? = null

    private fun loadData() {
        loadJob?.cancel()
        // ✅ v2.6: collect منفصلان — يمنع deadlock الـ collect المتداخل
        loadJob = viewModelScope.launch {
            prefsRepo.getPrefs().collectLatest { prefs ->
                if (prefs == null) return@collectLatest
                _uiState.update {
                    it.copy(method = prefs.prayerMethod, madhab = prefs.madhab, city = prefs.lastKnownCity)
                }

                val here = coordsOrNull(prefs.lastKnownLat, prefs.lastKnownLng)
                if (here == null) {
                    _uiState.update {
                        it.copy(times = null, isLoading = false, error = null, needsLocation = true)
                    }
                    return@collectLatest
                }
                /*  الإزاحاتُ الخمس كانت تسقط هنا وحدَها.
                 *
                 *  فكانت الشاشةُ تعرض مواقيتَ بلا تعديلِ صاحبها، ثم تُعيد
                 *  جدولةَ التنبيهات بها بنفس أرقام الطلب — فتدهس ما كان
                 *  `PrayerRescheduler` قد جدوله صحيحاً بالإزاحات. أي أنّ
                 *  فتحَ شاشة المواقيت وحدَه كان يُفسد تنبيهاتٍ سليمة.  */
                val result = getPrayerTimes(
                    here.lat, here.lng, prefs.prayerMethod,
                    elevation     = prefs.elevation,
                    madhab        = prefs.madhab,
                    fajrOffset    = prefs.fajrOffset,
                    dhuhrOffset   = prefs.dhuhrOffset,
                    asrOffset     = prefs.asrOffset,
                    maghribOffset = prefs.maghribOffset,
                    ishaOffset    = prefs.ishaOffset,
                )
                when (result) {
                    is RafiqResult.Success -> {
                        val times = result.data
                        _uiState.update { it.copy(times = times, isLoading = false, error = null, needsLocation = false) }
                        if (prefs.notificationsEnabled) {
                            alarmManager.scheduleAllForToday(
                                prayerTimes = mapOf(
                                    "fajr"    to times.fajr,
                                    "dhuhr"   to times.dhuhr,
                                    "asr"     to times.asr,
                                    "maghrib" to times.maghrib,
                                    "isha"    to times.isha
                                ),
                                prayers = prefs.notifyPrayers,
                                morning = prefs.notifyMorning,
                                evening = prefs.notifyEvening,
                            )
                        }
                    }
                    is RafiqResult.Error ->
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    RafiqResult.Loading -> Unit
                }
            }
        }

        // ✅ coroutine منفصل للـ prayer logs — لا يحجب prefs collector.
        // ويتبع loadJob كي يُلغى معه عند التحديث بدل أن يتراكم.
        logJob?.cancel()
        logJob = viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            prayerRepo.getByDate(today).collect { logs ->
                _uiState.update { it.copy(prayerLogs = logs) }
            }
        }

        weekJob?.cancel()
        weekJob = viewModelScope.launch {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val days = (6 downTo 0).map { today.minus(kotlinx.datetime.DatePeriod(days = it)) }
            prayerRepo.getRange(days.first().toString(), today.toString()).collect { logs ->
                val byDay = logs.filter { it.inMasjid }.groupingBy { it.date }.eachCount()
                _uiState.update { s -> s.copy(masjidWeek = days.map { it to (byDay[it.toString()] ?: 0) }) }
            }
        }
    }

    private var weekJob: kotlinx.coroutines.Job? = null

    /**
     * نافذةُ اللوحة: لم تُسجَّل ← صلّيتُ ← في المسجد ← لم تُسجَّل.
     *
     * والضغطةُ الثالثةُ تمحو الصلاةَ والمسجدَ معاً — من ضغط خطأً يدور
     * إلى البداية بلا زرٍّ آخر.
     */
    fun cycle(prayerName: String) {
        val entry = _uiState.value.prayerLogs.find { it.prayerName == prayerName }
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        viewModelScope.launch {
            when {
                entry?.inMasjid == true -> {
                    prayerRepo.setInMasjid(today, prayerName, false)
                    prayerRepo.logPrayer(today, prayerName, false)
                }
                entry?.prayed == true -> prayerRepo.setInMasjid(today, prayerName, true)
                else -> prayerRepo.logPrayer(today, prayerName, true)
            }
            progressRepo.ensureExists(today)
            progressRepo.updatePrayers(today, prayerRepo.getPrayedCount(today, today))
        }
    }

    fun markPrayed(prayerName: String, prayed: Boolean) {
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            prayerRepo.logPrayer(today, prayerName, prayed)
            
            // ✅ تحديث التقدم اليومي للصلوات
            progressRepo.ensureExists(today)
            val prayedCount = prayerRepo.getPrayedCount(today, today)
            progressRepo.updatePrayers(today, prayedCount)
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadData()
    }
}
