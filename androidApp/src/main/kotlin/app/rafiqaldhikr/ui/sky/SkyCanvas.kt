package app.rafiqaldhikr.ui.sky

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import app.rafiqaldhikr.ui.theme.contrast

/* ══════════════════════════════════════════════════════════════
   سماءُ المخطوط

   النهارُ وحده أُعيدت صبغتُه. الأزرقُ الفوتوغرافيّ زاويتُه 205–213°
   ولا ينتمي إلى عنقودٍ في اللوحة (دافئٌ 29–48° · أخضرُ 143–153° ·
   نيليٌّ يتيمٌ 225°)، وتشبّعُه 60–68٪ أي ضِعفُ نيليّها الخافت (35٪).
   فصار النهارُ عند 158° — على بُعد 5° من الزمرّد — بتشبّعٍ 32٪.

   والليلُ والغسقُ لم يُمسّا: ليلُ السماء 228.9° و`lightNight` 224.5°
   (فرقُ 3.9°)، وغسقُها 26.9° و`lightDusk` 29.2° (فرقُ 2.3°).

   وفائدةٌ لم تكن مقصودة: المسافةُ بين نهار السماء وليلها كانت 16°
   — أزرقٌ ونيليٌّ جاران لا يفصلهما إلا الإضاءة — فصارت 71°.
══════════════════════════════════════════════════════════════ */

private class Band(val alt: Float, val top: Color, val bottom: Color)

private val SKY = listOf(
    Band(-18f, Color(0xFF070C22), Color(0xFF16204A)),   // ليل
    Band(-12f, Color(0xFF0D1330), Color(0xFF2C2A5C)),   // فلكيّ
    Band(-6f,  Color(0xFF141C42), Color(0xFF7A3A46)),   // بحريّ
    Band(0f,   Color(0xFF22355F), Color(0xFFD98A4A)),   // غسق
    Band(8f,   Color(0xFF3C675E), Color(0xFFECD4AC)),   // ذهبيّ
    Band(25f,  Color(0xFF407765), Color(0xFFBCD7C9)),   // نهار
    Band(70f,  Color(0xFF428069), Color(0xFFC4DECF)),   // ذروة
)

/** لونا السماء عند ارتفاعٍ ما، باستكمالٍ خطّيٍّ بين النطاقات. */
fun skyColors(altitude: Float): Pair<Color, Color> {
    if (altitude <= SKY.first().alt) return SKY.first().top to SKY.first().bottom
    for (i in 0 until SKY.lastIndex) {
        val a = SKY[i]; val b = SKY[i + 1]
        if (altitude <= b.alt) {
            val k = ((altitude - a.alt) / (b.alt - a.alt)).coerceIn(0f, 1f)
            return lerp(a.top, b.top, k) to lerp(a.bottom, b.bottom, k)
        }
    }
    return SKY.last().top to SKY.last().bottom
}

/**
 * الحبرُ الذي يُقرأ على السماء — ينقلب مع ضوئها.
 *
 * ظهراً السماءُ فاتحةٌ فالحبرُ داكن، وليلاً العكس. ويُقاس ولا يُخمَّن:
 * الكريميُّ على سماء الظهيرة 2.34 والداكنُ 7.03 — واختيارُ الأبيض
 * دائماً (وهو ما يفعله كلُّ نموذجٍ رأيتُه) يسقط ثلاثةَ أرباع النهار.
 */
fun skyInk(sky: Pair<Color, Color>): Color {
    val mid = lerp(sky.first, sky.second, 0.72f)
    val cream = Color(0xFFF7F2E6)
    val dark = Color(0xFF0E1613)
    return if (contrast(cream, mid) >= contrast(dark, mid)) cream else dark
}

/** نجومٌ ثابتةُ المواضع — لا عشوائيةَ تتغيّر مع كلِّ إطار. */

/* ══════════════════════════════════════════════════════════════
   وهنا كان الرسمُ — وقد حُذف.

   كان مئةً وثمانين سطراً على `Canvas`: ستٌّ وأربعون نقطةً بيضاءَ
   متساويةُ الحجم موزَّعةً بانتظام، وقرصُ قمرٍ مسطَّحٌ بلونٍ واحدٍ في
   موضعٍ ثابتٍ من الشاشة لا يطلع ولا يغيب، ووهجُ شفقٍ مركزُه وسطُ
   الشاشة لا سَمْتُ الشمس.

   والسماءُ الآن تُحسب على المعالج الرسوميّ في `SkyShader`/`SkyGL`:
   شعاعٌ لكل بكسل، وتشتّتٌ جوّيٌّ يحمرّ الأفقَ من نفسه، ونجومٌ
   بأقدارها، وقمرٌ كرةٌ يقطعها الشعاعُ فالهلالُ نتيجةُ هندسة.

   وبقي من هذا الملفّ ما يُستعمل فعلاً: [skyColors] لِلَون الاحتياط
   حين يتعذّر الرسمُ الرسوميّ، و[skyInk] لحبر الكتابة فوق السماء.
══════════════════════════════════════════════════════════════ */
