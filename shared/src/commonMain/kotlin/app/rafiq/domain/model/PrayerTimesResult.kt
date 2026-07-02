package app.rafiq.domain.model

data class PrayerTimesResult(
    val fajr:    Long,
    val sunrise: Long,
    val dhuhr:   Long,
    val asr:     Long,
    val maghrib: Long,
    val isha:    Long
)
