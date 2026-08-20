package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * بحث المدن يجب أن يسامح ما يكتبه الناس فعلاً: بلا تشكيل، وبهمزة عادية
 * بدل همزة القطع، و«ه» بدل «ة»، وبأل التعريف أو بدونها.
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

    @Test fun `البحث بالإنجليزية بلا حساسية لحالة الأحرف`() {
        assertTrue(baghdad.matches("baghdad"))
        assertTrue(baghdad.matches("BAGHDAD"))
        assertTrue(mecca.matches("Mecca"))
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

    @Test fun `بحث فارغ لا يطابق كل شيء بالخطأ`() {
        assertFalse(baghdad.matches(""))
        assertFalse(baghdad.matches("   "))
    }
}
