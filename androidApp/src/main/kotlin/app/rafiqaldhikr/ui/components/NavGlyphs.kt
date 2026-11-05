package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * رموزُ الشريط السفليّ — ⌂ ▤ ◌ ♡ ☰
 * ═════════════════════════════════
 *
 * النموذجُ يكتبها **محارفَ نصّ**، لأنّ صفحةَ HTML تتّكل على خطوط
 * النظام. وجُرد خطوطُ التطبيق الأربعةَ عشرَ فوُجد أنّ **رمزاً واحداً
 * منها فقط** — `◌` U+25CC — مغطّى؛ والأربعةُ الباقية `⌂` U+2302
 * و`▤` U+25A4 و`♡` U+2661 و`☰` U+2630 ليست في خطٍّ مشحون. فكتابتُها
 * حروفاً تجعلها **مربّعاتٍ فارغة** على أيّ جهازٍ ينقصه خطُّ الرموز.
 *
 * فرُسمت أشكالاً متّجهة: **نفسُ الشكل** كما في النموذج، ويُرسم على
 * كلّ جهازٍ بلا اتّكالٍ على خط.
 *
 * والمربّعُ المرجعيُّ ١٦×١٦ كما يرسم المتصفّحُ محرفاً بمقاس ١٠٫٥،
 * والخطُّ شعرةٌ واحدةٌ في كلّ الرموز ليقرأها الطرفُ سواءً.
 */

/** مربّعٌ مرجعيٌّ ١٦×١٦ يُمطّ إلى مقاس العنصر. */
private inline fun DrawScope.on16(crossinline body: DrawScope.() -> Unit) {
    val k = size.minDimension / 16f
    withTransform({ scale(k, k, Offset.Zero) }) { body() }
}

private fun stroke(w: Float = 1.25f) =
    Stroke(width = w, cap = StrokeCap.Round, join = StrokeJoin.Round)

/** `⌂` U+2302 — سقفٌ مثلّثٌ فوق قائمٍ مفتوحِ القاعدة. «اليوم». */
@Composable
fun GlyphHouse(s: Dp = 12.dp, c: Color) = Canvas(Modifier.size(s)) {
    on16 {
        val p = Path().apply {
            moveTo(2f, 8f); lineTo(8f, 2.6f); lineTo(14f, 8f)     // السقف
            moveTo(3.6f, 7.4f); lineTo(3.6f, 13.6f)               // القائمُ الأيمن
            moveTo(12.4f, 7.4f); lineTo(12.4f, 13.6f)             // القائمُ الأيسر
            moveTo(3.6f, 13.6f); lineTo(12.4f, 13.6f)             // القاعدة
        }
        drawPath(p, c, style = stroke())
    }
}

/** `▤` U+25A4 — مربّعٌ بثلاثة سطورٍ أفقيّة. «المصحف». */
@Composable
fun GlyphLines(s: Dp = 12.dp, c: Color) = Canvas(Modifier.size(s)) {
    on16 {
        drawRect(c, Offset(2.5f, 2.5f), Size(11f, 11f), style = stroke())
        for (y in listOf(5.8f, 8f, 10.2f)) {
            drawLine(c, Offset(4.6f, y), Offset(11.4f, y), strokeWidth = 1.1f, cap = StrokeCap.Round)
        }
    }
}

/** `◌` U+25CC — حلقةٌ منقّطة. «الذِّكر»: حبّاتُ التسبيح على خيطها. */
@Composable
fun GlyphDotted(s: Dp = 12.dp, c: Color) = Canvas(Modifier.size(s)) {
    on16 {
        val r = 5.4f
        repeat(12) { i ->
            val a = i * (2.0 * Math.PI / 12.0)
            drawCircle(
                c, 0.78f,
                Offset(8f + (r * kotlin.math.cos(a)).toFloat(), 8f + (r * kotlin.math.sin(a)).toFloat()),
            )
        }
    }
}

/** `♡` U+2661 — قلبٌ مفرَّغ. «الدعاء». */
@Composable
fun GlyphHeart(s: Dp = 12.dp, c: Color) = Canvas(Modifier.size(s)) {
    on16 {
        val p = Path().apply {
            moveTo(8f, 13.4f)
            cubicTo(8f, 13.4f, 2.2f, 9.9f, 2.2f, 6.2f)
            cubicTo(2.2f, 4.1f, 3.9f, 2.8f, 5.6f, 2.8f)
            cubicTo(6.9f, 2.8f, 7.7f, 3.6f, 8f, 4.2f)
            cubicTo(8.3f, 3.6f, 9.1f, 2.8f, 10.4f, 2.8f)
            cubicTo(12.1f, 2.8f, 13.8f, 4.1f, 13.8f, 6.2f)
            cubicTo(13.8f, 9.9f, 8f, 13.4f, 8f, 13.4f)
            close()
        }
        drawPath(p, c, style = stroke())
    }
}

/** `☰` U+2630 — ثلاثةُ سطورٍ متساوية. «أوراقي». */
@Composable
fun GlyphTrigram(s: Dp = 12.dp, c: Color) = Canvas(Modifier.size(s)) {
    on16 {
        for (y in listOf(4.4f, 8f, 11.6f)) {
            drawLine(c, Offset(2.6f, y), Offset(13.4f, y), strokeWidth = 1.35f, cap = StrokeCap.Round)
        }
    }
}
