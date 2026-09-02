package app.rafiqaldhikr

import app.rafiqaldhikr.ui.sky.WeatherStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ترميزُ WMO — معيارٌ عالميّ، والخطأُ في قراءته يُظهر ثلجاً في بغداد.
 *
 * وهذه الاختباراتُ تثبّت الحالاتِ التي يسهل الخطأ فيها: الحدودُ بين
 * المدَيات، والرمزُ الذي يقول مطراً والمقياسُ يقول صفراً.
 */
class SkyWeatherTest {

    private fun wx(code: Int, cloud: Double = 0.0, mm: Double = 0.0, vis: Double = 50_000.0) =
        WeatherStore.interpret(code, cloud, mm, vis, 25.0)

    @Test
    fun `الصحوُ لا مطرَ فيه ولا ثلجَ ولا ضباب`() {
        val w = wx(0)
        assertEquals(0f, w.rain, 1e-6f); assertEquals(0f, w.snow, 1e-6f); assertEquals(0f, w.fog, 1e-6f)
        assertTrue(w.known)
    }

    @Test
    fun `الغيمُ من نسبته المئوية لا من الرمز`() {
        assertEquals(0.75f, wx(3, cloud = 75.0).cloud, 1e-6f)
        assertEquals(0f, wx(0, cloud = 0.0).cloud, 1e-6f)
    }

    /**
     * الحالُ التي تُظهر سماءً صافيةً والمطرُ ينزل على النافذة: الخدمةُ
     * تُعلن رمزَ مطرٍ ولم تقس مليمتراً بعد. فالحدُّ الأدنى يُبقيه مرئياً.
     */
    @Test
    fun `رمزُ مطرٍ بلا مليمتراتٍ يبقى مطراً يُرى`() {
        val w = wx(61, mm = 0.0)
        assertTrue("مطرٌ معلَنٌ يجب أن يُرى، فجاء ${w.rain}", w.rain >= 0.2f)
        assertEquals(0f, w.snow, 1e-6f)
    }

    @Test
    fun `الوابلُ أشدُّ من الرذاذ`() {
        assertTrue(wx(65, mm = 6.0).rain > wx(51, mm = 0.2).rain)
    }

    @Test
    fun `الثلجُ ثلجٌ لا مطر`() {
        val w = wx(73, mm = 1.0)
        assertTrue(w.snow > 0f); assertEquals(0f, w.rain, 1e-6f)
    }

    /** المدى المرئيّ يكشف ضباباً لم يُعلنه الرمز. */
    @Test
    fun `الضبابُ يُقاس بالمدى المرئيّ أيضاً`() {
        assertTrue(wx(0, vis = 600.0).fog > 0.9f)
        assertEquals(0f, wx(0, vis = 50_000.0).fog, 1e-6f)
        assertTrue(wx(45).fog > 0.7f)
    }

    @Test
    fun `العاصفةُ الرعدية مطرٌ وبرق`() {
        val w = wx(95, mm = 4.0)
        assertTrue(w.thunder); assertTrue(w.rain > 0f)
    }

    @Test
    fun `الشدّاتُ لا تتجاوز الواحد مهما اشتدّ المطر`() {
        val w = wx(82, mm = 900.0, cloud = 400.0)
        assertTrue(w.rain <= 1f && w.cloud <= 1f)
    }
}
