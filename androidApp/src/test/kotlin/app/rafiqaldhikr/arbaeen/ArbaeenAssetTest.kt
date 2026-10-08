package app.rafiqaldhikr.arbaeen

import app.rafiq.domain.model.parseAthar
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * الأربعون النوويّة — على الأصل المشحون.
 *
 * وهذا الاختبارُ يحرس **الدعوى**: أنّ هذه هي الأربعون، اثنان وأربعون
 * حديثاً بترتيبها، كلٌّ بدرجته وتخريجه. فلو سقط حديثٌ في جلبٍ، أو تبدّل
 * رقمٌ في خريطة `tools/build_arbaeen.py`، أو تكرّر حديثٌ مرّتين — سقط
 * البناءُ قبل أن تُعرَض الدعوى على أحد.
 */
class ArbaeenAssetTest {

    private val book by lazy {
        val f = listOf(
            File("src/main/assets/arbaeen.json"),
            File("androidApp/src/main/assets/arbaeen.json"),
        ).firstOrNull { it.exists() }
        requireNotNull(parseAthar(requireNotNull(f) { "arbaeen.json غير موجود" }
            .readText(Charsets.UTF_8))) { "تعذّر تحليلُ arbaeen.json" }
    }

    /** **اثنان وأربعون، لا أقلّ ولا أكثر.** */
    @Test
    fun hasFortyTwoHadiths() {
        assertEquals(42, book.items.size)
    }

    /** وكلُّها بدرجةٍ وتخريج — ولا حديثَ بلا دليل. */
    @Test
    fun everyHadithIsAttributedAndGraded() {
        val naked = book.items.filter { it.grade.isBlank() || it.source.isBlank() }
        assertTrue("أحاديثُ بلا إسناد: ${naked.map { it.sourceId }}", naked.isEmpty())
    }

    /** ولا حديثَ مكرَّرٌ — فرقمُ المصدر فريدٌ لكلٍّ. */
    @Test
    fun noHadithIsRepeated() {
        assertEquals(42, book.items.map { it.sourceId }.toSet().size)
        assertEquals(42, book.items.map { it.text }.toSet().size)
    }

    /** يُجرَّد النصُّ من التشكيل قبل المقابلة. */
    private fun bare(s: String) =
        s.replace(Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]"), "")

    /**
     * والترتيبُ محفوظٌ: الأوّلُ حديثُ النيّات.
     *
     * ويُقابَل بـ«الأعمال» و«هجرته» لا بـ«بالنيات»: روايةُ الموسوعة
     * «إنما الأعمال **بالنية**» مفردةً، ونسخةُ دار السلام «بالنيات».
     * **واختلافُ الروايتين ليس خطأً** — وهو بعينه سببُ أنّ الربطَ بين
     * المصدرين جرى بأطول مقطعٍ مشترك لا بتساوي النصّ.
     */
    @Test
    fun firstIsTheHadithOfIntentions() {
        val first = bare(book.items.first().text)
        assertTrue("الأوّل: ${first.take(70)}", first.contains("الأعمال"))
        assertTrue("الأوّل: ${first.take(70)}", first.contains("هجرته"))
    }

    /**
     * **والثاني والأربعون آخرُها** — «يا ابن آدم إنك ما دعوتني ورجوتني».
     *
     * وهذا هو الحدُّ الذي عُرفت به الأربعون من تتمّة الخمسين: ما بعده
     * زياداتٌ ليست من كتاب النوويّ.
     */
    @Test
    fun lastIsTheHadithOfHope() {
        val last = bare(book.items.last().text)
        assertTrue("الأخير: ${last.take(70)}", last.contains("ابن ادم") || last.contains("ابن آدم"))
    }

    /** ونسبةُ الناشر محفوظةٌ — شرطٌ من شروط النشر. */
    @Test
    fun publisherAndEditionAreRecorded() {
        assertEquals("HadeethEnc.com", book.publisher)
        assertTrue("تاريخُ الإصدار فارغ", book.fetchedOn.isNotBlank())
    }

    /** والشرحُ موجودٌ لأكثرها — فالكتابُ يُقرأ لا يُسرَد. */
    @Test
    fun mostHaveCommentary() {
        val with = book.items.count { it.explanation.isNotBlank() }
        assertTrue("بشرح: $with من ٤٢", with >= 38)
    }
}
