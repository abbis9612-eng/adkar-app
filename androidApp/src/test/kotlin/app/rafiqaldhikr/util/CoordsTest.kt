package app.rafiqaldhikr.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** حارس القاعدة: صفر/صفر معاً = لا موقع. أي شيء آخر موقعٌ صالح. */
class CoordsTest {

    @Test
    fun `صفر وصفر معاً يعني لا موقع`() {
        assertNull(coordsOrNull(0.0, 0.0))
    }

    @Test
    fun `خط عرض صفر وحده موقع صالح`() {
        assertEquals(Coords(0.0, 101.45), coordsOrNull(0.0, 101.45))
    }

    @Test
    fun `خط طول صفر وحده موقع صالح`() {
        // خط غرينتش يمرّ فعلاً على مدن مأهولة — الصفر هنا ليس غياباً
        assertEquals(Coords(51.48, 0.0), coordsOrNull(51.48, 0.0))
    }

    @Test
    fun `إحداثيات سالبة موقع صالح`() {
        assertEquals(Coords(-6.17, 106.82), coordsOrNull(-6.17, 106.82))
    }
}
