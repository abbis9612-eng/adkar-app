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

    /**
     * البطاقة لا بدّ أن تُرى — لكن ليس بالنبرة بالضرورة.
     *
     * كان المعيار «نبرتها عن الورق ≥ 1.2». وهو صحيحٌ لبطاقةٍ معبّأة،
     * وخاطئٌ للبطاقة البيضاء على ورقٍ شبه أبيض: نبرتُها 1.018 لأن
     * التعبئة لا ترسمها أصلاً — يرسمها الحدُّ والظلّ. فالمطلوب أن
     * تُميَّز بإحدى الآليتين لا بواحدةٍ بعينها.
     */
    private fun cardIsDistinguishable(p: RafiqPalette, where: String) {
        val tone   = contrast(p.card, p.bg)
        val border = contrast(p.cardBorder, p.bg)
        assertTrue(
            "$where: البطاقة غير مميَّزة — نبرة $tone وحدّ $border",
            tone >= 1.15f || border >= 1.15f,
        )
        assertTrue("$where: الحدّ فوق البطاقة ${contrast(p.cardBorder, p.card)}",
            contrast(p.cardBorder, p.card) >= 1.10f)
    }

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
        cardIsDistinguishable(p, where)
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

    /**
     * اللوحتان المشحونتان — «ورقٌ وعنبر» — تُفحصان كما تُفحص أي تركيبة
     * يختارها المستخدم. لا استثناء للافتراضي: هو ما يراه كلُّ من يفتح
     * التطبيق قبل أن يمسّ الإعدادات، فهو الأولى بالحراسة لا العكس.
     */
    @Test fun shippedPalettesAreReadable() {
        for (p in listOf(LightRafiqPalette, DarkRafiqPalette)) {
            assertTrue("النصّ ${contrast(p.ink, p.bg)}", contrast(p.ink, p.bg) >= 4.5f)
            assertTrue("الثانوي على البطاقة ${contrast(p.inkMed, p.card)}",
                contrast(p.inkMed, p.card) >= 4.5f)
            assertTrue("الثانوي على الورق ${contrast(p.inkMed, p.bg)}",
                contrast(p.inkMed, p.bg) >= 4.5f)
            assertTrue("الثانوي على الحبّة ${contrast(p.inkMed, p.chipBg)}",
                contrast(p.inkMed, p.chipBg) >= 4.5f)
            assertTrue("اللهجة نصّاً ${contrast(p.emerald, p.card)}",
                contrast(p.emerald, p.card) >= 4.5f)
            assertTrue("الذهبي ${contrast(p.gold, p.card)}", contrast(p.gold, p.card) >= 4.5f)
            assertTrue("نصّ الزرّ ${contrast(p.onEmeraldFill, p.emeraldFill)}",
                contrast(p.onEmeraldFill, p.emeraldFill) >= 4.5f)
            assertTrue("الأيقونات ${contrast(p.inkLight, p.card)}",
                contrast(p.inkLight, p.card) >= 3.0f)
            cardIsDistinguishable(p, "مشحونة")
            assertTrue("onEmerald فوق اللهجة ${contrast(p.onEmerald, p.emerald)}",
                contrast(p.onEmerald, p.emerald) >= 4.5f)
        }
    }

    /** بلا تخصيص تبقى اللوحة كما هي حرفياً. */
    @Test fun noOverrideKeepsPalette() {
        assertTrue(base.tuned(null, null) === base)
    }
}
