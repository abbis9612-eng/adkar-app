package app.rafiq.domain.model

data class PrayerTimesResult(
    val fajr:    Long,
    val sunrise: Long,
    val dhuhr:   Long,
    val asr:     Long,
    val maghrib: Long,
    val isha:    Long
)

expect class PrayerTimeCalculator() {
    suspend fun calculate(lat: Double, lng: Double, method: String, elevation: Double = 0.0, madhab: String = "shafi", fajrOffset: Int = 0, dhuhrOffset: Int = 0, asrOffset: Int = 0, maghribOffset: Int = 0, ishaOffset: Int = 0): PrayerTimesResult
    suspend fun calculateForTomorrow(lat: Double, lng: Double, method: String, elevation: Double = 0.0, madhab: String = "shafi", fajrOffset: Int = 0, dhuhrOffset: Int = 0, asrOffset: Int = 0, maghribOffset: Int = 0, ishaOffset: Int = 0): PrayerTimesResult
}
