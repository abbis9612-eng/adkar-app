package app.rafiqaldhikr.adhkar

import app.rafiqaldhikr.ui.screens.adhkar.AdhkarCategoriesViewModel.Companion.nowKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * بابُ الساعة — ترشيحٌ لا قفل، لكنّه يجب أن يغطّي اليومَ كلَّه بلا ثغرة.
 */
class AdhkarNowKeyTest {

    @Test
    fun `كل ساعة من اليوم لها باب`() {
        val keys = (0..23).map { nowKey(it) }
        assertEquals(24, keys.size)
        assertTrue("بابٌ غيرُ معروف", keys.all { it in setOf("morning", "prayer", "evening", "sleep") })
    }

    @Test
    fun `الحدود عند أطرافها`() {
        assertEquals("sleep", nowKey(3))
        assertEquals("morning", nowKey(4))
        assertEquals("morning", nowKey(10))
        assertEquals("prayer", nowKey(11))
        assertEquals("prayer", nowKey(14))
        assertEquals("evening", nowKey(15))
        assertEquals("evening", nowKey(20))
        assertEquals("sleep", nowKey(21))
        assertEquals("sleep", nowKey(0))
    }

    /** الأبوابُ الأربعةُ الموقوتةُ تُرشَّح كلُّها في يومٍ واحد — لا بابَ ميّت. */
    @Test
    fun `الأبواب الموقوتة الأربعة كلها تظهر`() {
        assertEquals(setOf("morning", "prayer", "evening", "sleep"), (0..23).map { nowKey(it) }.toSet())
    }
}
