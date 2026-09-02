package app.rafiqaldhikr

import app.rafiqaldhikr.ui.screens.hub.countdownParts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * قراءةُ العدّاد في الرئيسية.
 *
 * كان هذا الاختبار **ينسخ** منطقَ الدالّة نسخاً في دالّةٍ خاصّةٍ به بدل
 * أن يناديها — فلو تغيّرت الدالّةُ في الشاشة لبقي الاختبارُ أخضرَ يحرس
 * نسخةً ميّتة. الآن ينادي [countdownParts] نفسَها.
 *
 * والصياغةُ (ساعة · ساعتان · ٣ ساعات) لم تعد هنا: صارت `plurals` في
 * الموارد، وأندرويد يعرف مثنّى العربية وصيغتَي جمعها ويعرف الإنجليزية —
 * وهو ما لم يكن الكودُ المكتوب يدوياً يعرفه.
 *
 * الأسماء لاتينية عمداً: أسماءُ دوالِّ الاختبار العربية تنتج ملفات .class
 * بأسماء عربية، وتفشل على أنظمةٍ محليّتها POSIX.
 */
class HomeHubFormatTest {

    /** «٠٠:٥٦:٤٤» → ٠ ساعة و٥٦ دقيقة. الثواني تُهمَل. */
    @Test fun minutesOnly() = assertEquals(0 to 56, countdownParts("00:56:44"))

    @Test fun oneHourFourMinutes() = assertEquals(1 to 4, countdownParts("01:04:00"))
    @Test fun twoHours()           = assertEquals(2 to 0, countdownParts("02:00:12"))
    @Test fun threeHoursThirty()   = assertEquals(3 to 30, countdownParts("03:30:00"))
    @Test fun elevenHours()        = assertEquals(11 to 0, countdownParts("11:00:00"))

    /** بلا موقعٍ لا عدّاد — والسطر يُحذف بدل عرض «بعد —». */
    @Test fun noCountdownYet() = assertNull(countdownParts("—"))
    @Test fun zeroIsNull()     = assertNull(countdownParts("00:00:00"))
    @Test fun emptyIsNull()    = assertNull(countdownParts(""))

    /** نصٌّ لا يُقرأ رقماً لا يُسقط الشاشة. */
    @Test fun garbageIsNull() = assertNull(countdownParts("aa:bb:cc"))
}
