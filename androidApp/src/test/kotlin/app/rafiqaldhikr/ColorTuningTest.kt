package app.rafiqaldhikr

import androidx.compose.ui.graphics.Color
import app.rafiqaldhikr.ui.theme.*
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ضمانُ القراءة على كل تركيبةٍ يستطيع المستخدم اختيارها.
 *
 * السماح بلونٍ حرّ بلا هذا الاختبار وعدٌ لا يُحرَس: يكفي ورقٌ داكن مع
 * لهجةٍ داكنة ليصير التطبيق غير مقروء، ولا شيء يمنعه. هنا تُفحص
 * ١٥×١٥ = ٢٢٥ تركيبة، وكلُّها لا بدّ أن تعبر.
 */
class ColorTuningTest {

    private val base = LightRafiqPalette

    private fun check(paper: Color, accent: Color) {
        val p = base.tuned(paper, accent)
        val where = "ورق=${paper.value.toString(16).take(8)} لهجة=${accent.value.toString(16).take(8)}"

        assertTrue("$where: النصّ ${contrast(p.ink, p.bg)}", contrast(p.ink, p.bg) >= 4.5f)
        assertTrue("$where: الثانوي ${contrast(p.inkMed, p.bg)}", contrast(p.inkMed, p.bg) >= 4.5f)
        assertTrue("$where: نصّ اللهجة ${contrast(p.emerald, p.bg)}", contrast(p.emerald, p.bg) >= 4.4f)
        assertTrue("$where: الذهبي ${contrast(p.gold, p.bg)}", contrast(p.gold, p.bg) >= 4.4f)
        assertTrue("$where: الزرّ ${contrast(p.onEmeraldFill, p.emeraldFill)}",
            contrast(p.onEmeraldFill, p.emeraldFill) >= 4.5f)
        assertTrue("$where: الثانوي على البطاقة ${contrast(p.inkMed, p.card)}",
            contrast(p.inkMed, p.card) >= 4.5f)
        assertTrue("$where: نصّ اللهجة على البطاقة ${contrast(p.emerald, p.card)}",
            contrast(p.emerald, p.card) >= 4.4f)
        assertTrue("$where: الأيقونات ${contrast(p.inkLight, p.card)}",
            contrast(p.inkLight, p.card) >= 3.0f)
        assertTrue("$where: نبرة البطاقة ${contrast(p.card, p.bg)}", contrast(p.card, p.bg) >= 1.15f)
    }

    /** كل ورقٍ مع كل لهجة — ٢٢٥ تركيبة. */
    @Test fun everyCombinationStaysReadable() {
        for (paper in PaperSwatches) for (accent in AccentSwatches) check(paper, accent)
    }

    /** التركيبات الجاهزة التسع. */
    @Test fun presetsStayReadable() {
        for (p in ColorPresets) check(p.paper, p.accent)
    }

    /** الحالات القصوى: أبيضُ نقيّ وأسودُ نقيّ. */
    @Test fun pureWhiteAndBlack() {
        for (a in AccentSwatches) { check(Color.White, a); check(Color.Black, a) }
    }

    /** لهجةٌ تساوي الورق تماماً — أسوأ مدخلٍ ممكن. */
    @Test fun accentEqualsPaper() {
        check(Color(0xFFF5EBD5), Color(0xFFF5EBD5))
        check(Color(0xFF101511), Color(0xFF101511))
    }

    /** بلا تخصيص تبقى اللوحة كما هي حرفياً. */
    @Test fun noOverrideKeepsPalette() {
        assertTrue(base.tuned(null, null) === base)
    }
}
