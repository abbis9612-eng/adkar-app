package app.rafiq.domain.model

actual class PrayerTimeCalculator actual constructor() {
    actual suspend fun calculate(lat: Double, lng: Double, method: String, elevation: Double, madhab: String, fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int, maghribOffset: Int, ishaOffset: Int): PrayerTimesResult {
        // TODO M3: Implement using Adhan Swift or cinterop
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return PrayerTimesResult(
            fajr    = now,
            sunrise = now,
            dhuhr   = now,
            asr     = now,
            maghrib = now,
            isha    = now
        )
    }

    actual suspend fun calculateForTomorrow(lat: Double, lng: Double, method: String, elevation: Double, madhab: String, fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int, maghribOffset: Int, ishaOffset: Int): PrayerTimesResult {
        // TODO M3: Implement using Adhan Swift or cinterop
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return PrayerTimesResult(
            fajr    = now,
            sunrise = now,
            dhuhr   = now,
            asr     = now,
            maghrib = now,
            isha    = now
        )
    }
}
