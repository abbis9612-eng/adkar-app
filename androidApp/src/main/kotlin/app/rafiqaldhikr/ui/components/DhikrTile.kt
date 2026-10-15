package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.sky.skyColors
import app.rafiqaldhikr.ui.sky.skyInk
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ═══════════════════════════════════════════════════════════════════
   بَلاطةُ الباب — تدرُّجُها سماءُ وقتِه، ورمزُها يُعرَف بلمحة

   كانت الأبوابُ الخمسةُ خمسَ دوائرَ خضراءَ متطابقة ([CategoryBadge]
   بتدرّجٍ واحدٍ مكتوبٍ في الكود)، يفرّقها رسمٌ صغيرٌ داخلها. فالشاشةُ
   تُقرأ صفّاً واحداً مكرّراً، ولا يُعرف بابٌ من بابٍ إلّا بقراءة اسمه.

   وهنا شيئان تغيّرا:

   ١) اللونُ معلومة. تدرُّجُ كلِّ بَلاطةٍ من [skyColors] — سلّمِ السماء
      نفسِه الذي تعلو به الرئيسيةُ وتُمسي. فبابُ الصباح سماءُ صباحٍ
      وبابُ النوم سماءُ ليلٍ، **بلا أن تُقرأ كلمة**. ولا لونَ مخترَعاً
      هنا: ما من قيمةٍ سُداسيّةٍ في هذا الملفّ إلّا حبرُ الرمز.

   ٢) الرمزُ يُعرَف لا يُحَلّ. كانت الأيقوناتُ خطوطاً شعريّةً دقيقةً
      تذوب عند ٢٨ نقطة. وهذه أقراصٌ وأهلّةٌ ممتلئةٌ وقوسُ محرابٍ
      مصمتة — تُقرأ عند ٢٤ نقطةً كما تُقرأ عند ٥٦.

   وسمكُ القلم واحدٌ في الخمسة، والمقاسُ البصريُّ واحد، والحبرُ واحد —
   لغةٌ واحدةٌ بكلماتٍ مختلفة، كما تقول قاعدةُ الأيقونات.
═══════════════════════════════════════════════════════════════════ */

/**
 * ارتفاعُ الشمسِ الذي يُلوِّن كلَّ باب — بالدرجات.
 *
 * وهو **تمثيلٌ لوقتِ البابِ لا قياسُ لحظتِك**: شاشةُ الأبواب لا تملك
 * إحداثيّاتٍ ولا تطلبها، فلو رُبط اللونُ بالشمسِ الحيّة لتغيّر البابُ
 * بين فتحةٍ وفتحة وصار علامةً لا يُعتمد عليها. فالقيمةُ ثابتةٌ مقصودة،
 * ومعناها «سماءُ هذا الوقت» لا «السماءُ الآن».
 *
 *  - [MORNING] ‎+١٢°‎  الشمسُ ارتفعت بعد الشروق — وقتُ أذكار الصباح
 *  - [PRAYER]  ‎+٤٥°‎  كبدُ السماء — والصلواتُ الخمسُ موقوتةٌ بها كلُّها
 *  - [EVENING] ‎+٢°‎   الشمسُ على الأفق — من العصر إلى المغرب
 *  - [SLEEP]   ‎−٦٠°‎  جوفُ الليل
 *  - [MISC]    ‎+٢٦°‎  نهارٌ مطلَقٌ — أذكارٌ لا وقتَ لها
 */
private object SkyAlt {
    const val MORNING = 12f
    const val PRAYER  = 45f
    const val EVENING = 2f
    const val SLEEP   = -60f
    const val MISC    = 26f
}

/**
 * حبرُ الرمز — **تختاره السماءُ لا أنا**، بـ[skyInk].
 *
 * كتبتُه أوّلاً كريميّاً واحداً لكلِّ البَلاطات، فاختفى قوسُ المحرابِ
 * في سماءِ الظهيرة: أسفلُ تدرُّجِها `#C4DECF`، وكريميٌّ فوق فاتحٍ لا
 * يُرى. و[skyInk] هي أداةُ التطبيق لهذا بعينه — تقيس التباينَ وتختار
 * الكريميَّ أو الحبرَ الداكن. فالبَلاطةُ المضيئةُ رمزُها داكنٌ كما
 * تُقرأ لافتةٌ في الشمس، والليليّةُ رمزُها كريميّ.
 */
private fun inkFor(sky: Pair<Color, Color>): Color = skyInk(sky)

private fun altitudeOf(key: String): Float = when (key) {
    "morning" -> SkyAlt.MORNING
    "prayer"  -> SkyAlt.PRAYER
    "evening" -> SkyAlt.EVENING
    "sleep"   -> SkyAlt.SLEEP
    else      -> SkyAlt.MISC
}

/**
 * بَلاطةُ بابٍ من أبواب الذِّكر.
 *
 * @param key مفتاحُ الباب كما في `ADHKAR_CATS` و`DatabaseSeeder` —
 *            `morning` · `evening` · `sleep` · `prayer` · وما سواها متفرّقات.
 */
@Composable
fun DhikrTile(
    key: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val sky = skyColors(altitudeOf(key))
    Canvas(
        modifier
            .size(size)
            /*  الزاويةُ ٣٠٪ من الضلع لا قيمةً ثابتة: البَلاطةُ تُعرض بين
             *  ٢٤ و٦٤ نقطة، ونصفُ قطرٍ ثابتٌ يجعل الصغيرةَ دائرةً
             *  والكبيرةَ مربّعاً. والنسبةُ تُبقيها البَلاطةَ نفسَها. */
            .clip(RoundedCornerShape(size * 0.30f)),
    ) {
        val s = this.size.minDimension

        // سماءُ الوقت — الأفتحُ أسفلَ كالأفق
        drawRect(
            Brush.linearGradient(
                listOf(sky.first, lerp(sky.first, sky.second, 0.55f), sky.second),
                start = Offset(s * 0.18f, 0f),
                end   = Offset(s * 0.82f, s),
            ),
        )
        // لمعةُ زجاجٍ خفيفةٌ في الركن — حدُّ الضوء، لا لونٌ جديد
        drawRect(
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                start = Offset(s, 0f),
                end   = Offset(s * 0.42f, s * 0.52f),
            ),
        )

        val k = s / 32f          // رسمُ الرموز على شبكةٍ من ٣٢ وحدة
        val pen = Stroke(2.3f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val ink = inkFor(sky)
        when (key) {
            "morning" -> drawMorning(k, pen, ink)
            "evening" -> drawEvening(k, pen, ink)
            "sleep"   -> drawSleep(k, ink)
            "prayer"  -> drawPrayer(k, pen, ink)
            else      -> drawMisc(k, ink)
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════
   الرموزُ الخمسة — على شبكةِ ٣٢ وحدة، و[k] معاملُ التكبير
═══════════════════════════════════════════════════════════════════ */

private fun DrawScope.line(
    k: Float, x1: Float, y1: Float, x2: Float, y2: Float, pen: Stroke, ink: Color, a: Float = 1f,
) = drawLine(ink.copy(alpha = a), Offset(x1 * k, y1 * k), Offset(x2 * k, y2 * k), pen.width, pen.cap)

/** شعاعٌ من مركزٍ حرّ — بزاويةٍ بالدرجات ومدىً من [r1] إلى [r2]. */
private fun DrawScope.ray(
    k: Float, cx: Float, cy: Float, deg: Float, r1: Float, r2: Float, pen: Stroke, ink: Color,
) {
    val t = deg * PI.toFloat() / 180f
    line(k, cx + r1 * cos(t), cy + r1 * sin(t), cx + r2 * cos(t), cy + r2 * sin(t), pen, ink)
}

private fun DrawScope.disc(k: Float, cx: Float, cy: Float, r: Float, ink: Color) =
    drawCircle(ink, r * k, Offset(cx * k, cy * k))

private fun DrawScope.chevron(k: Float, yTip: Float, yWing: Float, ink: Color) = drawPath(
    Path().apply {
        moveTo(10.8f * k, yWing * k); lineTo(16f * k, yTip * k); lineTo(21.2f * k, yWing * k)
    },
    ink,
    style = Stroke(2.7f * k, cap = StrokeCap.Round, join = StrokeJoin.Round),
)

/** أذكارُ الصباح — نصفُ قرصٍ يطلع من الأفق، وسهمٌ صاعد. */
private fun DrawScope.drawMorning(k: Float, pen: Stroke, ink: Color) {
    line(k, 5f, 26.2f, 27f, 26.2f, pen, ink)
    drawPath(
        Path().apply {
            moveTo(10f * k, 26.2f * k)
            /*  ١٨٠° مبدأً و+١٨٠° مدىً: الزوايا تدور مع عقارب الساعة من
             *  الثالثة، والمحورُ الرأسيُّ نازل — فالمرورُ بـ٢٧٠° يعلو.
             *  وهذا نصفُ القرصِ الأعلى، أي الطالعُ فوق الأفق. */
            arcTo(
                androidx.compose.ui.geometry.Rect(10f * k, 20.2f * k, 22f * k, 32.2f * k),
                180f, 180f, false,
            )
            close()
        },
        ink,
    )
    ray(k, 16f, 26.2f, -90f, 9f, 13.4f, pen, ink)
    ray(k, 16f, 26.2f, -134f, 9f, 13.4f, pen, ink)
    ray(k, 16f, 26.2f, -46f, 9f, 13.4f, pen, ink)
    // السهمُ هو ما يفرّقه عن المساء — وفوقه فراغٌ لا تبلغه الأشعّة
    chevron(k, yTip = 4.2f, yWing = 9.4f, ink = ink)
}

/** أذكارُ المساء — قرصٌ منخفضٌ فوق الأفق، وسهمٌ نازل. */
private fun DrawScope.drawEvening(k: Float, pen: Stroke, ink: Color) {
    line(k, 5f, 26.2f, 27f, 26.2f, pen, ink)
    disc(k, 16f, 20.4f, 4.9f, ink)
    /*  بلا شعاعٍ رأسيّ: كان فيها شعاعٌ إلى ‎−٩٤°‎ ينتهي عند ١٠٫٢ وحدة،
     *  والسهمُ يبدأ عند ٩٫٤ — فالتحما وقُرئا حرفَ «Y» لا شمساً وسهماً. */
    floatArrayOf(-180f, -136f, -44f, 0f).forEach { ray(k, 16f, 20.4f, it, 7.1f, 10.2f, pen, ink) }
    chevron(k, yTip = 9.4f, yWing = 4.2f, ink = ink)
}

/** أذكارُ النوم — هلالٌ وثلاثُ نجوم. */
private fun DrawScope.drawSleep(k: Float, ink: Color) {
    drawPath(crescent(k, 21.8f, 16.9f, 8.2f), ink)
    disc(k, 8.0f, 7.4f, 1.7f, ink)
    disc(k, 12.6f, 12.6f, 1.15f, ink)
    disc(k, 6.2f, 14.8f, 0.95f, ink)
}

/** أذكارُ الصلاة — قوسُ محرابٍ وفيه الشمسُ التي تُوقِّت الخمس. */
private fun DrawScope.drawPrayer(k: Float, pen: Stroke, ink: Color) {
    val arch = Path().apply {
        moveTo(6.4f * k, 29.2f * k)
        lineTo(6.4f * k, 13.4f * k)
        cubicTo(6.4f * k, 7.4f * k, 10.7f * k, 3f * k, 16f * k, 3f * k)
        cubicTo(21.3f * k, 3f * k, 25.6f * k, 7.4f * k, 25.6f * k, 13.4f * k)
        lineTo(25.6f * k, 29.2f * k)
        close()
    }
    drawPath(arch, ink.copy(alpha = 0.26f))
    drawPath(arch, ink.copy(alpha = 0.9f), style = pen)
    disc(k, 16f, 15.4f, 3.5f, ink)
    for (i in 0 until 8) ray(k, 16f, 15.4f, i * 45f, 5.6f, 8.2f, pen, ink)
}

/**
 * أذكارٌ متفرّقة — النجمةُ الثُّمانيّة، وهي زخرفةُ المصحف لا وقتاً.
 *
 * وتُولَّد من ستّةَ عشرَ رأساً بالحساب لا تُكتب نقاطُها: كُتبت أوّلاً
 * بثمانيةِ رؤوسٍ فخرجت نجمةً رباعيّةً لا ثُمانيّة.
 */
private fun DrawScope.drawMisc(k: Float, ink: Color) {
    val outer = 12.6f; val inner = 5.9f
    drawPath(
        Path().apply {
            for (i in 0 until 16) {
                val r = if (i % 2 == 0) outer else inner
                val t = (i * 22.5f - 90f) * PI.toFloat() / 180f
                val x = (16f + r * cos(t)) * k
                val y = (16f + r * sin(t)) * k
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        },
        ink,
    )
}

/**
 * هلالٌ: قرصٌ تامٌّ يقضمه قرصٌ آخرُ، بقاعدة [PathFillType.EvenOdd].
 *
 * والنسبتان ليستا ذوقاً: القرنان لا يبقيان إلّا إذا كان القاضمُ لا يبلغ
 * طرفَي الأصل، أي `biteR < hypot(offset, r)`. وكانتا ‎١٫٢٢‎ و‎٠٫٦٢‎ فصار
 * `٩٫٦٤ < ١٠٫٠٠` كاذباً — فابتُلع القرنان وخرج الشكلُ حلقةً سميكةً لا
 * هلالاً. وهاتان تُبقيان `٩٫٣٦ > ٨٫٨٦`، فله قرنان.
 */
private const val BITE_SCALE  = 1.08f
private const val BITE_OFFSET = 0.55f

private fun crescent(k: Float, cx: Float, cy: Float, r: Float): Path =
    Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(androidx.compose.ui.geometry.Rect((cx - r) * k, (cy - r) * k, (cx + r) * k, (cy + r) * k))
        val br = r * BITE_SCALE
        val bx = cx + r * BITE_OFFSET
        addOval(androidx.compose.ui.geometry.Rect((bx - br) * k, (cy - br) * k, (bx + br) * k, (cy + br) * k))
    }
