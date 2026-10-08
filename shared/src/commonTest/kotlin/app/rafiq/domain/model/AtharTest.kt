package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * تحليلُ كتاب الأثر.
 *
 * وأهمُّ ما يُختبر هنا [itemWithoutIsnadIsDropped]: حديثٌ بلا تخريجٍ أو
 * بلا درجةٍ **لا يدخل** — فالأثرُ بابُ الدليل، وحديثٌ فيه بلا دليلٍ ينقض
 * البابَ كلَّه.
 */
class AtharTest {

    private val one = """
        {"publisher":"HadeethEnc.com","publisher_url":"https://hadeethenc.com",
         "fetched_on":"2026-10-08",
         "items":[
           {"category":"mathura","title":"ت","title_en":"T","text_ar":"متنٌ",
            "text_en":"body","source":"متفق عليه\nالبخاري (1)","source_grade":"صحيح",
            "virtue":"شرحٌ","virtue_en":"c","hadeeth_id":"6274"}]}
    """.trimIndent()

    @Test
    fun parsesPublisherAndItems() {
        val b = requireNotNull(parseAthar(one))
        assertEquals("HadeethEnc.com", b.publisher)
        assertEquals("2026-10-08", b.fetchedOn)
        assertEquals(1, b.items.size)
        val i = b.items[0]
        assertEquals("متنٌ", i.text)
        assertEquals("صحيح", i.grade)
        assertEquals("6274", i.sourceId)
        assertTrue(i.source.contains("البخاري"))
    }

    /** **حديثٌ بلا درجةٍ أو بلا تخريجٍ يُسقَط ولا يُعرَض بلا دليل.** */
    @Test
    fun itemWithoutIsnadIsDropped() {
        val raw = """
            {"publisher":"P","fetched_on":"d","items":[
              {"text_ar":"أ","source":"س","source_grade":"صحيح","hadeeth_id":"1"},
              {"text_ar":"ب","source":"س","hadeeth_id":"2"},
              {"text_ar":"ج","source_grade":"صحيح","hadeeth_id":"3"},
              {"source":"س","source_grade":"صحيح","hadeeth_id":"4"}]}
        """.trimIndent()
        val b = requireNotNull(parseAthar(raw))
        assertEquals(1, b.items.size)
        assertEquals("أ", b.items[0].text)
    }

    /** ملفٌّ غائبٌ أو معطوبٌ: البابُ يُخفى ولا يسقط التطبيق. */
    @Test
    fun brokenOrEmptyGivesNull() {
        assertNull(parseAthar("{ not json"))
        assertNull(parseAthar("""{"publisher":"P","items":[]}"""))
        assertNull(parseAthar("""{"publisher":"P"}"""))
        assertNull(parseAthar("[]"))
    }

    /** الأبوابُ بترتيب ورودها لا بترتيبٍ أبجديّ — فترتيبُ المصدر معنى. */
    @Test
    fun categoriesKeepSourceOrder() {
        val items = listOf("b", "b", "a", "c", "a").map {
            AtharItem(it, "", "", "م", "", "س", "صحيح", "", "", "1")
        }
        assertEquals(listOf("b" to 2, "a" to 2, "c" to 1), atharCategories(items))
    }

    /** والترجمةُ الغائبةُ فراغٌ لا حشو. */
    @Test
    fun missingTranslationStaysEmpty() {
        val b = requireNotNull(parseAthar(
            """{"publisher":"P","fetched_on":"d","items":[
                 {"text_ar":"م","source":"س","source_grade":"صحيح","hadeeth_id":"9"}]}""",
        ))
        assertEquals("", b.items[0].textEn)
        assertEquals("", b.items[0].explanation)
    }
}
