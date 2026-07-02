package app.rafiq.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.rafiq.db.RafiqDatabase
import app.rafiq.domain.model.UserPrefsInfo
import app.rafiq.domain.model.toDomain
import app.rafiq.domain.repository.PrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PrefsRepositoryImpl(private val db: RafiqDatabase) : PrefsRepository {

    override fun getPrefs(): Flow<UserPrefsInfo> =
        db.userPrefsQueries.get()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { it?.toDomain() ?: UserPrefsInfo(
                theme = "system", dynamicColor = true, fontScale = 1.0,
                notificationsEnabled = true, glassLevel = "fake",
                gamificationVisible = true, prayerMethod = "mwl",
                locale = "ar", hijriOffset = 0L, reducedMotion = false,
                highContrast = false, soundProfile = "beads", hapticsEnabled = true,
                lastKnownCity = "", lastKnownLat = 0.0, lastKnownLng = 0.0,
                onboardingCompleted = false,
                fajrOffset = 0, dhuhrOffset = 0, asrOffset = 0, maghribOffset = 0, ishaOffset = 0,
                elevation = 0.0, madhab = "shafi", numerals = "arabic"
            )}

    override suspend fun updateTheme(theme: String, dynamic: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateTheme(theme, if (dynamic) 1L else 0L)
        }

    override suspend fun updateFontScale(scale: Double) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateFontScale(scale)
        }

    override suspend fun updateNotifications(enabled: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateNotifications(if (enabled) 1L else 0L)
        }

    override suspend fun updateGamification(visible: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateGamification(if (visible) 1L else 0L)
        }

    override suspend fun updatePrayerMethod(method: String) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updatePrayerMethod(method)
        }

    override suspend fun updateLocale(locale: String) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateLocale(locale)
        }

    override suspend fun updateLocation(city: String, lat: Double, lng: Double) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateLocation(city, lat, lng)
        }

    override suspend fun updateOnboarding(completed: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateOnboarding(if (completed) 1L else 0L)
        }

    override suspend fun updateAccessibility(reducedMotion: Boolean, highContrast: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateAccessibility(
                if (reducedMotion) 1L else 0L,
                if (highContrast) 1L else 0L
            )
        }

    override suspend fun updateSound(profile: String, haptics: Boolean) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateSound(profile, if (haptics) 1L else 0L)
        }

    override suspend fun updatePrayerOffsets(fajr: Int, dhuhr: Int, asr: Int, maghrib: Int, isha: Int) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updatePrayerOffsets(
                fajr = fajr.toLong(),
                dhuhr = dhuhr.toLong(),
                asr = asr.toLong(),
                maghrib = maghrib.toLong(),
                isha = isha.toLong()
            )
        }

    override suspend fun updateElevation(elevation: Double) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateElevation(elevation)
        }

    override suspend fun updateNumerals(numerals: String) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateNumerals(numerals)
        }

    override suspend fun updateMadhab(madhab: String) =
        withContext(Dispatchers.IO) {
            db.userPrefsQueries.updateMadhab(madhab)
        }
}
