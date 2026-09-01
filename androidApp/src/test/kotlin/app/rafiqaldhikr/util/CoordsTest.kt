package app.rafiqaldhikr.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** حارس القاعدة: صفر/صفر معاً = لا موقع. أي شيء آخر موقعٌ صالح. */
class CoordsTest {

    @Test
    /** صفر وصفر معاً = لا موقع */
    fun zero_and_zero_together_means_no_location() {
        assertNull(coordsOrNull(0.0, 0.0))
    }

    @Test
    /** خط عرض صفر وحده موقع صالح */
    fun zero_latitude_alone_is_valid() {
        assertEquals(Coords(0.0, 101.45), coordsOrNull(0.0, 101.45))
    }

    @Test
    /** خط طول صفر وحده موقع صالح */
    fun zero_longitude_alone_is_valid() {
        // خط غرينتش يمرّ فعلاً على مدن مأهولة — الصفر هنا ليس غياباً
        assertEquals(Coords(51.48, 0.0), coordsOrNull(51.48, 0.0))
    }

    @Test
    /** إحداثيات سالبة موقع صالح */
    fun negative_coordinates_are_valid() {
        assertEquals(Coords(-6.17, 106.82), coordsOrNull(-6.17, 106.82))
    }
}
