package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * تطبيعُ البحث العربي.
 *
 * وُجد هذا الاختبار لأن بحثَ القرآن كان يعطي **صفر نتائج دائماً**: عمودُ
 * البحث مشكولٌ بالرسم العثماني، والاستعلامُ يُمرَّر خاماً. ولم يمسك ذلك
 * مترجمٌ ولا حارس، لأنّ الشيفرةَ كانت سليمةً تماماً — والبياناتُ لا.
 *
 * وأخطرُ ما هنا أنّ التطبيعَ مكتوبٌ مرّتين: هنا في Kotlin، وفي
 * `tools/build_quran_assets.py` الذي يولّد العمود. فلو افترقا لعاد البحثُ
 * إلى الصفر بلا أن يفشل شيء. الحالاتُ أدناه هي عقدُهما.
 *
 * أسماءُ الدوال ASCII لا عربيةً بين backticks: العربيةُ تُنتج ملفات
 * `.class` بأسماء عربية، وهي تفشل على محلّيةٍ غير UTF-8 (ويندوز شائع).
 */
class ArabicSearchTest {

    private fun n(s: String) = ArabicSearch.normalize(s)

    /** التشكيلُ يسقط: «الرَّحْمَٰنِ» و«الرحمن» سواء. */
    @Test
    fun diacriticsAreStripped() {
        assertEquals(n("الرحمن"), n("ٱلرَّحْمَٰنِ"))
        assertEquals(n("الله"), n("ٱللَّهِ"))
    }

    /**
     * الألفُ تسقط من الطرفين.
     *
     * وهي عقدةُ المسألة: العثمانيّ يكتب «ٱلْعَٰلَمِينَ» بألفٍ خنجريّة
     * والناسُ يكتبون «العالمين» بألفٍ تامّة. فلو أُبقيت الألفُ لما التقيا،
     * ولو حُوّلت الخنجريّةُ ألفاً لانكسر «الرحمن» (يكتبها الناس بلا ألف).
     */
    @Test
    fun alifFoldsBothWays() {
        assertEquals(n("العالمين"), n("ٱلْعَٰلَمِينَ"))
        assertEquals(n("الرحمن"), n("ٱلرَّحْمَٰنِ"))
    }

    /** الهمزةُ المفردة تُحذف: «ءَامَنُوا۟» يكتبها الناس «آمنوا». */
    @Test
    fun standaloneHamzaIsDropped() {
        assertEquals(n("آمنوا"), n("ءَامَنُوٓا۟"))
    }

    /**
     * المسافةُ تسقط.
     *
     * العثمانيّ يصل «يَٰٓأَيُّهَا» والناسُ يكتبون «يا أيها» — ولولا هذا
     * لما وُجد أشهرُ نداءٍ في القرآن.
     */
    @Test
    fun spacesAreIgnored() {
        assertTrue(n("يَٰٓأَيُّهَا ٱلَّذِينَ ءَامَنُوا۟").contains(n("يا أيها الذين آمنوا")))
    }

    /** رسمٌ عثمانيٌّ يخالف الإملائيَّ بالواو: «ٱلصَّلَوٰةَ» ← «الصلاة». */
    @Test
    fun uthmaniWawSpellingsAreReconciled() {
        assertEquals(n("الصلاة"), n("ٱلصَّلَوٰةَ"))
        assertEquals(n("الزكاة"), n("ٱلزَّكَوٰةَ"))
    }

    /** الحروفُ الموحَّدة: ى←ي و ة←ه و أإآ←ا. */
    @Test
    fun lettersAreFolded() {
        assertEquals(n("علي"), n("عَلَىٰ"))
        assertEquals(n("رحمه"), n("رَحْمَةً"))
    }

    /**
     * الحدُّ الأدنى على النصّ **المطبَّع** لا على ما كُتب.
     *
     * التطبيعُ يُقصّر الكلمة («الماء» تصير حرفين)، واستعلامٌ بحرفين
     * يُطابق آلافَ الآيات فيعطي ضجيجاً لا نتائج.
     */
    @Test
    fun shortQueriesAreRejectedAfterNormalization() {
        assertTrue(ArabicSearch.isSearchable("الرحمن"))
        assertTrue(!ArabicSearch.isSearchable("ال"))
        assertTrue(!ArabicSearch.isSearchable(""))
    }

    /** لا يرمي على نصٍّ فارغٍ أو لاتينيّ أو رموز. */
    @Test
    fun normalizeIsTotal() {
        assertEquals("", n(""))
        assertEquals("abc", n("abc"))
        assertEquals("%_", n("%_"))
    }
}
