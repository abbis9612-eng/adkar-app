package app.rafiqaldhikr.ui.mushaf

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * أيقوناتُ ورقة الآية — ستٌّ لكلٍّ هيئةٌ لا تُشبه أختَها
 * ═══════════════════════════════════════════════════
 *
 * ═══ ولماذا لم تُؤخذ من [RIcon] ═══
 *
 * كانت الستُّ من المجموعة العامّة: خطٌّ واحدٌ وسمكٌ واحدٌ وحجمٌ واحد.
 * فتتشابه صامتةً — لا تُميَّز بالنظرة، ويُقرأ الاسمُ تحتها ليُعرف الزرّ.
 * وزرٌّ يحتاج اسمَه ليُعرف، رمزُه زينة.
 *
 * فصار لكلٍّ **ظلٌّ مختلف**: شريطٌ طويلٌ · ورقتان مربّعتان · قلمٌ مائل ·
 * خطٌّ رفيعٌ قائم · سهمٌ خارجٌ من صندوق · قرصٌ على قوس. تُعرف بالهيئة
 * قبل أن تُقرأ.
 *
 * ═══ والذهبُ يربطها ═══
 *
 * في كلٍّ **تفصيلةٌ ذهبيّةٌ واحدةٌ مملوءة** — عقدةُ الشريط، طيّةُ الورقة،
 * سنُّ القلم، معينُ الوقف، رأسُ السهم، رأسُ المِجهر. فتُقرأ عائلةً واحدة
 * لا ستَّ أيقوناتٍ من ستّة مصادر. وواحدةٌ لا أكثر: ذهبٌ في كلّ خطٍّ لا
 * يُشير إلى شيء.
 *
 * ═══ ولماذا Canvas لا ملفَّ رسمٍ في `res/drawable` ═══
 *
 * في التطبيق نظاما رموزٍ قائمان: [RIcon] ملفّاتُ رسمٍ تُلوَّن بلونٍ واحد
 * (`tint`)، و`IslamicIcons.kt` رسمٌ على Canvas لما يحتاج لونين فأكثر.
 * وهذه ذاتُ لونين — حبرٌ وذهب — فهي من الثاني. ولو رُسمت ملفّاً لثُبّت
 * الذهبُ في XML فلا ينقلب مع الوضعين.
 */

/** سمكُ القلم ونهاياتُه — واحدةٌ في الستّ، وإلّا لم تُقرأ عائلة. */
private fun DrawScope.pen(w: Float) =
    Stroke(width = w * 0.071f, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
private fun Glyph(size: Dp, draw: DrawScope.(Float) -> Unit) {
    Box(Modifier.size(size)) {
        Canvas(Modifier.size(size)) { draw(this.size.width) }
    }
}

/** علامة — شريطٌ طويلٌ بذيلٍ مشقوق، وعقدتُه ذهبيّة. */
@Composable
fun IcMark(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    val p = Path().apply {
        moveTo(w * .25f, w * .125f); lineTo(w * .75f, w * .125f)
        lineTo(w * .75f, w * .875f); lineTo(w * .50f, w * .685f)
        lineTo(w * .25f, w * .875f); close()
    }
    drawPath(p, ink, style = pen(w))
    drawCircle(gold, w * .071f, Offset(w * .50f, w * .358f))
}

/** نسخ — ورقتان مربّعتان، وزاويةُ العليا مطويّةٌ ذهباً. */
@Composable
fun IcCopy(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    val sheet = Path().apply {
        moveTo(w * .167f, w * .175f); lineTo(w * .558f, w * .175f)
        lineTo(w * .708f, w * .325f); lineTo(w * .708f, w * .675f)
        lineTo(w * .167f, w * .675f); close()
    }
    drawPath(sheet, ink, style = pen(w))
    //  الطيّةُ مملوءة — هي التفصيلةُ الذهبيّة، وهي أيضاً ما يقول «ورقة»
    drawPath(
        Path().apply {
            moveTo(w * .558f, w * .175f); lineTo(w * .708f, w * .325f)
            lineTo(w * .558f, w * .325f); close()
        },
        gold,
    )
    val behind = Path().apply {
        moveTo(w * .317f, w * .825f); lineTo(w * .717f, w * .825f)
        cubicTo(w * .777f, w * .825f, w * .825f, w * .777f, w * .825f, w * .717f)
        lineTo(w * .825f, w * .392f)
    }
    drawPath(behind, ink, style = pen(w))
}

/** ملاحظة — قلمٌ مائل، وسِنُّه ذهبيّة. */
@Composable
fun IcNote(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    val body = Path().apply {
        moveTo(w * .650f, w * .142f); lineTo(w * .858f, w * .350f)
        lineTo(w * .388f, w * .821f); lineTo(w * .150f, w * .875f)
        lineTo(w * .204f, w * .638f); close()
    }
    drawPath(body, ink, style = pen(w))
    drawPath(
        Path().apply {
            moveTo(w * .204f, w * .638f); lineTo(w * .150f, w * .875f)
            lineTo(w * .388f, w * .821f); close()
        },
        gold,
    )
    drawLine(
        ink, Offset(w * .567f, w * .225f), Offset(w * .775f, w * .433f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
}

/** فاصل — خطُّ وقفٍ رفيعٌ عليه معينٌ ذهبيّ، كعلامات الوقف في المصحف. */
@Composable
fun IcStop(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    drawLine(
        ink, Offset(w * .5f, w * .125f), Offset(w * .5f, w * .875f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
    drawLine(
        ink, Offset(w * .275f, w * .192f), Offset(w * .725f, w * .192f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
    drawPath(
        Path().apply {
            moveTo(w * .5f, w * .342f); lineTo(w * .629f, w * .5f)
            lineTo(w * .5f, w * .658f); lineTo(w * .371f, w * .5f); close()
        },
        gold,
    )
}

/** مشاركة — سهمٌ يخرج من صندوقٍ مفتوح، ورأسُه ذهبيّ. */
@Composable
fun IcSend(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    drawLine(
        ink, Offset(w * .5f, w * .633f), Offset(w * .5f, w * .150f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
    drawPath(
        Path().apply {
            moveTo(w * .5f, w * .108f); lineTo(w * .671f, w * .287f)
            lineTo(w * .329f, w * .287f); close()
        },
        gold,
    )
    val box = Path().apply {
        moveTo(w * .192f, w * .517f); lineTo(w * .192f, w * .767f)
        cubicTo(w * .192f, w * .822f, w * .237f, w * .867f, w * .292f, w * .867f)
        lineTo(w * .708f, w * .867f)
        cubicTo(w * .763f, w * .867f, w * .808f, w * .822f, w * .808f, w * .767f)
        lineTo(w * .808f, w * .517f)
    }
    drawPath(box, ink, style = pen(w))
}

/** تسميع — مِجهرٌ مستديرٌ بقوسٍ تحته، ورأسُه ذهبيّ. */
@Composable
fun IcMic(size: Dp = 17.dp, ink: Color, gold: Color) = Glyph(size) { w ->
    val head = Path().apply {
        moveTo(w * .375f, w * .233f)
        cubicTo(w * .375f, w * .164f, w * .431f, w * .108f, w * .5f, w * .108f)
        cubicTo(w * .569f, w * .108f, w * .625f, w * .164f, w * .625f, w * .233f)
        lineTo(w * .625f, w * .442f)
        cubicTo(w * .625f, w * .511f, w * .569f, w * .567f, w * .5f, w * .567f)
        cubicTo(w * .431f, w * .567f, w * .375f, w * .511f, w * .375f, w * .442f)
        close()
    }
    drawPath(head, gold)
    val arc = Path().apply {
        moveTo(w * .225f, w * .442f)
        cubicTo(w * .225f, w * .594f, w * .348f, w * .717f, w * .5f, w * .717f)
        cubicTo(w * .652f, w * .717f, w * .775f, w * .594f, w * .775f, w * .442f)
    }
    drawPath(arc, ink, style = pen(w))
    drawLine(
        ink, Offset(w * .5f, w * .717f), Offset(w * .5f, w * .875f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
    drawLine(
        ink, Offset(w * .358f, w * .875f), Offset(w * .642f, w * .875f),
        strokeWidth = w * .071f, cap = StrokeCap.Round,
    )
}

/** تمّ — صحٌّ بنفس القلم، فلا يختلط نظاما رموزٍ في صفٍّ واحد. */
@Composable
fun IcCheck(size: Dp = 17.dp, ink: Color) = Glyph(size) { w ->
    drawPath(
        Path().apply {
            moveTo(w * .167f, w * .521f); lineTo(w * .375f, w * .729f)
            lineTo(w * .833f, w * .271f)
        },
        ink, style = pen(w),
    )
}

/** أغلق — ضربتان متقاطعتان، بنفس القلم كذلك. */
@Composable
fun IcClose(size: Dp = 17.dp, ink: Color) = Glyph(size) { w ->
    drawLine(ink, Offset(w * .225f, w * .225f), Offset(w * .775f, w * .775f),
        strokeWidth = w * .071f, cap = StrokeCap.Round)
    drawLine(ink, Offset(w * .775f, w * .225f), Offset(w * .225f, w * .775f),
        strokeWidth = w * .071f, cap = StrokeCap.Round)
}

/**
 * وردةُ الآية — علامةُ آخرِ الآية في المصحف المطبوع، تحمل رقمَها.
 *
 * اثنتا عشرة ورقةً بنصفَي قطرٍ متناوبَين حول قرصٍ ذهبيٍّ خفيف — فتُقرأ
 * وردةً لا تِرساً. والرقمُ يُرسم فوقها نصّاً، لا هنا.
 */
@Composable
fun AyahRosette(size: Dp, gold: Color) = Glyph(size) { w ->
    for (i in 0 until 12) {
        val a = i * 30f * kotlin.math.PI.toFloat() / 180f
        val r = (if (i % 2 == 0) 0.47f else 0.40f) * w
        drawCircle(
            gold.copy(alpha = if (i % 2 == 0) 0.50f else 0.28f),
            radius = w * 0.05f,
            center = Offset(w / 2f + r * kotlin.math.cos(a), w / 2f + r * kotlin.math.sin(a)),
        )
    }
    drawCircle(gold.copy(alpha = 0.09f), w * 0.33f, Offset(w / 2f, w / 2f))
    drawCircle(
        gold.copy(alpha = 0.50f), w * 0.33f, Offset(w / 2f, w / 2f),
        style = Stroke(width = w * 0.045f),
    )
}
