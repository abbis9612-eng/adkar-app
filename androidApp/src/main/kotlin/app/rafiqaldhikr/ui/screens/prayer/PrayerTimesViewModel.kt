package app.rafiqaldhikr.ui.screens.prayer

import androidx.lifecycle.ViewModel
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
        val city:       String             = "",
        val isLoading:  Boolean            = true,
        val error:      String?            = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadData() }

    private fun loadData() {
        // ✅ v2.6: collect منفصلان — يمنع deadlock الـ collect المتداخل
        viewModelScope.launch {
            prefsRepo.getPrefs().collectLatest { prefs ->
                if (prefs == null) return@collectLatest
                _uiState.update { it.copy(method = prefs.prayerMethod, city = prefs.lastKnownCity) }

                val lat = prefs.lastKnownLat.takeIf { it != 0.0 } ?: 35.5558
                val lng = prefs.lastKnownLng.takeIf { it != 0.0 } ?: 45.4436
                val result = getPrayerTimes(
                    lat, lng, prefs.prayerMethod,
                    elevation = prefs.elevation,
                    madhab = prefs.madhab
                )
                when (result) {
                    is RafiqResult.Success -> {
                        val times = result.data
                        _uiState.update { it.copy(times = times, isLoading = false, error = null) }
                        if (prefs.notificationsEnabled) {
                            alarmManager.scheduleAllForToday(
                                mapOf(
                                    "fajr"    to times.fajr,
                                    "dhuhr"   to times.dhuhr,
                                    "asr"     to times.asr,
                                    "maghrib" to times.maghrib,
                                    "isha"    to times.isha
                                )
                            )
                        }
                    }
                    is RafiqResult.Error ->
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    RafiqResult.Loading -> Unit
                }
            }
        }

        // ✅ coroutine منفصل للـ prayer logs — لا يحجب prefs collector
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            prayerRepo.getByDate(today).collect { logs ->
                _uiState.update { it.copy(prayerLogs = logs) }
            }
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
