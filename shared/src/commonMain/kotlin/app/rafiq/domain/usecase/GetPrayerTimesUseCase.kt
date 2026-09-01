package app.rafiq.domain.usecase

import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiq.domain.model.RafiqResult
import app.rafiq.domain.model.ErrorType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * حساب مواقيت الصلاة.
 *
 * كان هنا احتياطيّ صامت إلى إحداثيات مكة (21.3891/39.8579)، وفي طبقة
 * الأندرويد احتياطيّ ثانٍ مختلف إلى السليمانية. النتيجة أن مستخدماً في
 * القاهرة كان يرى أوقاتاً ليست أوقاته، ويظنّها أوقاته، فيصلّي عليها.
 *
 * لا احتياطيّ بعد اليوم: بلا إحداثيات = خطأ صريح من نوع LOCATION،
 * وعلى الواجهة أن تطلب الموقع بدل أن تعرض رقماً مطمئناً كاذباً.
 */
class GetPrayerTimesUseCase(
    private val calculator: PrayerTimeCalculator
) {
    /** إحداثيات صفرية = «لا موقع» في هذا التطبيق (خط الطول والعرض معاً). */
    private fun missingLocation(lat: Double, lng: Double) = lat == 0.0 && lng == 0.0

    private fun <T> noLocation(): RafiqResult<T> = RafiqResult.Error(
        message = "لم يُحدَّد موقعك بعد، ولا يمكن حساب المواقيت بدونه.",
        type    = ErrorType.LOCATION,
    )

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
        if (missingLocation(lat, lng)) return@withContext noLocation()
        try {
            val result = calculator.calculate(lat, lng, method, elevation, madhab, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
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
        if (missingLocation(lat, lng)) return@withContext noLocation()
        try {
            val result = calculator.calculateForTomorrow(lat, lng, method, elevation, madhab, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)
            RafiqResult.Success(result)
        } catch (e: Exception) {
            RafiqResult.Error(
                message = e.message ?: "خطأ في حساب مواقيت الصلاة للغد",
                type    = ErrorType.GENERIC
            )
        }
    }
}
