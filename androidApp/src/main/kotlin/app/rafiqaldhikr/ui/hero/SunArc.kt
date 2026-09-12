package app.rafiqaldhikr.ui.hero

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiq.domain.model.PrayerTimesResult
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.formatClock
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   قوسُ الشمس — حبّةُ الميقات

   كلُّ ما جُرّب قبله كان **قالباً**: حبّةٌ أو حلقةٌ أو شريطُ تقدّم. وهي
   أشكالٌ تُستعار من أيّ تطبيقٍ ولا تقول شيئاً عن الصلاة.

   والحقيقةُ التي تُغني عن الزخرفة كلِّها: **مواقيتُ الصلاة ليست أرقاماً،
   هي مواضعُ الشمس.** الفجرُ قبل طلوعها، والظهرُ زوالُها، والعصرُ حين
   يصير ظلُّ الشيء مثلَه، والمغربُ غروبُها، والعشاءُ بعد مغيب الشفق.

   فإذا رُسم مسارُ الشمس، رُسمت المواقيتُ نفسُها. وثلاثةُ أشياءَ تخرج
   **لأنّها صحيحةٌ لا لأنّها زُخرفت**:

     • الظهرُ يقع في قمّة القوس — لأنّه زوالُها.
     • المغربُ يقع على خطّ الأفق — لأنّه غروبُها.
     • الفجرُ والعشاءُ تحت الخطّ — لأنّهما وهي غائبة، فيصير القرصُ هلالاً.

   والقرصُ الذي يمشي هنا **هو نفسُه** الضوءُ الذي يزحف على الخلفيّة في
   `HeroBackdrop`: شمسٌ واحدةٌ في موضعين.

   **ولا يُرسم القوسُ بمواقيتَ مخترعة.** بلا موقعٍ لا مواقيتَ، وبلا
   مواقيتَ لا قوس — كما لا يُرسم شريطُ `Meeqat` حين `resolved = false`.
══════════════════════════════════════════════════════════════ */

private class Mark(@StringRes val label: Int, val at: Long)

/*  قياساتُ النموذج المعتمَد — صريحةٌ لا كسورٌ من ارتفاعٍ متغيّر:
 *  القوسُ شكلٌ محسوبٌ لا تخطيطٌ يتمدّد. */
/*  والهوامشُ محسوبةٌ على **الهالة** لا على الخطّ: نصفُ قطرها ٢٦
 *  نهاراً، فإن ضاق اللوحُ عنها قُصَّت الشمسُ نصفَ قرصٍ عند الشروق
 *  والغروب وعند الزوال. فالسقفُ `cy - ryDay - 26 ≥ 0` والأرضُ
 *  `cy + ryNight + 15 ≤ h` والجانبُ `pad ≥ 26`. */
private val ARC_H = 108.dp      // ارتفاعُ اللوح
private val ARC_CY = 76.dp      // خطُّ الأفق داخله
private val ARC_PAD = 28.dp     // هامشُ طرفَي القوس — يسَعُ الهالة
private val ARC_RY_DAY = 50.dp  // ارتفاعُ قوس النهار
private val ARC_RY_NIGHT = 17.dp // وقوسُ الليل أخفضُ: مسارٌ لا يُرى

/**
 * @param times مواقيتُ اليوم الحقيقيّة، أو `null` فلا يُرسم شيء.
 * @param left  ما بقي للميقات التالي بصيغةٍ مقروءة، من مُنسِّق الشاشة
 *              نفسِه — **لا يُحسب هنا ثانيةً**، فصيغةُ المدّة في التطبيق
 *              واحدة.
 * @param nowMs اللحظة — تُمرَّر لا تُقرأ، فتتحرّك مع نبضة الشاشة.
 * @param accent لونُ علامة الميقات التالي. **لا يكون داكناً**: القوسُ
 *        يقع على خلفيّةٍ تُظلم ليلاً، والأخضرُ الداكنُ يختفي فيها.
 */
@Composable
fun SunArc(
    times: PrayerTimesResult?,
    nowMs: Long,
    left: String?,
    ink: Color,
    accent: Color,
    ar: Boolean,
    modifier: Modifier = Modifier,
) {
    times ?: return
    val marks = listOf(
        Mark(R.string.fajr, times.fajr),
        Mark(R.string.sunrise, times.sunrise),
        Mark(R.string.dhuhr, times.dhuhr),
        Mark(R.string.asr, times.asr),
        Mark(R.string.maghrib, times.maghrib),
        Mark(R.string.isha, times.isha),
    )
    //  الميقاتُ التالي: أوّلُ ما لم يأتِ بعد، وإلّا فجرُ الغد.
    val next = marks.firstOrNull { it.at > nowMs } ?: marks.first()

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(ARC_H)) {
            drawSunPath(marks, next, nowMs, times, ink, accent)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            val name = stringResource(next.label)
            androidx.compose.material3.Text(
                //  الاسمُ وحدَه ثقيل: «التالي» كلمةُ ربطٍ لا خبر.
                emphasize(stringResource(R.string.meeqat_next, name), name),
                style = RafiqType.body, color = ink, maxLines = 1,
            )
            androidx.compose.material3.Text(
                buildString {
                    append(formatClock(next.at, ar))
                    if (left != null) append(" · ").append(stringResource(R.string.meeqat_in, left))
                },
                style = RafiqType.bodyS, color = ink.copy(alpha = 0.78f), maxLines = 1,
            )
        }
    }
}

/** يُثقّل [part] داخل [whole] — بالبحث لا بالقصّ، فيصحّ في اللغتين. */
private fun emphasize(whole: String, part: String): AnnotatedString {
    val i = whole.indexOf(part)
    if (i < 0) return AnnotatedString(whole)
    return buildAnnotatedString {
        append(whole)
        addStyle(SpanStyle(fontWeight = FontWeight.SemiBold), i, i + part.length)
    }
}

/** ساعةُ اليوم كسراً — من أجل زاوية الشمس. */
private fun hourOf(ms: Long): Float {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60f
}

/**
 * زاويةُ الشمس: صفرٌ عند الشروق على الأفق يميناً، و`π` عند الغروب
 * يساراً، وما بينهما فوق الخطّ. وما بعد `π` ليلٌ تحت الخطّ.
 *
 * والشرقُ يميناً عمداً: الزمنُ يجري كما يجري الخطُّ العربيّ.
 */
private fun theta(h: Float, sunrise: Float, sunset: Float): Float {
    if (h in sunrise..sunset) return (PI * (h - sunrise) / (sunset - sunrise)).toFloat()
    val nightLen = 24f - (sunset - sunrise)
    val q = if (h > sunset) h - sunset else h + 24f - sunset
    return (PI + PI * (q / nightLen)).toFloat()
}

private fun DrawScope.drawSunPath(
    marks: List<Mark>,
    next: Mark,
    nowMs: Long,
    t: PrayerTimesResult,
    ink: Color,
    accent: Color,
) {
    val sunrise = hourOf(t.sunrise)
    val sunset = hourOf(t.maghrib)
    if (sunset <= sunrise) return                 // مدارٌ قطبيٌّ شاذّ: لا يُرسم

    val cx = size.width / 2f
    val cy = ARC_CY.toPx()
    val rx = size.width / 2f - ARC_PAD.toPx()
    val ryDay = ARC_RY_DAY.toPx()
    val ryNight = ARC_RY_NIGHT.toPx()
    fun at(th: Float): Offset {
        val s = sin(th)
        return Offset(cx + rx * cos(th), cy - s * (if (s > 0f) ryDay else ryNight))
    }
    fun arc(a: Float, b: Float, steps: Int = 72): Path = Path().apply {
        for (i in 0..steps) {
            val p = at(a + (b - a) * i / steps)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
    }

    //  خطُّ الأفق
    drawLine(ink.copy(alpha = 0.22f), Offset(6.dp.toPx(), cy), Offset(size.width - 6.dp.toPx(), cy), 1.dp.toPx())
    //  ما تحته منقّطٌ خافت: الشمسُ غائبة
    drawPath(
        arc(PI.toFloat(), (2 * PI).toFloat()),
        ink.copy(alpha = 0.20f),
        style = Stroke(
            width = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx())),
        ),
    )
    //  وما فوقه خافتٌ لِما لم يأتِ بعد
    drawPath(arc(0f, PI.toFloat()), ink.copy(alpha = 0.26f), style = Stroke(1.6.dp.toPx()))

    //  وما مضى من النهار صُلب — فيُقرأ التقدّمُ بلا عدّادٍ ولا نسبةٍ مكتوبة
    val now = theta(hourOf(nowMs), sunrise, sunset)
    if (now > 0f && now < PI.toFloat()) {
        drawPath(
            arc(0f, now, 60),
            ink.copy(alpha = 0.92f),
            style = Stroke(2.2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }

    //  كلُّ ميقاتٍ في موضع شمسه لا في موضعٍ مختار
    for (m in marks) {
        val p = at(theta(hourOf(m.at), sunrise, sunset))
        val isNext = m === next
        drawCircle(
            color = if (isNext) accent else ink.copy(alpha = if (m.at <= nowMs) 0.70f else 0.34f),
            radius = if (isNext) 4.6.dp.toPx() else 2.6.dp.toPx(),
            center = p,
        )
    }

    //  الشمس — أو الهلالُ إن كانت تحت الأفق
    val s = at(now)
    val up = now > 0f && now < PI.toFloat()
    val halo = if (up) 26.dp.toPx() else 15.dp.toPx()
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            0f to (if (up) Color(0xFFFFECB2) else Color(0xFFCEE0FF)).copy(alpha = if (up) 0.55f else 0.34f),
            1f to Color.Transparent,
            center = s, radius = halo,
        ),
        radius = halo, center = s,
    )
    val disc = if (up) Color(0xFFFFF1CB) else Color(0xFFD9E4F2)
    if (up) {
        drawCircle(disc, 6.4.dp.toPx(), s)
    } else {
        //  هلالٌ بفرق دائرتين — لا قصٌّ بطبقةٍ ولا لونُ خلفيّةٍ مزيّف
        val r = 4.6.dp.toPx()
        val full = Path().apply { addOval(Rect(Offset(s.x - r, s.y - r), Size(r * 2, r * 2))) }
        val bite = Path().apply {
            val o = Offset(s.x + r * 0.56f, s.y - r * 0.34f)
            addOval(Rect(Offset(o.x - r * 0.96f, o.y - r * 0.96f), Size(r * 1.92f, r * 1.92f)))
        }
        drawPath(Path().apply { op(full, bite, PathOperation.Difference) }, disc)
    }

    /*  حلقةُ القادم **آخرَ ما يُرسم**.
     *
     *  بعد المغرب بقليلٍ يقع القرصُ والعشاءُ في البقعة نفسِها — وهو
     *  صحيحٌ لا خطأ — فكانت الحلقةُ تُدفَن تحت القرص فتضيع العلامة.
     *  وهي فوقه الآن فتُطوّقه: «الشمسُ عند ميقاتِك القادم». */
    drawCircle(
        accent.copy(alpha = 0.55f),
        9.dp.toPx(),
        at(theta(hourOf(next.at), sunrise, sunset)),
        style = Stroke(1.4.dp.toPx()),
    )
}

/**
 * القوسُ **قبل أن يُعرف الموقع**: أفقٌ وقوسان بلا علامةٍ ولا شمس.
 *
 * البديلُ كان حبّةً زجاجيّةً مكتوباً فيها «مواقيتُك لم تُضبط بعد» —
 * شكلٌ من تصميمٍ آخرَ يظهر في أوّل ما يفتحه صاحبُ الهاتف، فيرى شاشةً
 * لا تشبه ما بعدها. والإطارُ الفارغ يقول الشيءَ نفسَه بلغة الشاشة:
 * **المكانُ محجوزٌ ليومك، وينقصه موقعُك**.
 *
 * ولا وقتَ يُخترع هنا: لا علامةَ ولا قرصَ ولا خطَّ تقدُّم.
 */
@Composable
fun SunArcEmpty(
    ink: Color,
    note: String,
    cta: String,
    accent: Color,
    onSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(ARC_H)) {
            val cx = size.width / 2f
            val cy = ARC_CY.toPx()
            val rx = size.width / 2f - ARC_PAD.toPx()
            fun at(th: Float): Offset {
                val s = sin(th)
                return Offset(
                    cx + rx * cos(th),
                    cy - s * (if (s > 0f) ARC_RY_DAY.toPx() else ARC_RY_NIGHT.toPx()),
                )
            }
            fun arc(a: Float, b: Float): Path = Path().apply {
                for (i in 0..72) {
                    val p = at(a + (b - a) * i / 72)
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
            }
            drawLine(
                ink.copy(alpha = 0.18f),
                Offset(6.dp.toPx(), cy), Offset(size.width - 6.dp.toPx(), cy),
                1.dp.toPx(),
            )
            drawPath(
                arc(PI.toFloat(), (2 * PI).toFloat()),
                ink.copy(alpha = 0.14f),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(2.dp.toPx(), 4.dp.toPx()),
                    ),
                ),
            )
            drawPath(arc(0f, PI.toFloat()), ink.copy(alpha = 0.18f), style = Stroke(1.6.dp.toPx()))
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            androidx.compose.material3.Text(
                note, style = RafiqType.bodyS, color = ink.copy(alpha = 0.78f), maxLines = 1,
            )
            androidx.compose.material3.Text(
                cta,
                style = RafiqType.body,
                color = accent,
                maxLines = 1,
                //  مساحةُ اللمس ٤٨ نقطةً حدّاً أدنى مهما صغر الحرف.
                modifier = Modifier
                    .clickable(onClick = onSet)
                    .padding(horizontal = 10.dp, vertical = 13.dp),
            )
        }
    }
}
