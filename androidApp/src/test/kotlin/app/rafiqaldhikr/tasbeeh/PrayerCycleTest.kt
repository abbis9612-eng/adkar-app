package app.rafiqaldhikr.tasbeeh

import app.rafiqaldhikr.ui.screens.tasbeeh.cycleComplete
import app.rafiqaldhikr.ui.screens.tasbeeh.cycleTotal
import app.rafiqaldhikr.ui.screens.tasbeeh.nextCycleStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * دورةُ دُبُر الصلاة: ثلاثٌ وثلاثون وثلاثٌ وثلاثون وأربعٌ وثلاثون = مائة.
 *
 * والخطأُ هنا لا يُسقط بناءً ولا يظهر في لقطةٍ — يظهر لمن يعدّ، فيقول
 * له العدّادُ «٣٣ من ١٠٠» وهو في أوّل التسبيح، أو «٩٩» وقد أتمّ المائة.
 * ولذلك يُقاس بالحساب لا بالنظر.
 */
class PrayerCycleTest {

    /** الأعدادُ كما في `adhkar_prayer.json`. */
    private val counts = listOf(33, 33, 34)

    @Test
    fun `المجموع يبدأ من صفر وينتهي عند مائة`() {
        assertEquals(0, cycleTotal(0, 0, counts))
        assertEquals(100, cycleTotal(2, 34, counts))
    }

    @Test
    fun `الخطواتُ الماضيةُ تُجمَع ولا تُنسى`() {
        //  أوّلُ لمسةٍ في الحمد: ثلاثٌ وثلاثون مضت وواحدة
        assertEquals(34, cycleTotal(1, 1, counts))
        //  أوّلُ لمسةٍ في التكبير: ستٌّ وستّون وواحدة
        assertEquals(67, cycleTotal(2, 1, counts))
    }

    @Test
    fun `حدودُ كلِّ خطوةٍ لا تتجاوز عددَها`() {
        assertEquals(33, cycleTotal(0, 33, counts))
        assertEquals(66, cycleTotal(1, 33, counts))
        //  عددٌ أكبرُ من الخطوة لا يسرق من التالية
        assertEquals(66, cycleTotal(1, 99, counts))
    }

    @Test
    fun `التمامُ عند آخرِ خطوةٍ لا قبلها`() {
        assertFalse(cycleComplete(0, 33, counts))
        assertFalse(cycleComplete(1, 33, counts))
        assertFalse(cycleComplete(2, 33, counts))
        assertTrue(cycleComplete(2, 34, counts))
    }

    @Test
    fun `الخطوةُ الأخيرةُ لا تالي لها`() {
        assertEquals(1, nextCycleStep(0, 3))
        assertEquals(2, nextCycleStep(1, 3))
        assertNull(nextCycleStep(2, 3))
    }

    @Test
    fun `دورةٌ فارغةٌ لا تُسقط الحساب`() {
        //  لو تعذّر قراءةُ الأصل، لا يُنهار العدّ
        assertEquals(5, cycleTotal(0, 5, emptyList()))
        assertFalse(cycleComplete(0, 5, emptyList()))
        assertNull(nextCycleStep(0, 0))
    }
}
