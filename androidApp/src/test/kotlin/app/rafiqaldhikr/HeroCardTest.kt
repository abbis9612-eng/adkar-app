package app.rafiqaldhikr

import app.rafiqaldhikr.ui.hero.HeroAnim
import app.rafiqaldhikr.ui.hero.HeroCard
import app.rafiqaldhikr.ui.hero.HeroKind
import app.rafiqaldhikr.ui.hero.HeroStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * قواعدُ قبول بطاقة الشاشة الأولى.
 *
 * هذه ليست اختباراتِ شكل: البطاقةُ تأتي من الشبكة، ولو اختُرق حسابُ
 * النشر لوصل ما فيها إلى شاشة كلّ مستخدم. فكلُّ حالةٍ هنا هجومٌ أو
 * خطأٌ رأيتُه ممكناً.
 */
class HeroCardTest {

    private fun ok(src: String = "", type: String = "") =
        HeroStore.card(id = "w37", from = "2026-09-07", to = "2026-09-14", type = type, src = src)

    @Test
    fun `تقبل بطاقةً سليمة`() {
        val c = HeroStore.card("w37", "2026-09-07", "2026-09-14", title = " ابدأ يومَك ")
        assertNotNull(c)
        assertEquals("ابدأ يومَك", c!!.title)          // تُقلَّم أطرافُها
        assertEquals(HeroKind.TEXT, c.kind)
        assertEquals(HeroAnim.PEN, c.anim)            // القلمُ هو الافتراض
    }

    @Test
    fun `ترفض المسارات في اسم الملف`() {
        //  أخطرُ حقلٍ في البطاقة: لو قُبل مسارٌ لخرج التنزيلُ عن مجلَّد
        //  صاحب التطبيق إلى أيّ مكانٍ في الشبكة.
        for (bad in listOf(
            "../../etc/passwd", "a/b.jpg", "/abs.jpg", "https://evil.example/x.jpg",
            "x.jpg?y=1", "no-extension", ".jpg", "sp ace.jpg",
        )) {
            assertNull("قُبل: $bad", ok(src = bad, type = "image"))
        }
    }

    @Test
    fun `تقبل اسمَ ملفٍّ مجرَّداً`() {
        val c = ok(src = "w38_a-1.jpg", type = "image")
        assertNotNull(c)
        assertEquals(HeroKind.IMAGE, c!!.kind)
        assertEquals("w38_a-1.jpg", c.src)
    }

    @Test
    fun `ترفض صورةً بلا ملفّ`() = assertNull(ok(src = "", type = "image"))

    @Test
    fun `ترفض التواريخَ الفاسدة والمقلوبة`() {
        assertNull(HeroStore.card("w1", "2026-9-7", "2026-09-14"))
        assertNull(HeroStore.card("w1", "أمس", "2026-09-14"))
        assertNull(HeroStore.card("w1", "2026-09-20", "2026-09-14"))   // النهايةُ قبل البداية
    }

    @Test
    fun `ترفض المعرّفاتِ الغريبة`() {
        for (bad in listOf("", "w 37", "w/37", "<b>", "x".repeat(25))) {
            assertNull("قُبل: $bad", HeroStore.card(bad, "2026-09-07", "2026-09-14"))
        }
    }

    @Test
    fun `تقصّ النصوصَ الطويلة`() {
        val c = HeroStore.card("w1", "2026-09-07", "2026-09-14",
            title = "ط".repeat(400), note = "ن".repeat(900))
        assertEquals(48, c!!.title.length)
        assertEquals(120, c.note.length)
    }

    @Test
    fun `تُهمل الأنواعَ التي لا تُرسم بعد`() {
        //  لا تُعرض بطاقةٌ غيرُ التي كُتبت: يُهمَل النوعُ ويبقى المضمَّن.
        assertNull(ok(src = "a.mp4", type = "video"))
        assertNull(ok(type = "misbaha"))
    }

    @Test
    fun `ترفض وجهةً غيرَ معروفة الشكل`() {
        val c = HeroStore.card("w1", "2026-09-07", "2026-09-14",
            route = "javascript:alert(1)")
        assertEquals("", c!!.actionRoute)
    }

    @Test
    fun `تختار بطاقةَ اليوم وحدَها`() {
        val cards = listOf(
            HeroCard("a", "2026-09-01", "2026-09-06"),
            HeroCard("b", "2026-09-07", "2026-09-14"),
            HeroCard("c", "2026-09-15", "2026-09-21"),
        )
        assertEquals("b", HeroStore.pick(cards, "2026-09-07")!!.id)   // أوّلُ يوم
        assertEquals("b", HeroStore.pick(cards, "2026-09-14")!!.id)   // آخرُ يوم
        assertEquals("c", HeroStore.pick(cards, "2026-09-15")!!.id)
        assertNull(HeroStore.pick(cards, "2026-08-30"))               // لا شيءَ بعد
        assertNull(HeroStore.pick(cards, "2026-10-01"))               // انتهت كلُّها
        assertNull(HeroStore.pick(emptyList(), "2026-09-09"))
    }
}
