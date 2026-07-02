package app.rafiqaldhikr.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.PrefsRepository
import app.rafiqaldhikr.service.PrayerRescheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val prefsRepo:   PrefsRepository,
    private val rescheduler: PrayerRescheduler
) : ViewModel() {

    private val _prefs = prefsRepo.getPrefs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val onboardingCompleted: StateFlow<Boolean?> = _prefs
        .map { it?.onboardingCompleted }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val theme: StateFlow<String> = _prefs
        .map { it?.theme ?: "system" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val dynamicColor: StateFlow<Boolean> = _prefs
        .map { it?.dynamicColor ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val fontScale: StateFlow<Float> = _prefs
        .map { it?.fontScale?.toFloat() ?: 1f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

    val gamificationVisible: StateFlow<Boolean> = _prefs
        .map { it?.gamificationVisible ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val notificationsEnabled: StateFlow<Boolean> = _prefs
        .map { it?.notificationsEnabled ?: true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /** لغة الأرقام: true = عربية ٠١٢٣ / false = إنجليزية 0123 */
    val arabicNumerals: StateFlow<Boolean> = _prefs
        .map { (it?.numerals ?: "arabic") == "arabic" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val elevation: StateFlow<Double> = _prefs
        .map { it?.elevation ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val madhab: StateFlow<String> = _prefs
        .map { it?.madhab ?: "shafi" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "shafi")

    val prayerMethod: StateFlow<String> = _prefs
        .map { it?.prayerMethod ?: "mwl" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "mwl")

    val reducedMotion: StateFlow<Boolean> = _prefs
        .map { it?.reducedMotion ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val fajrOffset: StateFlow<Int> = _prefs.map { it?.fajrOffset ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val dhuhrOffset: StateFlow<Int> = _prefs.map { it?.dhuhrOffset ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val asrOffset: StateFlow<Int> = _prefs.map { it?.asrOffset ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val maghribOffset: StateFlow<Int> = _prefs.map { it?.maghribOffset ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val ishaOffset: StateFlow<Int> = _prefs.map { it?.ishaOffset ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setTheme(theme: String, dynamic: Boolean) {
        viewModelScope.launch { prefsRepo.updateTheme(theme, dynamic) }
    }
    fun setFontScale(scale: Float) {
        viewModelScope.launch { prefsRepo.updateFontScale(scale.toDouble()) }
    }
    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.updateNotifications(enabled)
            // reschedule() يلغي التنبيهات عند التعطيل ويجدولها عند التفعيل
            runCatching { rescheduler.reschedule() }
        }
    }
    fun setGamification(visible: Boolean) {
        viewModelScope.launch { prefsRepo.updateGamification(visible) }
    }
    fun setAccessibility(reducedMotion: Boolean, highContrast: Boolean) {
        viewModelScope.launch { prefsRepo.updateAccessibility(reducedMotion, highContrast) }
    }
    fun completeOnboarding() {
        viewModelScope.launch { prefsRepo.updateOnboarding(true) }
    }
    fun setPrayerOffsets(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) {
        viewModelScope.launch {
            prefsRepo.updatePrayerOffsets(fajr, dhuhr, asr, maghrib, isha)
        }
    }
    fun setElevation(elevation: Double) {
        viewModelScope.launch { prefsRepo.updateElevation(elevation) }
    }
    fun setMadhab(madhab: String) {
        viewModelScope.launch { prefsRepo.updateMadhab(madhab) }
    }
    fun setNumerals(arabic: Boolean) {
        viewModelScope.launch { prefsRepo.updateNumerals(if (arabic) "arabic" else "latin") }
    }
    fun setPrayerMethod(method: String) {
        viewModelScope.launch { prefsRepo.updatePrayerMethod(method) }
    }
}
