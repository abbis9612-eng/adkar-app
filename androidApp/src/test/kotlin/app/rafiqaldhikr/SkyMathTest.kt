package app.rafiqaldhikr

import app.rafiqaldhikr.ui.sky.moonPhase
import app.rafiqaldhikr.ui.sky.sunPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * حسابُ السماء يُقاس بالفلك لا بالعين: خطأٌ في الإشارة أو في الحقبة
 * يعطي شمساً في السماء ليلاً — وهو ما وقع فعلاً في أوّل نسخة، فظهرت
 * الظهيرةُ بارتفاع ‎−٤٦°.
 */
class SkyMathTest {

    // بغداد
    private val lat = 33.31
    private val lng = 44.36
    /** ٢٦ آب ٢٠٢٦، منتصفُ الليل بتوقيت بغداد — أي 2026-08-25T21:00Z. */
    private val baghdadMidnight = 1_787_691_600_000L

    private fun at(hour: Double) =
        sunPosition(baghdadMidnight + (hour * 3_600_000L).toLong(), lat, lng)

    @Test fun sunPeaksAtSolarNoon() {
        val noon = at(12.12)
        assertTrue("الظهرُ ارتفاعُه ${noon.altitude}", noon.altitude > 60)
        // عند الذروة تكون الشمسُ في الجنوب لمن هو شمال المدار
        assertTrue("سَمْتُ الظهر ${noon.azimuth}", abs(noon.azimuth - 180) < 12)
    }

    @Test fun sunIsBelowHorizonAtMidnight() {
        assertTrue("منتصفُ الليل ${at(0.0).altitude}", at(0.0).altitude < -30)
    }

    @Test fun dawnAndDuskStraddleTheHorizon() {
        assertTrue("قبل الفجر", at(3.0).altitude < -18)
        assertTrue("بعد الشروق", at(8.0).altitude > 0)
        assertTrue("بعد المغرب", at(19.5).altitude < 0)
    }

    /** لا يقفز الارتفاعُ بين دقيقتين متتاليتين — يكشف كسرَ الحقبة. */
    @Test fun altitudeIsContinuous() {
        var prev = at(0.0).altitude
        var h = 0.02
        while (h < 24) {
            val cur = at(h).altitude
            assertTrue("قفزةٌ عند $h: $prev → $cur", abs(cur - prev) < 1.0)
            prev = cur; h += 0.02
        }
    }

    /**
     * جنوبُ الكرة: الذروةُ في الشمال لا الجنوب.
     *
     * ولا يُثبَّت وقتُ الذروة في الاختبار — يُبحث عنه. أوّلُ صيغةٍ ثبّتَته
     * تخميناً فسقط الاختبارُ على حسابٍ سليم، والخطأُ كان في الاختبار.
     */
    @Test fun southernHemisphereNoonFacesNorth() {
        val day = 1_787_616_000_000L                       // ٢٦ آب ٢٠٢٦، منتصفُ ليل UTC
        var best = day
        var high = -90.0
        var m = 0L
        while (m < 1440) {
            val p = sunPosition(day + m * 60_000L, -33.87, 151.21)
            if (p.altitude > high) { high = p.altitude; best = day + m * 60_000L }
            m += 2
        }
        val noon = sunPosition(best, -33.87, 151.21)
        assertTrue("ارتفاعُ ذروة سيدني ${noon.altitude}", noon.altitude > 40)
        assertTrue("سَمْتُ ذروة سيدني ${noon.azimuth}", noon.azimuth < 15 || noon.azimuth > 345)
    }

    /* ── القمر ── */

    @Test fun fullMoonMatchesAstronomy() {
        // بدرُ ٢٦ أيلول ٢٠٢٦ الساعة 16:49 UTC
        val p = moonPhase(1_790_441_340_000L)
        assertEquals("إضاءةُ البدر ${p.illumination}", 1.0, p.illumination, 0.02)
    }

    @Test fun newMoonMatchesAstronomy() {
        // محاقُ ١١ أيلول ٢٠٢٦ الساعة 03:27 UTC
        val p = moonPhase(1_789_097_220_000L)
        assertTrue("إضاءةُ المحاق ${p.illumination}", p.illumination < 0.03)
    }

    @Test fun illuminationStaysInRangeAndCycles() {
        var t = 1_787_691_600_000L
        var sawFull = false
        var sawNew = false
        repeat(60) {
            val p = moonPhase(t)
            assertTrue(p.illumination in 0.0..1.0)
            assertTrue(p.age in 0.0..29.6)
            if (p.illumination > 0.97) sawFull = true
            if (p.illumination < 0.03) sawNew = true
            t += 86_400_000L
        }
        assertTrue("لم يمرّ بدرٌ في ستّين يوماً", sawFull)
        assertTrue("لم يمرّ محاقٌ في ستّين يوماً", sawNew)
    }
}
