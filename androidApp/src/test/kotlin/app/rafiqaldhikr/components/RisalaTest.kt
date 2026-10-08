package app.rafiqaldhikr.components

import app.rafiq.domain.model.DuaItem
import app.rafiqaldhikr.ui.components.pickRisala
import app.rafiqaldhikr.ui.theme.MeeqatPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * رسالةُ اليوم — **ثابتةٌ في طورها، متبدّلةٌ بتبدّله.**
 *
 * والفرقُ بين الأمرين هو كلُّ الفكرة: لو تبدّلت بكلّ فتحةٍ لصارت آلةَ
 * تسليةٍ لا رسالة، ولو ثبتت طولَ اليوم لضاع أنّ التطبيق يعرف أين
 * صاحبُه من يومه.
 */
class RisalaTest {

    private fun duas(n: Int) = (1..n).map {
        DuaItem(
            id = it.toLong(), category = "c", occasion = "o",
            textAr = "نصّ $it", source = "م$it", sourceGrade = "صحيح",
            isFavorite = false, sortOrder = it,
        )
    }

    /** الفتحةُ الثانيةُ في الطور نفسِه تُعطي الدعاءَ نفسَه. */
    @Test
    fun stableWithinPhase() {
        val d = duas(27)
        val a = pickRisala(d, 20_000L, MeeqatPhase.DUHA)
        val b = pickRisala(d, 20_000L, MeeqatPhase.DUHA)
        assertEquals(a, b)
    }

    /** وتبدّلُ الطور يُبدّلها — دعاءُ الفجر غيرُ دعاء العشاء. */
    @Test
    fun changesWithPhase() {
        val d = duas(27)
        val seen = MeeqatPhase.entries.map { pickRisala(d, 20_000L, it) }
        assertEquals("الأطوارُ السبعةُ تعطي سبعةَ أدعيةٍ مختلفة", 7, seen.toSet().size)
    }

    /** ويومٌ جديدٌ يُبدّلها كذلك. */
    @Test
    fun changesWithDay() {
        val d = duas(27)
        val today = pickRisala(d, 20_000L, MeeqatPhase.FAJR)
        val tomorrow = pickRisala(d, 20_001L, MeeqatPhase.FAJR)
        assertTrue(today != tomorrow)
    }

    /**
     * ولا تدور على نفسها بسرعة: سبعةُ أطوارٍ × ثلاثين يوماً = ٢١٠
     * اختياراً، تغطّي الأدعيةَ السبعةَ والعشرين كلَّها.
     */
    @Test
    fun coversAllDuasOverAMonth() {
        val d = duas(27)
        val seen = buildSet {
            for (day in 20_000L until 20_030L) {
                MeeqatPhase.entries.forEach { add(pickRisala(d, day, it)) }
            }
        }
        assertEquals("لا دعاءَ يُهمَل في شهر", 27, seen.size)
    }

    /** واليومُ السالبُ (قبل المبدأ) لا يُسقط الفهرسَ خارج القائمة. */
    @Test
    fun negativeDayIsSafe() {
        val d = duas(27)
        val r = pickRisala(d, -5_000L, MeeqatPhase.LAYL)
        assertTrue(r in d)
    }

    /** وقائمةٌ فارغةٌ تُرجع null ولا ترمي. */
    @Test
    fun emptyIsNull() {
        assertNull(pickRisala(emptyList(), 20_000L, MeeqatPhase.FAJR))
    }
}
