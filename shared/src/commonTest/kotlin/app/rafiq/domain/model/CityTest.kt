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

    @Test /** همزة القطع وهمزة الوصل سواء */
    fun hamza_forms_are_equivalent() {
        assertTrue(alexandria.matches("الاسكندرية"))
        assertTrue(alexandria.matches("الإسكندرية"))
        assertTrue(alexandria.matches("اسكندرية"))
    }

    @Test /** التاء المربوطة والهاء سواء */
    fun taa_marbuta_matches_haa() {
        assertTrue(mecca.matches("مكه"))
        assertTrue(mecca.matches("مكة"))
    }

    @Test /** البحث بالإنجليزية بلا حساسية لحالة الأحرف */
    fun english_search_is_case_insensitive() {
        assertTrue(baghdad.matches("baghdad"))
        assertTrue(baghdad.matches("BAGHDAD"))
        assertTrue(mecca.matches("Mecca"))
    }

    @Test /** البحث باسم البلد يُظهر مدنه */
    fun country_name_matches_its_cities() {
        assertTrue(baghdad.matches("العراق"))
        assertTrue(alexandria.matches("مصر"))
    }

    @Test /** المسافات لا تمنع المطابقة */
    fun whitespace_does_not_block_match() {
        assertTrue(mecca.matches("مكة المكرمة"))
        assertTrue(mecca.matches("مكةالمكرمة"))
    }

    @Test /** اسم غير موجود لا يطابق */
    fun unknown_name_does_not_match() {
        assertFalse(baghdad.matches("طوكيو"))
        assertFalse(mecca.matches("tokyo"))
    }

    @Test /** بحث فارغ لا يطابق كل شيء بالخطأ */
    fun blank_query_matches_nothing() {
        assertFalse(baghdad.matches(""))
        assertFalse(baghdad.matches("   "))
    }
}
