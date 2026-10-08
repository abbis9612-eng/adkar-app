package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * مرآةُ السنة.
 *
 * وأهمُّ ما يُختبر هنا أنّ **العمق عددُ الأبواب لا مجموعُ الأرقام**: لو
 * جُمعت لطغى التسبيح، فبدا يومُ من سبّح ألفاً وترك ما سواه أعمقَ من يومٍ
 * صلّى فيه صاحبُه وقرأ وذكر. وذلك كذبٌ على صاحبه في مرآته.
 */
class YearMirrorTest {

    private fun day(
        date: String, m: Boolean = false, e: Boolean = false,
        q: Long = 0, t: Long = 0, p: Long = 0,
    ) = DailyProgressInfo(date, m, e, q, t, p, 0L)

    @Test
    fun depthCountsDoorsNotNumbers() {
        //  ألفُ تسبيحةٍ وحدَها = بابٌ واحد
        assertEquals(1, depthOf(day("2026-01-01", t = 1000)))
        //  وثلاثةُ أبوابٍ بأرقامٍ صغيرة = ثلاثة
        assertEquals(3, depthOf(day("2026-01-02", m = true, q = 1, t = 1)))
    }

    @Test
    fun emptyDayIsZeroAndFiveIsMax() {
        assertEquals(0, depthOf(day("2026-01-01")))
        assertEquals(
            5,
            depthOf(day("2026-01-01", m = true, e = true, q = 1, t = 1, p = 1)),
        )
    }

    /** واليومُ الذي لا صفَّ له يبقى في الشبكة بعمقِ صفر — لا يُحذف. */
    @Test
    fun missingDaysKeepTheirPlace() {
        val dates = listOf("2026-01-01", "2026-01-02", "2026-01-03")
        val m = mirrorOf(listOf(day("2026-01-03", m = true)), dates)
        assertEquals(3, m.size)
        assertEquals(listOf(0, 0, 1), m.map { it.depth })
        assertEquals("2026-01-03", m[2].date)
    }

    @Test
    fun longestRunFindsTheLongest() {
        val m = listOf(1, 1, 0, 1, 1, 1, 0, 1).mapIndexed { i, d ->
            MirrorDay("d$i", d)
        }
        assertEquals(3, longestRun(m))
    }

    /** وسلسلةٌ تملأ العامَ كلَّه تُحسب كاملةً. */
    @Test
    fun longestRunCanBeTheWholeYear() {
        val m = (1..365).map { MirrorDay("d$it", 2) }
        assertEquals(365, longestRun(m))
    }

    @Test
    fun longestRunOfNothingIsZero() {
        assertEquals(0, longestRun((1..10).map { MirrorDay("d$it", 0) }))
    }

    /** و«أكثرُ ما واظبتَ عليه» يُقاس بعدد الأيّام لا بالمقدار. */
    @Test
    fun mostKeptIsByDaysNotAmount() {
        val days = listOf(
            day("2026-01-01", t = 9999),
            day("2026-01-02", m = true),
            day("2026-01-03", m = true),
            day("2026-01-04", m = true),
        )
        val s = summarize(days, mirrorOf(days, days.map { it.date }))
        assertEquals(Door.MORNING, s.mostKept)
    }

    /** ولا يُقال «أكثرُ ما واظبتَ عليه» لمن لم يواظب على شيء. */
    @Test
    fun mostKeptIsNullWhenNothingDone() {
        val days = listOf(day("2026-01-01"), day("2026-01-02"))
        val s = summarize(days, mirrorOf(days, days.map { it.date }))
        assertNull(s.mostKept)
        assertEquals(0, s.activeDays)
    }

    @Test
    fun summaryTotals() {
        val days = listOf(
            day("2026-01-01", q = 10, t = 100, p = 5),
            day("2026-01-02", q = 5, t = 33, p = 3),
        )
        val s = summarize(days, mirrorOf(days, days.map { it.date }))
        assertEquals(15L, s.quranPages)
        assertEquals(133L, s.tasbeeh)
        assertEquals(8L, s.prayers)
        assertEquals(2, s.activeDays)
    }
}
