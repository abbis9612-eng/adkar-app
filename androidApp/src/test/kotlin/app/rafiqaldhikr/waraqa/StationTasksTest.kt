package app.rafiqaldhikr.waraqa

import app.rafiqaldhikr.ui.screens.waraqa.StationTask
import app.rafiqaldhikr.ui.screens.waraqa.StationTasks
import app.rafiqaldhikr.ui.screens.waraqa.penLoopPoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * مهامُّ المحطّة تُقرأ من ثلاثة أماكن: `PrayerLog` للصلاة، و`DayStationLog`
 * لغيرها، و`DailyProgress` لأذكار الصباح والمساء من شاشة القراءة. وأيُّ
 * خلطٍ بينها يُري «تمّت» لما لم يُفعل — أو يُخفيها عمّا فُعل.
 */
class StationTasksTest {

    private val none = emptySet<String>()

    @Test fun prayerTaskReadsPrayerLogOnly() {
        val fajr = StationTasks.of("fajr_morning").first { it.kind == StationTask.Kind.PRAYER }
        assertEquals("fajr", fajr.key)
        assertTrue(StationTasks.isDone(fajr, setOf("fajr"), none, false, false))
        // مفتاحٌ بالاسم نفسِه في سجلّ المحطّات لا يشهد بصلاة
        assertFalse(StationTasks.isDone(fajr, none, setOf("fajr"), false, false))
    }

    @Test fun morningAdhkarCountsFromReadingScreen() {
        val morning = StationTasks.of("fajr_morning").first { it.adhkar == "morning" }
        assertTrue(StationTasks.isDone(morning, none, none, morning = true, evening = false))
        assertFalse(StationTasks.isDone(morning, none, none, morning = false, evening = true))
        assertTrue(StationTasks.isDone(morning, none, setOf(morning.key), false, false))
    }

    @Test fun stationDoneWhenAllTasksDone() {
        val tasks = StationTasks.of("isha")
        assertFalse(StationTasks.isStationDone("isha", tasks, setOf("isha"), none, false, false))
        assertTrue(StationTasks.isStationDone("isha", tasks, setOf("isha"), setOf("isha.witr"), false, false))
    }

    @Test fun stationDoneWhenMarkedItself() {
        assertTrue(StationTasks.isStationDone("wake", StationTasks.of("wake"), none, setOf("wake"), false, false))
    }

    @Test fun unknownStationHasNoTasksAndIsNotDone() {
        assertTrue(StationTasks.of("nope").isEmpty())
        assertFalse(StationTasks.isStationDone("nope", emptyList(), none, none, false, false))
    }

    @Test fun taskKeysAreUnique() {
        val ids = listOf("wake", "fajr_morning", "duha", "dhuhr", "asr_evening", "maghrib", "isha", "sleep", "friday_kahf", "wird")
        val keys = ids.flatMap { StationTasks.of(it) }.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test fun penLoopIsDeterministicAndDiffersPerPrayer() {
        assertEquals(penLoopPoints(2), penLoopPoints(2))
        assertNotEquals(penLoopPoints(1), penLoopPoints(2))
        // الدائرةُ داخل مربّعها — تحيط بالرقم ولا تخرج عن خلّيته
        penLoopPoints(3).forEach { (x, y) -> assertTrue(x in 0f..1f && y in 0f..1f) }
    }
}
