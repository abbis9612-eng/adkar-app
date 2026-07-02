package app.rafiq.domain.model

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * حساب مواقيت الصلاة محلياً بالكامل (بدون إنترنت) بمكتبة adhan2 متعددة المنصات —
 * يعمل على أندرويد وiOS من نفس الكود، ويحافظ على قاعدة "التطبيق يعمل offline دائماً".
 */
class PrayerTimeCalculator {

    suspend fun calculate(
        lat: Double, lng: Double, method: String,
        elevation: Double = 0.0, madhab: String = "shafi",
        fajrOffset: Int = 0, dhuhrOffset: Int = 0, asrOffset: Int = 0,
        maghribOffset: Int = 0, ishaOffset: Int = 0
    ): PrayerTimesResult = calculateForDate(
        today(), lat, lng, method, elevation, madhab,
        fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset
    )

    suspend fun calculateForTomorrow(
        lat: Double, lng: Double, method: String,
        elevation: Double = 0.0, madhab: String = "shafi",
        fajrOffset: Int = 0, dhuhrOffset: Int = 0, asrOffset: Int = 0,
        maghribOffset: Int = 0, ishaOffset: Int = 0
    ): PrayerTimesResult = calculateForDate(
        today().plus(1, DateTimeUnit.DAY), lat, lng, method, elevation, madhab,
        fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset
    )

    private fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun calculateForDate(
        date: LocalDate,
        lat: Double, lng: Double, method: String,
        elevation: Double, madhab: String,
        fajrOffset: Int, dhuhrOffset: Int, asrOffset: Int,
        maghribOffset: Int, ishaOffset: Int
    ): PrayerTimesResult {
        val params = methodParams(method).copy(
            madhab = if (madhab == "hanafi") Madhab.HANAFI else Madhab.SHAFI
        )
        val times = PrayerTimes(
            Coordinates(lat, lng),
            DateComponents(date.year, date.monthNumber, date.dayOfMonth),
            params
        )

        // تصحيح الارتفاع: الشروق أبكر والغروب أوخر بحسب ارتفاع الموقع
        val elevCorrMs = elevationCorrectionMinutes(elevation) * 60_000L

        return PrayerTimesResult(
            fajr    = times.fajr.toEpochMilliseconds()    + fajrOffset    * 60_000L,
            sunrise = times.sunrise.toEpochMilliseconds() - elevCorrMs,
            dhuhr   = times.dhuhr.toEpochMilliseconds()   + dhuhrOffset   * 60_000L,
            asr     = times.asr.toEpochMilliseconds()     + asrOffset     * 60_000L,
            maghrib = times.maghrib.toEpochMilliseconds() + maghribOffset * 60_000L + elevCorrMs,
            isha    = times.isha.toEpochMilliseconds()    + ishaOffset    * 60_000L
        )
    }

    private fun methodParams(method: String) = when (method) {
        "umm_al_qura" -> CalculationMethod.UMM_AL_QURA.parameters
        "egyptian"    -> CalculationMethod.EGYPTIAN.parameters
        "isna"        -> CalculationMethod.NORTH_AMERICA.parameters
        "karachi"     -> CalculationMethod.KARACHI.parameters
        "turkey"      -> CalculationMethod.TURKEY.parameters
        else          -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
    }

    private fun elevationCorrectionMinutes(elevationMeters: Double): Int {
        if (elevationMeters <= 0.0) return 0
        val seconds = (elevationMeters / 100.0) * 37.5
        return (seconds / 60.0).toInt()
    }
}
