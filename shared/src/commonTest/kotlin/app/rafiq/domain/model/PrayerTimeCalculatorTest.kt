package app.rafiq.domain.model

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * حسابُ المواقيت — ما لا يجوز أن ينكسر.
 *
 * أسماءُ الدوال ASCII: العربيةُ بين backticks تُنتج ملفات `.class` بأسماء
 * عربية، وتفشل على محلّيةٍ غير UTF-8.
 */
class PrayerTimeCalculatorTest {

    private val calc = PrayerTimeCalculator()

    private companion object {
        const val METHOD = "mwl"
        // ستوكهولم — 59.3° شمالاً، فوق خطّ العروض العليا بكثير.
        const val STOCKHOLM_LAT = 59.3293
        const val STOCKHOLM_LNG = 18.0686
        // مكّة — أوّلُ ما يُختبر عليه أيُّ حسابٍ للمواقيت.
        const val MECCA_LAT = 21.4225
        const val MECCA_LNG = 39.8262
    }

    /**
     * فوق خطّ العروض العليا يبقى الفجرُ والعشاءُ معرَّفين.
     *
     * `CalculationParameters` كانت بلا `highLatitudeRule` إطلاقاً. وفوق
     * ٤٨ درجةً لا تنحدر الشمسُ في الصيف إلى زاوية الحساب، فلا يعطي وقتاً
     * أو يعطي رقماً بلا معنى — وسكّانُ لندن وبرلين وستوكهولم وموسكو كلُّهم
     * فوق ذلك الخطّ.
     *
     * الشرطُ هنا ليس دقّةَ الرقم بل أن يبقى الترتيبُ صحيحاً واليومُ يوماً:
     * فجرٌ قبل الشروق، وعشاءٌ بعد المغرب، وكلُّها داخل أربعٍ وعشرين ساعة.
     */
    @Test
    fun highLatitudeStaysOrdered() = runTest {
        val t = calc.calculate(STOCKHOLM_LAT, STOCKHOLM_LNG, METHOD)
        assertTrue(t.fajr < t.sunrise, "الفجر ليس قبل الشروق في ستوكهولم")
        assertTrue(t.sunrise < t.dhuhr, "الشروق ليس قبل الظهر")
        assertTrue(t.dhuhr < t.asr, "الظهر ليس قبل العصر")
        assertTrue(t.asr < t.maghrib, "العصر ليس قبل المغرب")
        assertTrue(t.maghrib < t.isha, "المغرب ليس قبل العشاء")
        val span = t.isha - t.fajr
        assertTrue(span in 1..(24 * 3600_000L), "مدى اليوم غير معقول: $span ملّي")
    }

    /** الإزاحةُ تُغيّر الوقتَ فعلاً — وكانت تُمرَّر ولا تصل في شاشة المواقيت. */
    @Test
    fun offsetsShiftTheTime() = runTest {
        val plain = calc.calculate(MECCA_LAT, MECCA_LNG, METHOD)
        val moved = calc.calculate(MECCA_LAT, MECCA_LNG, METHOD, fajrOffset = 7)
        val diffMinutes = (moved.fajr - plain.fajr) / 60_000L
        assertTrue(diffMinutes == 7L, "إزاحةُ الفجر +٧ أعطت $diffMinutes دقيقة")
    }

    /** المذهبُ الحنفيّ يؤخّر العصر — فرقٌ حقيقيٌّ لا إعدادٌ صامت. */
    @Test
    fun hanafiDelaysAsr() = runTest {
        val shafi  = calc.calculate(MECCA_LAT, MECCA_LNG, METHOD, madhab = "shafi")
        val hanafi = calc.calculate(MECCA_LAT, MECCA_LNG, METHOD, madhab = "hanafi")
        assertTrue(hanafi.asr > shafi.asr, "العصرُ الحنفيّ ليس بعد الشافعيّ")
    }

    /** مواقيتُ الغد بعد مواقيت اليوم — لا نسخةٌ منها. */
    @Test
    fun tomorrowIsAfterToday() = runTest {
        val today    = calc.calculate(MECCA_LAT, MECCA_LNG, METHOD)
        val tomorrow = calc.calculateForTomorrow(MECCA_LAT, MECCA_LNG, METHOD)
        assertTrue(tomorrow.fajr > today.fajr, "فجرُ الغد ليس بعد فجر اليوم")
        val gap = tomorrow.fajr - today.fajr
        assertTrue(gap in (23 * 3600_000L)..(25 * 3600_000L), "الفارق غير يومٍ واحد: $gap")
    }

    /** كلُّ طريقةٍ معروضةٍ في الشاشة تُنتج مواقيتَ مرتّبة. */
    @Test
    fun everyOfferedMethodWorks() = runTest {
        val methods = listOf("mwl", "umm_al_qura", "egyptian", "karachi", "isna", "turkey")
        for (m in methods) {
            val t = calc.calculate(MECCA_LAT, MECCA_LNG, m)
            assertTrue(t.fajr < t.dhuhr && t.dhuhr < t.isha, "الطريقة $m أعطت ترتيباً خاطئاً")
        }
    }

    /**
     * «أم القرى» تبقى «أم القرى».
     *
     * كان في `DatabaseDriverFactory` سطرٌ يُنفَّذ في كل إقلاع يُبدّلها
     * إلى MWL. وهذا يمسك الطرفَ الآخر: أنّ الطريقتين تُنتجان وقتين
     * مختلفين فعلاً — فالتبديلُ لم يكن بلا أثر.
     */
    @Test
    fun ummAlQuraDiffersFromMwl() = runTest {
        val mwl = calc.calculate(MECCA_LAT, MECCA_LNG, "mwl")
        val uaq = calc.calculate(MECCA_LAT, MECCA_LNG, "umm_al_qura")
        assertTrue(mwl.isha != uaq.isha, "أم القرى وMWL أعطتا العشاءَ نفسَه")
    }
}
