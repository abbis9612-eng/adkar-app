package app.rafiq.domain.repository

import app.rafiq.domain.model.UserPrefsInfo
import kotlinx.coroutines.flow.Flow

interface PrefsRepository {
    fun getPrefs(): Flow<UserPrefsInfo>
    suspend fun updateTheme(theme: String, dynamic: Boolean)
    suspend fun updateFontScale(scale: Double)
    suspend fun updateNotifications(enabled: Boolean)
    suspend fun updateGamification(visible: Boolean)
    suspend fun updatePrayerMethod(method: String)
    suspend fun updateLocale(locale: String)
    suspend fun updateLocation(city: String, lat: Double, lng: Double)
    suspend fun updateOnboarding(completed: Boolean)
    suspend fun updateAccessibility(reducedMotion: Boolean, highContrast: Boolean)
    suspend fun updateSound(profile: String, haptics: Boolean)
    suspend fun updatePrayerOffsets(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int)
    suspend fun updateElevation(elevation: Double)
    suspend fun updateMadhab(madhab: String)
}
