package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * جَرْدُ الإسناد.
 *
 * وأهمُّ ما يُختبر هنا [percentNeverRoundsUp]: الشاشةُ تدّعي الكمال، فلو
 * قرّبت ٩٩٫٦ إلى ١٠٠ لادّعته وهي ناقصة — **ونصٌّ واحدٌ بلا إسنادٍ يُسقط
 * الدعوى كلَّها**.
 */
class IsnadTest {

    private fun e(grade: String, group: String = "morning", source: String = "البخاري") =
        IsnadEntry(text = "نصّ", source = source, grade = grade, group = group)

    @Test
    fun countsTotalAndSourced() {
        val s = summarizeIsnad(listOf(e("صحيح"), e("حسن"), e("قرآن")))
        assertEquals(3, s.total)
        assertEquals(3, s.sourced)
        assertEquals(100, isnadPercent(s))
    }

    /** نصٌّ بلا مصدرٍ أو بلا درجةٍ لا يُحتسب مُسنَداً. */
    @Test
    fun blankSourceOrGradeIsNotSourced() {
        val s = summarizeIsnad(listOf(e("صحيح"), e("", source = "البخاري"), e("حسن", source = " ")))
        assertEquals(3, s.total)
        assertEquals(1, s.sourced)
    }

    /**
     * **النسبةُ لا تُقرَّب لأعلى.**
     *
     * مئتان وخمسون نصّاً، واحدٌ منها بلا درجة: ٩٩٫٦٪. وتُعرَض ٩٩.
     */
    @Test
    fun percentNeverRoundsUp() {
        val entries = List(249) { e("صحيح") } + e("")
        val s = summarizeIsnad(entries)
        assertEquals(250, s.total)
        assertEquals(249, s.sourced)
        assertEquals(99, isnadPercent(s))
    }

    /** الدرجاتُ مرتّبةٌ من الأكثر — ولا ترتيبَ مكتوبٌ في الكود. */
    @Test
    fun gradesSortedByCount() {
        val s = summarizeIsnad(
            List(5) { e("صحيح") } + List(9) { e("حسن") } + List(2) { e("قرآن") },
        )
        assertEquals(listOf("حسن" to 9, "صحيح" to 5, "قرآن" to 2), s.byGrade)
    }

    /** وتساوي العددِ يُرتَّب بالاسم فلا يتبدّل الترتيبُ بين تشغيلين. */
    @Test
    fun tiesAreOrderedByName() {
        val s = summarizeIsnad(listOf(e("حسن"), e("صحيح")))
        assertEquals(listOf("حسن" to 1, "صحيح" to 1), s.byGrade)
    }

    /** الأبوابُ تُحصى كما تُحصى الدرجات. */
    @Test
    fun groupsAreCounted() {
        val s = summarizeIsnad(
            listOf(e("صحيح", "morning"), e("صحيح", "morning"), e("حسن", "duas")),
        )
        assertEquals(listOf("morning" to 2, "duas" to 1), s.byGroup)
    }

    /** جردٌ فارغ: صفرٌ لا قسمةٌ على صفر. */
    @Test
    fun emptyInventoryIsZeroNotCrash() {
        val s = summarizeIsnad(emptyList())
        assertEquals(0, s.total)
        assertEquals(0, isnadPercent(s))
        assertTrue(s.byGrade.isEmpty())
    }

    /** وقائمةُ التجاوز توأمُ الحارس — فلا تُفحَص آياتُ القرآن نصّاً نصّاً. */
    @Test
    fun skipListMatchesGuard() {
        assertEquals(
            setOf("quran_uthmani.json", "tafsir_muyassar.json", "surah_metadata.json"),
            ISNAD_SKIP,
        )
    }
}

/**
 * تحليلُ ملفّات المحتوى.
 *
 * وشرطُ القبول هنا **شرطُ الحارس حرفاً بحرف**: كائنٌ فيه `text_ar`. فلو
 * اختلفا دخل الحارسُ نصّاً لا تراه الشاشةُ، فتقول «مئةٌ بالمئة» وهي لا
 * ترى كلَّ شيء.
 */
class IsnadParseTest {

    @Test
    fun parsesFlatArrayShape() {
        val rows = parseIsnadFile(
            "adhkar_morning.json",
            """[{"text_ar":"سبحان الله","source":"مسلم","source_grade":"صحيح"}]""",
        )
        assertEquals(1, rows.size)
        assertEquals("سبحان الله", rows[0].text)
        assertEquals("مسلم", rows[0].source)
        assertEquals("صحيح", rows[0].grade)
        assertEquals("adhkar_morning", rows[0].group)
    }

    /** وصورةُ الكائنِ الذي قيمُه قوائم — وهي في الأصول كذلك. */
    @Test
    fun parsesNestedObjectShape() {
        val rows = parseIsnadFile(
            "duas.json",
            """{"items":[{"text_ar":"ربنا آتنا","source":"البقرة ٢٠١","source_grade":"قرآن"}],
                "meta":{"v":1}}""",
        )
        assertEquals(1, rows.size)
        assertEquals("قرآن", rows[0].grade)
    }

    /** ملفٌّ بلا `text_ar` ليس ملفَّ نصوصٍ — كـ`cities.json`. */
    @Test
    fun fileWithoutTextIsNotContent() {
        assertTrue(parseIsnadFile("cities.json", """[{"name":"بغداد","lat":33.3}]""").isEmpty())
    }

    /** والقرآنُ والتفسيرُ يُتجاوزان: لهما مسارُهما لا نصٌّ مفردٌ بمصدر. */
    @Test
    fun skippedFilesAreNotCounted() {
        val body = """[{"text_ar":"آية","source":"س","source_grade":"قرآن"}]"""
        for (f in ISNAD_SKIP) assertTrue(parseIsnadFile(f, body).isEmpty(), f)
    }

    /** ملفٌّ معطوبٌ لا يُسقط الجردَ كلَّه. */
    @Test
    fun brokenJsonIsSkippedNotThrown() {
        assertTrue(parseIsnadFile("x.json", "{ not json").isEmpty())
    }

    /** وما ليس json يُترك. */
    @Test
    fun nonJsonNameIsIgnored() {
        assertTrue(parseIsnadFile("tajweed.txt", "[]").isEmpty())
    }

    /** المصدرُ الفارغُ يُقرأ فارغاً — فتُحصى النسبةُ على حقيقتها. */
    @Test
    fun blankSourceStaysBlank() {
        val rows = parseIsnadFile(
            "x.json",
            """[{"text_ar":"نصّ","source":"   ","source_grade":""}]""",
        )
        assertEquals(1, rows.size)
        assertEquals("", rows[0].source)
        assertEquals(0, summarizeIsnad(rows).sourced)
    }
}
