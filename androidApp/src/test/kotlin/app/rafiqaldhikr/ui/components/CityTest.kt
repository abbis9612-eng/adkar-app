package app.rafiqaldhikr.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * بحث المدن يجب أن يسامح ما يكتبه الناس فعلاً: بلا تشكيل، وبهمزة عادية
 * بدل همزة القطع، وبـ«ه» بدل «ة»، وبأل التعريف أو بدونها.
 */
class CityTest {

    private val alexandria = City("الإسكندرية", "Alexandria", "EG", "مصر", 31.2001, 29.9187)
    private val mecca      = City("مكة المكرمة", "Mecca", "SA", "السعودية", 21.4225, 39.8262)
    private val baghdad    = City("بغداد", "Baghdad", "IQ", "العراق", 33.3152, 44.3661)

    @Test fun `همزة القطع وهمزة الوصل سواء`() {
        assertTrue(alexandria.matches("الاسكندرية"))
        assertTrue(alexandria.matches("الإسكندرية"))
        assertTrue(alexandria.matches("اسكندرية"))
    }

    @Test fun `التاء المربوطة والهاء سواء`() {
        assertTrue(mecca.matches("مكه"))
        assertTrue(mecca.matches("مكة"))
    }

    @Test fun `البحث بالإنجليزية يعمل بلا حساسية لحالة الأحرف`() {
        assertTrue(baghdad.matches("baghdad"))
        assertTrue(baghdad.matches("BAGHDAD"))
        assertTrue(mecca.matches("mecca"))
    }

    @Test fun `البحث باسم البلد يُظهر مدنه`() {
        assertTrue(baghdad.matches("العراق"))
        assertTrue(alexandria.matches("مصر"))
    }

    @Test fun `المسافات لا تمنع المطابقة`() {
        assertTrue(mecca.matches("مكة المكرمة"))
        assertTrue(mecca.matches("مكةالمكرمة"))
    }

    @Test fun `اسم غير موجود لا يطابق`() {
        assertFalse(baghdad.matches("طوكيو"))
        assertFalse(mecca.matches("tokyo"))
    }
}
