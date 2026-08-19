package app.rafiq.domain.usecase

import app.rafiq.domain.model.ErrorType
import app.rafiq.domain.model.PrayerTimeCalculator
import app.rafiq.domain.model.RafiqResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * أوّل اختبارات المستودع، ومكانها ليس صدفة.
 *
 * كان في التطبيق احتياطيّان صامتان للإحداثيات: مكة هنا، والسليمانية في
 * سبعة مواضع من طبقة الأندرويد. من لم يمنح إذن الموقع كان يرى مواقيت
 * مدينةٍ ليست مدينته، معروضةً كأنها مواقيته، ويصلّي عليها.
 *
 * هذه الاختبارات تمنع عودة ذلك الاحتياطيّ تحت أي اسم.
 */
class GetPrayerTimesUseCaseTest {

    private val useCase = GetPrayerTimesUseCase(PrayerTimeCalculator())

    private companion object {
        const val METHOD = "mwl"
        // بغداد — إحداثيات حقيقية للتحقق من المسار السليم
        const val BAGHDAD_LAT = 33.3152
        const val BAGHDAD_LNG = 44.3661
    }

    @Test
    fun `بلا إحداثيات يعطي خطأ موقع لا مواقيت افتراضية`() = runTest {
        val result = useCase(lat = 0.0, lng = 0.0, method = METHOD)

        val error = assertIs<RafiqResult.Error>(result)
        assertEquals(ErrorType.LOCATION, error.type)
    }

    @Test
    fun `بلا إحداثيات يعطي خطأ موقع لمواقيت الغد أيضاً`() = runTest {
        val result = useCase.getForTomorrow(lat = 0.0, lng = 0.0, method = METHOD)

        val error = assertIs<RafiqResult.Error>(result)
        assertEquals(ErrorType.LOCATION, error.type)
    }

    @Test
    fun `إحداثيات حقيقية تعطي مواقيت مرتّبة تصاعدياً`() = runTest {
        val result = useCase(BAGHDAD_LAT, BAGHDAD_LNG, METHOD)

        val times = assertIs<RafiqResult.Success<*>>(result).data
            as app.rafiq.domain.model.PrayerTimesResult

        assertTrue(times.fajr    < times.dhuhr,   "الفجر قبل الظهر")
        assertTrue(times.dhuhr   < times.asr,     "الظهر قبل العصر")
        assertTrue(times.asr     < times.maghrib, "العصر قبل المغرب")
        assertTrue(times.maghrib < times.isha,    "المغرب قبل العشاء")
    }

    @Test
    fun `خط عرض صفر مع خط طول غير صفر موقعٌ صالح لا غياب موقع`() = runTest {
        // نقطة على خط الاستواء قبالة سواحل إندونيسيا — lat = 0.0 وحدها
        // ليست "لا موقع"، والشرط يجب أن يكون على الاثنين معاً.
        val result = useCase(lat = 0.0, lng = 101.45, method = METHOD)

        assertIs<RafiqResult.Success<*>>(result)
    }
}
