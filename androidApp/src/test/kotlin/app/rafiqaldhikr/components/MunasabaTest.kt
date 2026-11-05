package app.rafiqaldhikr.components

import app.rafiqaldhikr.ui.components.nearestOccasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import app.rafiqaldhikr.ui.components.localDay
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * اختيارُ المناسبة القادمة.
 *
 * والقاعدةُ التي تُختبر هنا: **يومُ المناسبة نفسُه حاضرٌ لا ماضٍ**. فمن
 * فتح التطبيق صباحَ عرفة يجب أن يُقال له «اليوم عرفة»، لا أن يُقفز به
 * إلى عرفةَ القادم بعد ثلاثمئةٍ وأربعةٍ وخمسين يوماً — وذاك أسوأُ ما قد
 * يفعله عدّادٌ يراد به التنبيه.
 */
class MunasabaTest {

    private val ASHURA = 1
    private val RAMADAN = 2
    private val ARAFAH = 3

    @Test
    fun picksTheNearestFutureOne() {
        val c = listOf(ASHURA to 100L, RAMADAN to 160L, ARAFAH to 250L)
        assertEquals(ASHURA to 100L, nearestOccasion(c, 50L))
    }

    /** ما مضى يُتخطّى، ولو كان أقربَ رقماً. */
    @Test
    fun skipsThePast() {
        val c = listOf(ASHURA to 100L, RAMADAN to 160L, ARAFAH to 250L)
        assertEquals(RAMADAN to 160L, nearestOccasion(c, 101L))
    }

    /** ويومُ المناسبة نفسُه حاضرٌ — لا يُقفز عنه. */
    @Test
    fun todayCountsAsPresent() {
        val c = listOf(ASHURA to 100L, RAMADAN to 160L)
        assertEquals(ASHURA to 100L, nearestOccasion(c, 100L))
    }

    /** ومناسباتُ السنة القادمة تُلتقط حين تمضي مناسباتُ هذه. */
    @Test
    fun fallsThroughToNextYear() {
        val thisYear = listOf(ASHURA to 100L, RAMADAN to 160L)
        val nextYear = listOf(ASHURA to 454L, RAMADAN to 514L)
        assertEquals(ASHURA to 454L, nearestOccasion(thisYear + nextYear, 400L))
    }

    /** ولو مضى كلُّ شيءٍ فلا يُعرض سطرٌ خاطئ. */
    @Test
    fun nothingLeftIsNull() {
        assertNull(nearestOccasion(listOf(ASHURA to 100L), 500L))
    }

    @Test
    fun emptyIsNull() {
        assertNull(nearestOccasion(emptyList(), 100L))
    }

    /**
     * **رقمُ اليوم يُقاس بالتقويم المحليّ** — وهذا ما كان مكسوراً.
     *
     * `IslamicCalendar` تُرجع منتصفَ ليلٍ محليّاً، وكان «اليوم» يُحسب
     * عالميّاً. فشرقَ غرينتش تبدو كلُّ مناسبةٍ قد مضت بيوم، فتُحذَف عرفةُ
     * صباحَ عرفة **ويُقفَز إلى عرفةِ العام القادم**.
     *
     * فمنتصفُ الليل والظهرُ من يومٍ واحدٍ يجب أن يكونا يوماً واحداً.
     */
    @Test
    fun localMidnightAndNoonAreTheSameDay() {
        val tz = java.util.TimeZone.getTimeZone("Asia/Baghdad")
        val old = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(tz)
            val cal = java.util.Calendar.getInstance(tz).apply {
                clear(); set(2026, 5, 17, 0, 0, 0)
            }
            val midnight = cal.timeInMillis
            val noon = midnight + 12 * 60 * 60 * 1000L
            assertEquals(localDay(midnight), localDay(noon))
            //  وبالقسمة العالميّة يفترقان — وهو العطبُ بعينه
            assertTrue(
                "القسمةُ العالميّةُ كانت تفرّقهما",
                Math.floorDiv(midnight, 86_400_000L) != Math.floorDiv(noon, 86_400_000L),
            )
        } finally {
            java.util.TimeZone.setDefault(old)
        }
    }

    /** واليومُ التالي يومٌ واحدٌ بعده، لا أكثر. */
    @Test
    fun consecutiveLocalDaysDifferByOne() {
        val tz = java.util.TimeZone.getTimeZone("Asia/Baghdad")
        val old = java.util.TimeZone.getDefault()
        try {
            java.util.TimeZone.setDefault(tz)
            val cal = java.util.Calendar.getInstance(tz).apply {
                clear(); set(2026, 5, 17, 0, 0, 0)
            }
            val a = localDay(cal.timeInMillis)
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            assertEquals(a + 1, localDay(cal.timeInMillis))
        } finally {
            java.util.TimeZone.setDefault(old)
        }
    }
}
