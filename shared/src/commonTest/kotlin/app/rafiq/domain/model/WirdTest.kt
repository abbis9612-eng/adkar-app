package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * حسابُ وِرد الختمة.
 *
 * وأهمُّ ما يُختبر هنا هو **ما يُفقد الناسَ ختماتِهم**: الختمةُ الساذجة
 * تقسم ٦٠٤ على ٣٠ فتقول «٢٠ صفحة» كلَّ يوم، فمن فاته ثلاثةُ أيّامٍ صار
 * عليه ثمانون صفحةً في يومٍ واحد فييأس ويترك. والقسمُ على ما بقي يمنع
 * ذلك — وهذه الاختباراتُ تُثبته بالأرقام.
 */
class WirdTest {

    private fun w(readTo: Int, today: Long, days: Int = 30, started: Long = 0L) =
        wirdOf(1, 604, days, started, readTo, today)

    /** اليومُ الأوّل: ٦٠٤ على ٣٠ = ٢١ صفحةً (قسمةً لأعلى). */
    @Test
    fun firstDay() {
        val r = w(readTo = 0, today = 0L)
        assertEquals(1, r.from)
        assertEquals(21, r.pages)
        assertEquals(0, r.behind)
    }

    /** ومن قرأ وِردَه بالأمس يبدأ من بعده لا من أوّل الختمة. */
    @Test
    fun continuesFromWhatWasRead() {
        val r = w(readTo = 21, today = 1L)
        assertEquals(22, r.from)
    }

    /**
     * **القاعدةُ الأهمّ**: من فاتته ثلاثةُ أيّامٍ لا يُطالَب بأربعة أوراد
     * دفعةً — يُعاد القسمُ على ما بقي.
     */
    @Test
    fun missedDaysAreSpreadNotPiled() {
        //  مضت ٤ أيّامٍ ولم يقرأ شيئاً
        val r = w(readTo = 0, today = 4L)
        val naive = 21 * 5 // ما كانت تطالبه به الختمةُ الساذجة
        assertTrue(r.pages < naive, "يُعاد القسمُ لا يُكوَّم: ${r.pages} يجب أن تقلّ عن $naive")
        //  ٦٠٤ على ٢٦ يوماً باقيةً = ٢٤ صفحة — زيادةٌ محتمَلةٌ لا يائسة
        assertEquals(24, r.pages)
    }

    /** ويُقال له كم تأخّر — صراحةً لا ضمناً. */
    @Test
    fun behindIsReported() {
        val r = w(readTo = 0, today = 4L)
        //  كان ينبغي أن يكون قرأ ٨١ صفحةً بعد أربعة أيّام
        assertEquals(81, r.behind)
    }

    /** ومن سبق جدولَه لا يُعاد عليه ما قرأ ولا يُقال له «متأخّر». */
    @Test
    fun aheadIsNotPunished() {
        val r = w(readTo = 200, today = 2L)
        assertEquals(201, r.from)
        assertEquals(0, r.behind)
        assertTrue(r.done, "من سبق فقد أتمّ وِردَ يومه")
    }

    /** والختمُ يُعرف: لا وِردَ بعد آخر صفحة. */
    @Test
    fun finishing() {
        val r = w(readTo = 604, today = 29L)
        assertTrue(r.finished)
        assertEquals(0, r.pages)
    }

    /** ومن تجاوز مدّتَه يُطالَب بالباقي لا بلا شيء — ولا تُقسَم على صفر. */
    @Test
    fun pastTheDeadlineStillAsks() {
        val r = w(readTo = 500, today = 60L)
        assertEquals(501, r.from)
        assertEquals(604, r.to)
        assertEquals(104, r.pages)
    }

    /** وختمةٌ تبدأ غداً لا تُطالِب بشيءٍ اليوم بأثرٍ رجعيّ. */
    @Test
    fun futureStartIsDayZero() {
        val r = wirdOf(
            fromPage = 1, toPage = 604, days = 30,
            startedOn = 10L, readTo = 0, today = 5L,
        )
        assertEquals(21, r.pages)
        assertEquals(0, r.behind)
    }

    /** والزمنُ يُقدَّر: ٢١ صفحةً ≈ ٣٥ دقيقة. */
    @Test
    fun minutesAreEstimated() {
        val r = w(readTo = 0, today = 0L)
        assertEquals(35, r.minutes)
    }

    /** وختمةُ جزءٍ واحدٍ في أسبوع تعمل كما تعمل ختمةُ المصحف. */
    @Test
    fun partialRangeWorks() {
        val r = wirdOf(
            fromPage = 1, toPage = 20, days = 4,
            startedOn = 0L, readTo = 0, today = 0L,
        )
        assertEquals(1, r.from)
        assertEquals(5, r.pages)
    }
}
