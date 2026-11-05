package app.rafiq.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * حسابُ اكتمال اليوم — كان ثلاثَ صيغٍ في ثلاث شاشات.
 *
 * وهذه الاختباراتُ تثبّت الحالاتِ التي كانت كلُّ صيغةٍ تُخطئ فيها بعينها،
 * حتى لا يعود الاختلافُ من باب السهو.
 */
class DayFillTest {

    private fun day(
        morning: Boolean = false,
        evening: Boolean = false,
        quran:   Long = 0,
        tasbeeh: Long = 0,
        prayers: Long = 0,
    ) = DailyProgressInfo("2026-09-02", morning, evening, quran, tasbeeh, prayers, 0)

    @Test
    fun `يومٌ بلا سجلّ صفر`() {
        assertEquals(0f, dayFill(null))
        assertEquals(0f, dayFill(day()))
    }

    @Test
    fun `اليومُ التامّ واحدٌ صحيح`() {
        assertEquals(1f, dayFill(day(true, true, 5, 100, 5)))
    }

    /**
     * الخطأُ الذي كان في `GardenScreen`: `coerceAtMost(4)` بعد جمع **خمسة**
     * أعمال — فأربعةٌ من خمسةٍ تبلغ السقفَ، ومن لم يصلِّ فرضاً واحداً
     * تُهنّئه الحديقةُ بزهرةٍ تامّة.
     */
    @Test
    fun `أربعةُ أعمالٍ بلا صلاةٍ ليست يوماً تامّاً`() {
        val fill = dayFill(day(morning = true, evening = true, quran = 3, tasbeeh = 33))
        assertTrue(fill < 1f, "بلا صلاةٍ لا يكتمل اليوم، وكان يكتمل")
        assertNear(0.8f, fill)
    }

    /**
     * الخطأُ الذي كان في `ProfileScreen`: صباحٌ + مساءٌ + عددُ الصلوات،
     * و«مكتمل» عند ٥ — فمن صلّى الخمسَ ولم يذكر شيئاً يُقال له يومُك تامّ.
     */
    @Test
    fun `الصلواتُ الخمسُ وحدَها ليست يوماً تامّاً`() {
        assertNear(0.2f, dayFill(day(prayers = 5)))
    }

    /** الكسورُ العشريةُ في Float لا تُطابَق حرفياً: ‎0.2f/5f = 0.040000003‎. */
    private fun assertNear(expected: Float, actual: Float) =
        assertTrue(kotlin.math.abs(expected - actual) < 1e-5f, "توقّعت $expected فجاء $actual")

    @Test
    fun `الصلواتُ تتدرّج ولا تقفز`() {
        assertNear(0.04f, dayFill(day(prayers = 1)))
        assertNear(0.12f, dayFill(day(prayers = 3)))
        // ما فوق الخمسِ لا يتجاوز الخمس
        assertEquals(dayFill(day(prayers = 5)), dayFill(day(prayers = 9)))
    }

    /**
     * الخطأُ الذي كان في `WeeklyReportScreen`: العدُّ للأذكار والصلوات
     * فقط — فيومٌ قرأتَ فيه عشرين صفحةً «غيرُ نشط»، والبطاقةُ التي تعلوه
     * تعرض تلك الصفحاتِ بعينها.
     */
    @Test
    fun `القرآنُ وحدَه يجعل اليومَ نشِطاً`() {
        assertTrue(isActiveDay(day(quran = 20)))
        assertTrue(isActiveDay(day(tasbeeh = 33)))
        assertFalse(isActiveDay(day()))
        assertFalse(isActiveDay(null))
    }
}
