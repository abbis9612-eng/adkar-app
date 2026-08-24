package app.rafiqaldhikr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * صيغةُ العدّاد البشرية في الرئيسية.
 *
 * الأسماء لاتينية عمداً: أسماءُ دوالِّ الاختبار العربية تنتج ملفات .class
 * بأسماء عربية، وتفشل على أنظمةٍ محليّتها POSIX. الشرح في KDoc.
 */
class HomeHubFormatTest {

    private fun human(raw: String): String? {
        val p = raw.split(":")
        if (p.size != 3) return null
        val h = p[0].toIntOrNull() ?: return null
        val m = p[1].toIntOrNull() ?: return null
        if (h == 0 && m == 0) return null
        val hs = when (h) { 0 -> null; 1 -> "ساعة"; 2 -> "ساعتين"
                            in 3..10 -> "$h ساعات"; else -> "$h ساعة" }
        val ms = when (m) { 0 -> null; 1 -> "دقيقة"; 2 -> "دقيقتين"
                            in 3..10 -> "$m دقائق"; else -> "$m دقيقة" }
        return listOfNotNull(hs, ms).joinToString(" و")
    }

    /** «٠٠:٥٦:٤٤» كانت تُعرض كما هي — رقمُ مؤقّتٍ لا جملةُ رفيق. */
    @Test fun minutesOnly() = assertEquals("56 دقيقة", human("00:56:44"))

    /** التصريف العربي: ساعة · ساعتان · ٣ ساعات — لا «١ ساعة». */
    @Test fun oneHourFourMinutes() = assertEquals("ساعة و4 دقائق", human("01:04:00"))
    @Test fun twoHours()          = assertEquals("ساعتين", human("02:00:12"))
    @Test fun threeHours()        = assertEquals("3 ساعات و30 دقيقة", human("03:30:00"))
    @Test fun elevenHours()       = assertEquals("11 ساعة", human("11:00:00"))

    /** بلا موقعٍ لا عدّاد — والسطر يُحذف بدل عرض «بعد —». */
    @Test fun noCountdownYet()  = assertNull(human("—"))
    @Test fun zeroIsNull()      = assertNull(human("00:00:00"))
    @Test fun emptyIsNull()     = assertNull(human(""))
}
