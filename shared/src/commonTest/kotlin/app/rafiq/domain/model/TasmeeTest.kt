package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * التسميع — المقابلةُ والحكم.
 *
 * وأهمُّ ما يُختبر هنا ليس أنّ الصحيحَ يُقال له صحيح. أهمُّه **أنّ الآلةَ
 * لا تتّهم قارئاً بخطأٍ في كتاب الله وهي لم تسمعه**: [massMismatchIsUnsure]
 * و[lowConfidenceIsUnsure] و[tooShortIsUnsure] هي الاختباراتُ التي تحمي
 * هذا، و[unsureNeverAccuses] تمنع أن يُشار إلى كلمةٍ بسوءٍ مع «لم أتبيّن».
 *
 * ونصوصُ الآيات هنا **منقولةٌ من `quran_uthmani.json` في المستودع**، لا
 * مكتوبةٌ من ذاكرة.
 */
class TasmeeTest {

    //  الفاتحة ٢ — أربعُ كلمات
    private val fatiha2 = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ"

    //  البقرة ٢ — تسعةُ رموزٍ بالمسافة، منها علامتا وقف، فسبعُ كلمات
    private val baqara2 = "ذَٰلِكَ ٱلْكِتَٰبُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًۭى لِّلْمُتَّقِينَ"

    //  البقرة ٢١ — إحدى عشرةَ كلمة، وأوّلُها نداءٌ موصولٌ في الرسم
    private val baqara21 =
        "يَٰٓأَيُّهَا ٱلنَّاسُ ٱعْبُدُوا۟ رَبَّكُمُ ٱلَّذِى خَلَقَكُمْ وَٱلَّذِينَ مِن قَبْلِكُمْ لَعَلَّكُمْ تَتَّقُونَ"

    private val baqara21Heard =
        "يا أيها الناس اعبدوا ربكم الذي خلقكم والذين من قبلكم لعلكم تتقون"

    /** علاماتُ الوقف ليست كلماتٍ تُحاسب. */
    @Test
    fun pauseMarksAreNotWords() {
        assertEquals(9, baqara2.split(' ').size)
        assertEquals(7, tasmeeTokens(baqara2).size)
    }

    /** الرسمُ العثمانيُّ والإملائيُّ يلتقيان بعد التطبيع. */
    @Test
    fun uthmaniMeetsImlaei() {
        assertEquals(
            tasmeeTokens(fatiha2),
            tasmeeTokens("الحمد لله رب العالمين"),
        )
    }

    /** تلاوةٌ تامّة: حكمٌ صريحٌ ولا كلمةَ مشكوكة. */
    @Test
    fun perfectRecitationIsClear() {
        val r = tasmee(fatiha2, "الحمد لله رب العالمين", 0.9f)
        assertEquals(Verdict.CLEAR, r.verdict)
        assertEquals(4, r.heard)
        assertEquals(4, r.total)
        assertTrue(r.marks.all { it == WordMark.HEARD })
    }

    /**
     * النداءُ الموصول: «يَٰٓأَيُّهَا» كلمةٌ في الرسم وكلمتان في السمع.
     *
     * ولولا الوصلُ في المحاذاة لعُدَّت كلُّ آيةٍ فيها نداءٌ خطأً — وفي
     * القرآن منها كثير.
     */
    @Test
    fun mergedCallIsNotAnError() {
        val r = tasmee(baqara21, baqara21Heard, 0.9f)
        assertEquals(Verdict.CLEAR, r.verdict)
        assertEquals(11, r.total)
    }

    /** كلمةٌ واحدةٌ غيرُ ما قيل: يُشار إلى موضعها بعينه. */
    @Test
    fun oneWrongWordIsPointedAt()  {
        val r = tasmee(baqara21, baqara21Heard.replace("خلقكم", "رزقكم"), 0.9f)
        assertEquals(Verdict.FLAWED, r.verdict)
        assertEquals(WordMark.SUSPECT, r.marks[5])
        assertEquals(1, r.marks.count { it != WordMark.HEARD })
    }

    /** كلمةٌ لم تُسمع: تُعلَم صامتةً لا مشكوكة — والفرقُ يُعرَض للقارئ. */
    @Test
    fun missedWordIsSilent() {
        val r = tasmee(baqara21, baqara21Heard.replace(" الناس", ""), 0.9f)
        assertEquals(Verdict.FLAWED, r.verdict)
        assertEquals(WordMark.SILENT, r.marks[1])
        assertEquals(1, r.marks.count { it != WordMark.HEARD })
    }

    /** زيادةٌ في السمع لا تُنقص من الآية شيئاً. */
    @Test
    fun extraHeardWordIsNotAnError() {
        val r = tasmee(fatiha2, "الحمد لله رب العالمين امين", 0.9f)
        assertEquals(Verdict.CLEAR, r.verdict)
        assertTrue(r.marks.all { it == WordMark.HEARD })
    }

    /**
     * **ثقةٌ دون الحدّ: لا حكمَ أصلاً.**
     *
     * والنصُّ هنا مطابقٌ تماماً — ومع ذلك لا يُقال «صحّ»: ما بُني على سمعٍ
     * لا يُثق به لا يُبنى عليه شيء، في الاتّهام ولا في التزكية.
     */
    @Test
    fun lowConfidenceIsUnsure() {
        val r = tasmee(fatiha2, "الحمد لله رب العالمين", 0.4f)
        assertEquals(Verdict.UNSURE, r.verdict)
    }

    /** تلاوةٌ أقصرُ من نصف الآية: انقطاعٌ لا خطأ. */
    @Test
    fun tooShortIsUnsure() {
        val r = tasmee(baqara21, "يا أيها الناس", 0.9f)
        assertEquals(Verdict.UNSURE, r.verdict)
    }

    /**
     * **وهذا هو الاختبارُ الذي من أجله كُتب الملفُّ كلُّه.**
     *
     * سمعٌ لا يشبه الآيةَ في شيء: لو حُكم بالظاهر لقيل للقارئ «أخطأتَ في
     * إحدى عشرةَ كلمة» — وهو لم يُخطئ، بل النموذجُ لم يسمع. فيُرَدّ الخللُ
     * إلى السمع.
     */
    @Test
    fun massMismatchIsUnsure() {
        val r = tasmee(
            baqara21,
            "واحد اثنان ثلاثه اربعه خمسه سته سبعه ثمانيه تسعه عشره احدعشر",
            0.9f,
        )
        assertEquals(Verdict.UNSURE, r.verdict)
    }

    /** ومع «لم أتبيّن» لا تُشار كلمةٌ بسوءٍ أبداً. */
    @Test
    fun unsureNeverAccuses() {
        for (r in listOf(
            tasmee(fatiha2, "الحمد لله رب العالمين", 0.1f),
            tasmee(baqara21, "يا أيها", 0.9f),
            tasmee(baqara21, "واحد اثنان ثلاثه اربعه خمسه سته سبعه ثمانيه تسعه عشره احدعشر", 0.9f),
        )) {
            assertEquals(Verdict.UNSURE, r.verdict)
            assertTrue(r.marks.none { it != WordMark.HEARD })
        }
    }

    /** طولُ العلامات طولُ الآية دائماً — فالواجهةُ لا تحتاج حساباً. */
    @Test
    fun marksAlwaysMatchAyahLength() {
        for (ayah in listOf(fatiha2, baqara2, baqara21)) {
            val n = tasmeeTokens(ayah).size
            for (conf in listOf(0.1f, 0.9f)) {
                assertEquals(n, tasmee(ayah, "الحمد لله", conf).marks.size)
            }
        }
    }

    /** آيةٌ بلا كلمات: لا حكمَ ولا اتّهام. */
    @Test
    fun emptyAyahIsUnsure() {
        val r = tasmee("", "الحمد لله", 0.9f)
        assertEquals(Verdict.UNSURE, r.verdict)
        assertEquals(0, r.total)
    }
}
