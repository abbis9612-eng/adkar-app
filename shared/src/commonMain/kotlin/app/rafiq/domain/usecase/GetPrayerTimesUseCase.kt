package app.rafiq.domain.usecase

import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiq.domain.model.RafiqResult
import app.rafiq.domain.model.ErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetPrayerTimesUseCase(
    private val calculator: PrayerTimeCalculator
) {
    suspend operator fun invoke(
        lat:       Double,
        lng:       Double,
        method:    String,
        elevation: Double = 0.0,
        madhab:    String = "shafi",
        fajrOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): RafiqResult<PrayerTimesResult> = withContext(Dispatchers.Default) {
        try {
            val rLat = if (lat == 0.0 && lng == 0.0) 21.3891 else lat
            val rLng = if (lat == 0.0 && lng == 0.0) 39.8579 else lng
            val result = calculator.calculate(rLat, rLng, method, elevation, madhab, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
            RafiqResult.Success(result)
        } catch (e: Exception) {
            RafiqResult.Error(
                message = e.message ?: "خطأ في حساب مواقيت الصلاة",
                type    = ErrorType.GENERIC
            )
        }
    }

    suspend fun getForTomorrow(
        lat:       Double,
        lng:       Double,
        method:    String,
        elevation: Double = 0.0,
        madhab:    String = "shafi",
        fajrOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): RafiqResult<PrayerTimesResult> = withContext(Dispatchers.Default) {
        try {
            val rLat = if (lat == 0.0 && lng == 0.0) 21.3891 else lat
            val rLng = if (lat == 0.0 && lng == 0.0) 39.8579 else lng
            val result = calculator.calculateForTomorrow(rLat, rLng, method, elevation, madhab, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
            RafiqResult.Success(result)
        } catch (e: Exception) {
            RafiqResult.Error(
                message = e.message ?: "خطأ في حساب مواقيت الصلاة للغد",
                type    = ErrorType.GENERIC
            )
        }
    }
}
