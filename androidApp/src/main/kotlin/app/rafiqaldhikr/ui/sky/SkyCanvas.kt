package app.rafiqaldhikr.ui.sky

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.contrast
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

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
private class Star(val x: Float, val y: Float, val r: Float, val a: Float)

private val STARS: List<Star> = Random(7).let { rnd ->
    List(46) { Star(rnd.nextFloat(), rnd.nextFloat() * 0.62f, 0.45f + rnd.nextFloat() * 0.75f, 0.35f + rnd.nextFloat() * 0.65f) }
}

/** غيومٌ ثابتةُ المواضع، لونُها من ضوء الساعة. */
private class Cloud(val x: Float, val y: Float, val w: Float, val a: Float)

/*  خمسٌ لا تسع، وناعمةُ الحواف.
 *
 *  أوّلُ نسخةٍ نُقلت عن نموذج HTML حرفياً — وهناك كان `filter: blur(11px)`
 *  يذيب حوافَّها. وليس لـ`drawOval` في Compose مقابلٌ لذلك، فنُقلت
 *  الأشكالُ وسقط الضباب: تسعُ غيماتٍ × ثلاثِ كتلٍ = سبعٌ وعشرون قطعةً
 *  بيضاءَ حادّةَ الحافّة بشفافية 0.75. لُطخٌ لا غيوم.
 *
 *  والعلاجُ ليس ضباباً بل تدرّجٌ شعاعيّ يتلاشى إلى الشفافية عند الحافّة:
 *  ناعمٌ بطبعه، وأرخصُ من أيّ مرشِّح.
 */
private val CLOUDS: List<Cloud> = kotlin.random.Random(23).let { rnd ->
    List(5) {
        Cloud(
            x = rnd.nextFloat() * 1.24f - 0.12f,
            y = 0.30f + rnd.nextFloat() * 0.30f,
            w = 0.34f + rnd.nextFloat() * 0.26f,
            a = 0.62f + rnd.nextFloat() * 0.38f,
        )
    }
}

/**
 * @param altitude ارتفاعُ الشمس بالدرجات · @param azimuth سَمْتُها
 * @param moon طورُ القمر — يُرسم حين تغيب الشمس
 */
@Composable
fun SkyCanvas(
    altitude: Float,
    azimuth: Float,
    moon: MoonPhase,
    rc: RafiqPalette,
    modifier: Modifier = Modifier,
) {
    val sky = remember(altitude) { skyColors(altitude) }
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val up = altitude > -6f

        drawRect(
            Brush.verticalGradient(
                0f to sky.first,
                0.58f to lerp(sky.first, sky.second, 0.55f),
                1f to sky.second,
            ),
        )

        // النجوم — تظهر تدريجاً بعد ‎−٣° وتكتمل عند ‎−١٦°
        val night = ((-altitude - 3f) / 13f).coerceIn(0f, 1f)
        if (night > 0.01f) {
            STARS.forEach { s ->
                drawCircle(
                    Color.White.copy(alpha = s.a * night),
                    radius = s.r * (w / 360f) * 2f,
                    center = Offset(s.x * w, s.y * h),
                )
            }
        }

        /*  السَّمْتُ يعطي الموضع الأفقيّ والارتفاعُ الرأسيّ. والمدى
            55°–305° هو قوسُ النهار المرئيّ؛ خارجَه القرصُ تحت الأفق. */
        /*  القرصُ كان يقع خلف «نهارٌ طيّب»: عند ارتفاع 47° موضعُه 36٪
            من السماء، والنصُّ يشغل أعلى 44٪. فحُصر في النطاق الخالي بين
            التحيّة والحبّة — 40٪–70٪ — ويبقى تدرّجُه مقروءاً. */
        val x = (((azimuth - 55f) / 250f).coerceIn(0.08f, 0.92f)) * w
        val y = (0.70f - (altitude.coerceIn(-8f, 70f) / 70f) * 0.30f) * h

        // الغيومُ خلف القرص: أعلاها من السماء وأسفلُها من ضوء الشمس
        val cloudTint = when {
            altitude > 14f -> Color(0xFFFFFFFF)
            altitude > 2f  -> Color(0xFFFFE6C0)
            altitude > -6f -> Color(0xFFE8A07A)
            else           -> Color(0xFF5A6488)
        }
        val cloudVis = if (altitude > -9f) 1f else ((altitude + 16f) / 7f).coerceIn(0f, 1f)
        if (cloudVis > 0.01f) drawClouds(cloudTint, cloudVis)

        if (up) {
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFD684).copy(alpha = 0.34f), Color.Transparent),
                    center = Offset(x, y), radius = w * 0.34f,
                ),
                radius = w * 0.34f, center = Offset(x, y),
            )
            drawCircle(
                if (altitude < 8f) Color(0xFFFFE0A8) else Color(0xFFFFF6DE),
                radius = w * 0.052f, center = Offset(x, y),
            )
        } else {
            // القمرُ مقابلُ الشمس تقريباً، وبطورِه المحسوب
            val mx = w - x
            val my = (1.10f - (y / h)).coerceIn(0.38f, 0.68f) * h
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFC6D4F2).copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(mx, my), radius = w * 0.24f,
                ),
                radius = w * 0.24f, center = Offset(mx, my),
            )
            drawMoon(Offset(mx, my), w * 0.048f, moon)
        }

        /*  الوهجُ يتمركز على الشمس لا على وسط الشاشة — فعند المغرب
            يشتعل الطرفُ الذي فيه الشمسُ ويبقى الآخرُ أزرق، وهو ما
            تراه العينُ فعلاً. والتدرّجُ المتساوي هو ما يفضح الرسم. */
        val warm = when {
            altitude > 14f -> Color(0xFFFFF6D6).copy(alpha = 0.16f)
            altitude > 2f  -> Color(0xFFFFD68C).copy(alpha = 0.42f)
            altitude > -6f -> Color(0xFFFFA858).copy(alpha = 0.52f)
            altitude > -12f-> Color(0xFFD26E5A).copy(alpha = 0.34f)
            else           -> Color(0xFF7878AA).copy(alpha = 0.16f)
        }
        drawRect(
            Brush.radialGradient(
                listOf(warm, Color.Transparent),
                center = Offset(w - x, h * 1.04f),
                radius = w * 1.2f,
            ),
        )

        /*  شريطُ ذهبٍ حيث تلتقي السماءُ الورق — goldLight من اللوحة،
            الفاصلُ الذي تستعمله المصاحفُ بين اللازورد والذهب. كان
            اللقاءُ حافّةً حادّةً بين لونين لا يجمعهما شيء (نبرة 1.93). */
        val edge = lerp(sky.second, rc.goldLight, 0.34f)
        drawRect(
            Brush.verticalGradient(0f to Color.Transparent, 1f to edge),
            topLeft = Offset(0f, h * 0.74f),
            size = Size(w, h * 0.26f),
        )
    }
}

private fun DrawScope.drawClouds(tint: Color, vis: Float) {
    val w = size.width
    val h = size.height
    CLOUDS.forEach { c ->
        val cw = c.w * w
        val ch = cw * 0.30f
        val cx = c.x * w
        val cy = c.y * h
        // ثلاثُ نفخاتٍ متداخلةٍ تصنع غيمةً — وكلُّ نفخةٍ تتلاشى عند حافّتها
        listOf(-0.30f to 0.74f, 0.02f to 1f, 0.31f to 0.66f).forEach { (dx, sc) ->
            val rw = cw * sc
            val rh = ch * sc
            val left = cx + dx * cw - rw / 2f
            val topY = cy - rh / 2f
            drawOval(
                Brush.radialGradient(
                    0f to tint.copy(alpha = c.a * vis * 0.22f),
                    0.55f to tint.copy(alpha = c.a * vis * 0.15f),
                    1f to Color.Transparent,
                    center = Offset(left + rw / 2f, topY + rh / 2f),
                    radius = rw / 2f,
                ),
                topLeft = Offset(left, topY),
                size = Size(rw, rh),
            )
        }
    }
}

/**
 * قرصُ القمر بطورِه: نصفُ دائرةٍ خارجيّ، ثمّ قوسُ الفاصل الضوئيّ عائداً.
 * نصفُ قطر القوس `R(1−2k)` حيث k نسبةُ الإضاءة — فيكون صفراً عند
 * التربيع (خطٌّ مستقيم) وسالباً عند الأحدب (فينتفخ إلى الخارج).
 */
private fun DrawScope.drawMoon(center: Offset, r: Float, p: MoonPhase) {
    val col = Color(0xFFEDF1FA)
    val hair = max(1f, size.width / 360f)
    val k = p.illumination.toFloat()
    when {
        k > 0.985f -> drawCircle(col, r, center)
        k < 0.015f -> drawCircle(col.copy(alpha = 0.28f), r, center, style = Stroke(hair))
        else -> {
            val rx = r * (1f - 2f * k)
            val disc = Path().apply { addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r)) }
            val term = Path().apply {
                addOval(Rect(center.x - abs(rx), center.y - r, center.x + abs(rx), center.y + r))
            }
            val half = Path().apply {
                val left = if (p.waxing) center.x else center.x - r
                addRect(Rect(left, center.y - r, left + r, center.y + r))
            }
            val lit = Path().apply {
                op(disc, half, PathOperation.Intersect)
                if (rx < 0f) op(this, term, PathOperation.Union) else op(this, term, PathOperation.Difference)
                op(this, disc, PathOperation.Intersect)
            }
            drawPath(lit, col)
        }
    }
}
