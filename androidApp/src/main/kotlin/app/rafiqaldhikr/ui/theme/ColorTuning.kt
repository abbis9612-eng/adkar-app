package app.rafiqaldhikr.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/* ═══════════════════════════════════════════════════════════════════
   اشتقاق لوحةٍ كاملة من لونين يختارهما المستخدم

   السماح باختيار لونٍ حرّ يعني السماح بكسر التباين — ومستخدمٌ يختار
   ورقاً داكناً ثم يرى حبراً داكناً فوقه لا يلوم نفسه، يلوم التطبيق.

   فالمستخدم يختار اثنين لا غير: الورق واللهجة. وكلُّ ما عداهما يُشتقّ:
     • اتجاه الحبر (فاتح/داكن) من إضاءة الورق
     • درجة الثانوي بالمشي نحو الورق حتى تقف عند ٥.٢ تماماً
     • نصّ اللهجة بتغميق/تفتيح اللهجة نفسها حتى تعبر ٤.٥ على الورق
     • لون المحتوى فوق اللهجة من إضاءتها

   فلا تركيبةَ تكسر القراءة مهما اختار.
═══════════════════════════════════════════════════════════════════ */

/** نسبة التباين بين لونين (WCAG). */
fun contrast(a: Color, b: Color): Float {
    val la = a.luminance(); val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

private fun mix(a: Color, b: Color, t: Float) = Color(
    red   = a.red   + (b.red   - a.red)   * t,
    green = a.green + (b.green - a.green) * t,
    blue  = a.blue  + (b.blue  - a.blue)  * t,
)

/**
 * أقربُ درجةٍ من [tint] تعبر [minRatio] على [bg].
 *
 * تمشي نحو الأسود إن كان الورق فاتحاً، ونحو الأبيض إن كان داكناً —
 * فتحفظ درجة اللون (hue) وتغيّر الإضاءة وحدها. وهذا بالضبط ما فعلناه
 * يدوياً حين صار #4ECF95 هو #1C6846 نصّاً: اللون نفسه أغمق.
 */
fun readableOn(tint: Color, bg: Color, minRatio: Float = 4.5f): Color {
    if (contrast(tint, bg) >= minRatio) return tint
    val target = if (bg.luminance() > 0.35f) Color.Black else Color.White
    var lo = 0f; var hi = 1f
    repeat(18) {
        val m = (lo + hi) / 2f
        if (contrast(mix(tint, target, m), bg) >= minRatio) hi = m else lo = m
    }
    return mix(tint, target, hi)
}

/**
 * لون المحتوى فوق سطحٍ مملوء.
 *
 * العتبة الثابتة («فاتحٌ فوق الداكن») قرارٌ ساذج: لهجةٌ متوسّطة الإضاءة
 * مثل الزيتوني #8CA83E تُعطي 2.41 مع أيٍّ من الطرفين المائلين. الاختبار
 * كشفها على ثلاث تركيباتٍ من ٢٢٥.
 *
 * فالاختيار الآن بالقياس: أعلى الأربعة تبايناً. وحبرُنا المائل مفضَّل
 * (أدفأ)، فإن لم يبلغ ٤.٥ سقطنا إلى الأسود أو الأبيض النقيّ — وهما
 * يضمنان ٤.٥٨ على الأقلّ لأيّ لونٍ ممكن: أسوأ حالةٍ رياضية هي إضاءة
 * 0.179 حيث يتساوى الطرفان عند 4.58.
 */
fun onFillFor(fill: Color): Color {
    val warm = listOf(Color(0xFF14201A), Color(0xFFF6F2E6))
    val best = warm.maxByOrNull { contrast(it, fill) }!!
    if (contrast(best, fill) >= 4.5f) return best
    return listOf(Color.Black, Color.White).maxByOrNull { contrast(it, fill) }!!
}

/**
 * يبني لوحةً كاملة من [base] بعد استبدال الورق و اللهجة.
 *
 * ما لا يُشتقّ يبقى من [base]: الذهبي وسلّم الضوء وشارات المصحف وألوان
 * الحالة — لأنها معانٍ لا زينة، وتغييرها مع الورق يفقدها دلالتها.
 */
fun RafiqPalette.tuned(paper: Color?, accent: Color?): RafiqPalette {
    if (paper == null && accent == null) return this

    val bg      = paper ?: this.bg
    val light   = bg.luminance() > 0.45f
    val ink     = if (light) Color(0xFF14201A) else Color(0xFFEDE6D4)
    val fill    = accent ?: this.emeraldFill

    // البطاقة أوّلاً: نبرةٌ مرئية (≥1.2). الورق الفاتح جداً لا يقبل أفتحَ
    // منه، فتصير غائرةً أغمق — وهو ما اضطُررنا إليه مع #F5EBD5 يدوياً.
    val cardT = if (light) 0.10f else 0.13f
    val card  = mix(bg, if (light) Color.Black else Color.White, cardT)

    /*  الثانوي يُضبط على البطاقة لا على الورق.
     *
     *  ضبطتُه أوّلاً على الورق فعبَر عنده وسقط فوق البطاقة إلى 4.19:
     *  البطاقة تتحرّك دائماً باتجاه الحبر (أغمق على الفاتح، أفتح على
     *  الداكن) فهي السطح الأصعب في الحالتين. والاختبار كشفه على
     *  ٢٢٥ تركيبة قبل أن يصل جهازاً.
     */
    var inkMed = ink
    run {
        var lo = 0f; var hi = 1f
        repeat(18) {
            val m = (lo + hi) / 2f
            if (contrast(mix(ink, bg, m), card) >= 5.2f) lo = m else hi = m
        }
        inkMed = mix(ink, bg, lo)
    }

    // الأيقونات عتبتها ٣:١ — تُضبط على البطاقة أيضاً وللسبب نفسه.
    var inkLight = ink
    run {
        var lo = 0f; var hi = 1f
        repeat(18) {
            val m = (lo + hi) / 2f
            if (contrast(mix(ink, bg, m), card) >= 3.3f) lo = m else hi = m
        }
        inkLight = mix(ink, bg, lo)
    }

    return copy(
        bg            = bg,
        card          = card,
        cardPrayed    = mix(bg, ink, cardT * 0.7f),
        chipBg        = mix(bg, ink, cardT * 0.55f),
        emeraldFill   = fill,
        onEmeraldFill = onFillFor(fill),
        emerald       = readableOn(fill, card, 4.5f),
        emeraldMed    = readableOn(fill, card, 5.5f),
        ink           = ink,
        inkDark       = mix(ink, bg, 0.12f),
        inkMed        = inkMed,
        inkLight      = inkLight,
        divider       = mix(bg, ink, 0.16f),
        gold          = readableOn(this.gold, card, 4.5f),
        onEmerald     = onFillFor(readableOn(fill, card, 4.5f)),
    )
}

/** التركيبات الجاهزة — كلُّها مقيسة، والاشتقاق يضمن الباقي. */
data class ColorPreset(val name: String, val paper: Color, val accent: Color)

val ColorPresets = listOf(
    ColorPreset("ورقٌ وعنبر",    Color(0xFFF4F1E7), Color(0xFFE0A83C)),   // الافتراضي المشحون
    ColorPreset("ورقٌ وزمرّد",   Color(0xFFF5EBD5), Color(0xFF4ECF95)),
    ColorPreset("رملٌ وزيتون",   Color(0xFFF2EFE4), Color(0xFF8CA83E)),
    ColorPreset("صنوبر ووردة",   Color(0xFFF5F2EF), Color(0xFFD98C97)),
    ColorPreset("أبيضُ وحبر",    Color(0xFFFBFAF7), Color(0xFF3E8F72)),
    ColorPreset("رمادٌ وفيروز",  Color(0xFFEBE8E0), Color(0xFF4FC3C6)),
    ColorPreset("ليلٌ وزمرّد",   Color(0xFF101511), Color(0xFF4ECF95)),
    ColorPreset("ليلٌ وذهب",     Color(0xFF0A0B10), Color(0xFFE7C57C)),
    ColorPreset("ليلٌ وفضّة",    Color(0xFF0B0E1A), Color(0xFFB8CCE1)),
)

/** ألوان الورق المتاحة للاختيار الحرّ. */
val PaperSwatches = listOf(
    0xFFFBFAF7, 0xFFF5EBD5, 0xFFF4F1E7, 0xFFF2EFE4, 0xFFEDE7DA,
    0xFFEBE8E0, 0xFFE7DFCD, 0xFFDCE6E2, 0xFFEDE4EC, 0xFFF6E9E4,
    0xFF2A2A28, 0xFF1C2320, 0xFF101511, 0xFF0A0B10, 0xFF0B0E1A,
).map { Color(it) }

/** ألوان اللهجة المتاحة. */
val AccentSwatches = listOf(
    0xFF4ECF95, 0xFF3E8F72, 0xFF0A5433, 0xFF8CA83E, 0xFF4FC3C6,
    0xFFE0A83C, 0xFFE7C57C, 0xFFD98C4A, 0xFFD98C97, 0xFFB8748E,
    0xFF9B7FD4, 0xFF6E92D8, 0xFF3E6FA8, 0xFFB8CCE1, 0xFF8A6414,
).map { Color(it) }
