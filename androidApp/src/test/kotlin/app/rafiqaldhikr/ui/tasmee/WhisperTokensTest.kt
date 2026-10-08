package app.rafiqaldhikr.ui.tasmee

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * فكُّ رموز النموذج إلى عربيّة.
 *
 * والأمثلةُ أدناه **مأخوذةٌ من `tokens.txt` المشحون نفسِه** بتشغيلِ فكٍّ
 * مرجعيٍّ في بايثون — لا مكتوبةٌ من تقدير. وأهمُّها
 * [arabicLetterSplitAcrossTokens]: الحرفُ العربيُّ بايتان، فيأتي موزَّعاً
 * على رمزين — ومن جمع نصوصَ الرموز كما هي خرج بنصٍّ مشوَّهٍ لا يُقرأ، ولا
 * يكشف ذلك إلّا مثالٌ كهذا.
 */
class WhisperTokensTest {

    private val tokens: WhisperTokens by lazy {
        val f = listOf(
            File("src/main/assets/tasmee/tokens.txt"),
            File("androidApp/src/main/assets/tasmee/tokens.txt"),
        ).firstOrNull { it.exists() }
        WhisperTokens.parse(requireNotNull(f) { "tokens.txt غير موجود" }
            .readText(Charsets.UTF_8))
    }

    /** المعجمُ المشحونُ بحجمه المنتظر: ٥٠٢٥٧ رمزاً وما بعدها خاصّ. */
    @Test
    fun shippedVocabularyHasExpectedSize() {
        assertEquals(50364, tokens.size)
    }

    /** رمزٌ واحدٌ يحمل كلمةً كاملةً بمسافتها. */
    @Test
    fun singleTokenCarriesWholeWord() {
        assertEquals(" الله", tokens.decode(listOf(21984)))
    }

    /**
     * **الحرفُ العربيُّ موزَّعٌ على رمزين — وهذا ما يُثبته.**
     *
     * ستّةُ رموزٍ تُخرج أربعةَ محارف. وهي مخرَجُ النموذج على ضجيجٍ أبيضَ
     * في `tools/tasmee_reference.py`، فالقيمةُ منقولةٌ من تشغيلٍ حقيقيّ.
     */
    @Test
    fun arabicLetterSplitAcrossTokens() {
        assertEquals("رَيْسَ", tokens.decode(listOf(2288, 6808, 1829, 17195, 3794, 6808)))
    }

    /** الرمزُ الخاصُّ يُسقَط ولا يُعرض في نصّ القارئ. */
    @Test
    fun specialTokensAreDropped() {
        //  50258 = <|startoftranscript|> · 50272 = <|ar|> · 50363 = <|notimestamps|>
        assertEquals("رَ", tokens.decode(listOf(50258, 50272, 50363, 2288, 6808)))
    }

    /** رقمٌ خارج المعجم يُسقَط ولا يُسقِط التسميعَ كلَّه. */
    @Test
    fun unknownIdIsSkippedNotCrashing() {
        assertEquals(" الله", tokens.decode(listOf(999_999, 21984, -3)))
    }

    /** لا شيءَ يُفَكّ: نصٌّ فارغٌ لا استثناء. */
    @Test
    fun emptyInputGivesEmptyText() {
        assertEquals("", tokens.decode(emptyList()))
    }

    /** ومخرَجُ الفكّ يصلح مدخلاً للمقابلة — فلا محارفَ بدلٍ فيه. */
    @Test
    fun decodedTextCarriesNoReplacementChar() {
        val text = tokens.decode(listOf(2288, 6808, 1829, 17195, 3794, 6808))
        assertTrue("فيه محرفُ بدل: $text", !text.contains('�'))
    }
}
