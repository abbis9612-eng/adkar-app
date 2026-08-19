package app.rafiqaldhikr.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.GregorianCalendar
import java.util.Calendar

/**
 * الحساب الذي حلّ محلّ DayOfWeek.FRIDAY — النوع الذي كان يُسقِط التطبيق
 * على أندرويد ٦ و٧. يُفحص هنا مقابل تقويم مستقل، على أربعين ألف يوم:
 * قبل الحقبة وبعدها، حتى لا يمرّ خطأ إشارة في باقي القسمة.
 */
class WeekdayTest {

    private fun isFridayByCalendar(epochDays: Int): Boolean {
        val cal = GregorianCalendar(1970, Calendar.JANUARY, 1)
        cal.add(Calendar.DAY_OF_MONTH, epochDays)
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }

    @Test
    fun `أول جمعة بعد الحقبة هي اليوم الأول`() {
        // ١٩٧٠-٠١-٠١ خميس، فـ ١٩٧٠-٠١-٠٢ جمعة
        assertFalse(isFridayFromEpochDays(0))
        assertTrue(isFridayFromEpochDays(1))
        assertFalse(isFridayFromEpochDays(2))
    }

    @Test
    fun `يطابق التقويم على أربعين ألف يوم حول الحقبة`() {
        for (d in -20_000..20_000) {
            assertTrue(
                "اختلاف عند يوم الحقبة $d",
                isFridayFromEpochDays(d) == isFridayByCalendar(d),
            )
        }
    }

    @Test
    fun `التواريخ قبل الحقبة لا تنكسر بباقي القسمة السالب`() {
        // ١٩٦٩-١٢-٢٦ جمعة، وهي اليوم ‎-6 من الحقبة
        assertTrue(isFridayFromEpochDays(-6))
        assertFalse(isFridayFromEpochDays(-5))
    }
}
