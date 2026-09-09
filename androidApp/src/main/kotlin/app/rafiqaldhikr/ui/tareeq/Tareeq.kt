package app.rafiqaldhikr.ui.tareeq

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.sky.SkyWeather
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   الطريق — سماءُ الشاشة الأولى

   صورةٌ واحدةٌ ساكنة، ويتحرّك فيها **الماءُ وحدَه**. وهذا هو الشكلُ
   الذي صكّه بِك وبِرغ سنة ٢٠١١ باسم «السينماغراف»: حركةٌ متكرّرةٌ في
   جزءٍ من الصورة في مقابل سكونِ بقيّتها. وقوّتُه في التضادّ لا في
   الكثرة — ولذلك لا يهتزّ هنا ورقٌ ولا يتمايل سعف.

   **ولا بكسلَ من الصورة يُشوَّه.** جرّبتُ قبلها إزاحةَ البكسلات نفسِها
   فتمطّطت: حين تتحرّك ورقةٌ يجب أن يظهر ما وراءها، ولا شيءَ وراءها في
   صورةٍ ساكنة. فالماءُ هنا **يُعاد بناؤه** لا يُزاح: لوحُ انعكاسٍ
   مقلوبٌ حول خطّ الشاطئ يُبنى مرّةً واحدة، ثمّ يُقطَّع كلَّ إطارٍ
   شرائحَ تُزاح كلُّ واحدةٍ بموجها. الأصلُ يبقى تحته أساساً للّون.

   **ولا قرصَ شمسٍ في المشهد.** ضوءُ الصورة منتشرٌ من سماءٍ غائمة،
   ولذلك لا حزمَ ضوءٍ ولا هالةَ ولا عمودَ بريقٍ حادّ — الصورتان
   السابقتان كانتا مصوَّرتَين عند الشروق، والقرصُ مطبوعٌ في بكسلاتهما
   فلا مفتاحَ يطفئه.

   والطريقُ نفسُه هو البطل: الممرُّ الحجريُّ والسياجُ يصعدان من أسفل
   اليسار نحو الشجر والمئذنة. ولذلك يبدأ الإطارُ من `FRAME_TOP` لا من
   أعلى الصورة — لتقع الكاميرا على الطريق لا على السماء.
══════════════════════════════════════════════════════════════ */

private const val PW = 720          // مقاسُ الصورة الأصليّة
private const val PH = 1280

/** أين تقع الكاميرا في الصورة: أعلى الإطار المرئيّ، نسبةً إلى الارتفاع. */
private const val FRAME_TOP = 0.315f

/*  حافّتا النهر، مقروءتان من الصورة نفسِها لا مخمَّنتَين: العليا خطُّ
 *  الشاطئ البعيد — وحولها يُقلب الانعكاس — والسفلى حافّةُ العشب القريب.
 *  والنهرُ إسفينٌ لا مستطيل: يضيق يساراً ويتّسع يميناً. */
private val SHORE = intArrayOf(
    178, 472, 220, 468, 260, 470, 300, 477, 340, 478, 380, 487, 420, 491,
    460, 495, 500, 500, 540, 515, 580, 538, 620, 540, 660, 538, 719, 535,
)
private val BANK = intArrayOf(
    178, 645, 220, 660, 260, 690, 300, 720, 340, 742, 380, 775, 420, 810,
    460, 845, 500, 880, 540, 902, 580, 936, 620, 976, 660, 1006, 719, 1052,
)
private const val X0 = 178          // أوّلُ عمودٍ فيه ماء — وما قبله جذعُ النخلة
private const val BAND_TOP = 460    // نطاقُ لوح الانعكاس
private const val BAND_BOT = 1060

/**
 * الصورةُ ولوحُ انعكاسِها ومسارُ الماء — تُبنى مرّةً واحدةً وتبقى.
 *
 * بناءُ اللوح يمرّ على نحوِ نصفِ مليون بكسل، وهو ثمنٌ يُدفع مرّةً عند
 * أوّل رسم؛ ولو أُعيد كلَّ إطارٍ لأكل المعالجَ كلَّه.
 */
internal class Plates(ctx: Context) {
    val photo: Bitmap
    val refl: Bitmap
    val water = Path()
    private val shoreY = FloatArray(PW)
    private val bankY = FloatArray(PW)

    init {
        val opts = BitmapFactory.Options().apply {
            inScaled = false
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        photo = BitmapFactory.decodeResource(ctx.resources, R.drawable.tareeq, opts)
        curve(SHORE, shoreY)
        curve(BANK, bankY)

        //  المسار: الشاطئُ ذهاباً وحافّةُ العشب إياباً.
        water.moveTo(X0.toFloat(), shoreY[X0])
        for (x in X0 until PW) water.lineTo(x.toFloat(), shoreY[x])
        for (x in PW - 1 downTo X0) water.lineTo(x.toFloat(), bankY[x])
        water.close()

        //  لوحُ الانعكاس: لكلّ عمودٍ يُقلب ما فوق شاطئِه، مضغوطاً
        //  منظورياً بـ٠٫٨٦ — فما بَعُد يُرى في الماء أقصرَ ممّا هو.
        val src = IntArray(PW * PH)
        photo.getPixels(src, 0, PW, 0, 0, PW, PH)
        val bh = BAND_BOT - BAND_TOP
        val dst = IntArray(PW * bh)                    // شفّافٌ ابتداءً
        for (x in X0 until PW) {
            val top = shoreY[x]
            val bot = min(bankY[x], (BAND_BOT - 1).toFloat())
            var y = max(top, BAND_TOP.toFloat()).toInt()
            while (y <= bot) {
                val ry = (top - (y - top) * 0.86f).toInt().coerceIn(0, PH - 1)
                dst[(y - BAND_TOP) * PW + x] = src[ry * PW + x]
                y++
            }
        }
        refl = Bitmap.createBitmap(dst, PW, bh, Bitmap.Config.ARGB_8888)
    }

    /** منحنىً من نقاطِ ضبطٍ بتنعيمٍ يمنع الزوايا. */
    private fun curve(pts: IntArray, out: FloatArray) {
        var i = 0
        while (i + 3 < pts.size) {
            val x1 = pts[i]; val y1 = pts[i + 1]; val x2 = pts[i + 2]; val y2 = pts[i + 3]
            for (x in x1..min(x2, PW - 1)) {
                val u = (x - x1).toFloat() / (x2 - x1)
                out[x] = y1 + (y2 - y1) * (u * u * (3f - 2f * u))
            }
            i += 2
        }
        for (x in 0 until X0) out[x] = out[X0]
    }
}

/** ما يتغيّر بين إطارٍ وإطار. */
internal class TareeqState(
    val time: Float,
    val sunAlt: Float,
    val weather: SkyWeather,
    val still: Boolean,
)

internal class TareeqRenderer(private val pl: Plates) {
    private val filter = Paint(Paint.FILTER_BITMAP_FLAG)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val src = Rect()
    private val dstR = RectF()

    fun render(c: android.graphics.Canvas, s: TareeqState, viewH: Float) {
        //  نهارٌ ودفء: صفرٌ تحت الشفق المدنيّ، وواحدٌ فوق ستِّ درجات.
        val day = ((s.sunAlt + 6f) / 12f).coerceIn(0f, 1f)
        val warm = (1f - abs(s.sunAlt) / 12f).coerceIn(0f, 1f)
        val t = if (s.still) 6f else s.time
        val wind = (s.weather.windKmh / 34f).coerceIn(0.25f, 1.6f)

        c.drawBitmap(pl.photo, 0f, 0f, filter)
        drawWater(c, t, day, wind)
        if (day > 0.5f && s.weather.rain < 0.35f) birds(c, t, day)
        weatherVeil(c, s, t)
        grade(c, day, warm)
        scrim(c, viewH)
    }

    /* ── الماء: لوحُ الانعكاس شرائحَ ─────────────────────────── */
    private fun drawWater(c: android.graphics.Canvas, t: Float, day: Float, wind: Float) {
        c.save()
        c.clipPath(pl.water)
        val step = 6f
        var y = SHORE_MIN
        while (y < BANK_MAX) {
            //  العمقُ دالّةُ ارتفاعٍ لا دالّةُ ضفّة: ما نزل في الصورة
            //  قرُب من العين، فطالَ موجُه — وهذا وحدَه ما يجعل مسطّحاً
            //  يُقرأ نهراً لا صفيحةً معدنية.
            val dep = ((y - SHORE_MIN) / (BANK_MAX - SHORE_MIN)).coerceIn(0f, 1f)
            val k = (300f - 230f * dep) / PH
            val off = (sin(y * k - t * 1.9f) * 0.45f + sin(y * k * 0.5f + t * 1.25f) * 0.28f) *
                (0.25f + dep * 1.35f) * 11.5f * wind
            //  فرينل: البعيدُ يعكس أكثرَ ممّا يعكس القريب، ثمّ يخفت
            //  الانعكاسُ عند أقربِ الماء فلا يصير مرآةً تحت القدم.
            val fres = (0.46f - 0.33f * dep) *
                (1f - smoothstep(0.70f, 1f, dep) * 0.88f)
            val y0 = (y - BAND_TOP).toInt().coerceIn(0, pl.refl.height - 1)
            val y1 = (y + step - BAND_TOP).toInt().coerceIn(y0 + 1, pl.refl.height)
            src.set(0, y0, PW, y1)
            dstR.set(off, y, PW + off, y + step)
            filter.alpha = (fres * 255f).toInt().coerceIn(0, 255)
            c.drawBitmap(pl.refl, src, dstR, filter)
            y += step
        }
        filter.alpha = 255

        //  بريقٌ باردٌ عريض: ضوءُ غيمٍ لا وخزُ قرص.
        p.style = Paint.Style.STROKE
        for (i in 0 until 11) {
            val yy = SHORE_MIN + 30f + i * ((BANK_MAX - SHORE_MIN - 40f) / 11f)
            val dep = i / 11f
            p.color = Color.argb(((0.05f + 0.09f * dep) * 255f * (0.30f + day)).toInt()
                .coerceIn(0, 255), 0xF2, 0xF8, 0xFA)
            p.strokeWidth = 1f + dep * 2.2f
            val w = Path()
            var wx = 0f
            while (wx <= PW) {
                val wy = yy + sin(wx * 0.021f + t * (0.55f + dep * 1.5f) + i * 1.3f) *
                    (1.1f + dep * 2.4f)
                if (wx == 0f) w.moveTo(wx, wy) else w.lineTo(wx, wy)
                wx += 14f
            }
            c.drawPath(w, p)
        }
        p.style = Paint.Style.FILL
        c.restore()
    }

    /* ── طيرٌ فوق الشجر ─────────────────────────────────────── */
    private fun birds(c: android.graphics.Canvas, t: Float, day: Float) {
        p.style = Paint.Style.STROKE
        p.strokeCap = Paint.Cap.ROUND
        p.color = Color.argb((0.62f * day * 255).toInt().coerceIn(0, 255), 0x1D, 0x25, 0x20)
        //  الفرشاةُ مشتركة، فما يُغيَّر هنا يُردّ في آخر الدالّة.
        for (i in 0 until 7) {
            val sp = 15f + i * 4.5f
            val x = ((t * sp + i * 133f) % (PW + 180f)) - 90f
            val y = 412f + i * 12f + sin(t * 0.6f + i) * 9f
            val sz = 3.0f + (i % 3) * 1.1f
            val f = sin(t * (6f + i * 0.6f) + i) * 0.95f
            p.strokeWidth = max(1.2f, sz * 0.30f)
            val w = Path()
            w.moveTo(x - sz * 1.8f, y - f * sz * 0.9f)
            w.quadTo(x - sz * 0.6f, y + sz * 0.55f, x, y)
            w.quadTo(x + sz * 0.6f, y + sz * 0.55f, x + sz * 1.8f, y - f * sz * 0.9f)
            c.drawPath(w, p)
        }
        p.style = Paint.Style.FILL
        p.strokeCap = Paint.Cap.BUTT
    }

    /* ── ضبابٌ ومطر ─────────────────────────────────────────── */
    private fun weatherVeil(c: android.graphics.Canvas, s: TareeqState, t: Float) {
        val fog = s.weather.fog
        if (fog > 0.04f) {
            //  الضبابُ يجلس على الماء لا على الشجر.
            val a = (fog * 132f).toInt().coerceIn(0, 255)
            p.shader = LinearGradient(
                0f, SHORE_MIN - 40f, 0f, BANK_MAX,
                intArrayOf(Color.argb(0, 0xDC, 0xE4, 0xE6),
                    Color.argb(a, 0xDC, 0xE4, 0xE6),
                    Color.argb(a / 5, 0xDC, 0xE4, 0xE6)),
                floatArrayOf(0f, 0.42f, 1f), Shader.TileMode.CLAMP,
            )
            c.drawRect(0f, SHORE_MIN - 40f, PW.toFloat(), BANK_MAX, p)
            p.shader = null
        }
        val rain = s.weather.rain
        if (rain > 0.04f) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = 1.5f
            p.color = Color.argb((rain * 110f).toInt().coerceIn(0, 255), 0xD7, 0xE4, 0xF4)
            val n = (rain * 150f).toInt().coerceIn(12, 150)
            var i = 0
            while (i < n) {
                val sx = (i * 137 % 860) - 70f
                val sy = ((i * 53 % 700) + t * 700f) % 900f + 380f
                c.drawLine(sx + sy * 0.13f, sy, sx + sy * 0.13f - 4f, sy + 20f, p)
                i++
            }
            p.style = Paint.Style.FILL
        }
    }

    /* ── لونُ الساعة ────────────────────────────────────────────
       ضربُ بورتر-دَف يضرب الشفافيةَ أيضاً في أندرويد، فلا يحتاج قناعاً
       كما يحتاج القماشُ في المتصفّح.

       ومشهدٌ غائمٌ لا تُصبَغ ساعاتُه صبغةَ شروقٍ ذهبيّ: النهارُ محايدٌ
       تقريباً، والدفءُ لمسةٌ لا طبقة. ولو زدتُها ذهبت الخضرةُ وصار
       المشهدُ بُنّيّاً — وهو عينُ ما لا نريد.
    ───────────────────────────────────────────────────────────── */
    private fun grade(c: android.graphics.Canvas, day: Float, warm: Float) {
        var r = lerp(0.34f, 1.02f, day)
        var g = lerp(0.42f, 1.03f, day)
        var b = lerp(0.60f, 1.00f, day)
        val w = warm * 0.55f
        r = lerp(r, 1.07f, w); g = lerp(g, 0.99f, w); b = lerp(b, 0.88f, w)
        c.drawColor(
            Color.argb(255, ch(r), ch(g), ch(b)),
            PorterDuff.Mode.MULTIPLY,
        )
    }

    /* ── حجابُ الكلام ───────────────────────────────────────────
       الكلامُ يقع فوق شجرٍ داكنٍ تارةً وفوق ماءٍ فاتحٍ تارة، فلا لونَ
       حبرٍ واحدٍ يكفي وحدَه. تدرُّجٌ خافتٌ من الأعلى يضمن التباينَ في
       كلّ ساعةٍ ويعطي المشهدَ عمقَه السينمائيّ في آنٍ واحد.
    ───────────────────────────────────────────────────────────── */
    private fun scrim(c: android.graphics.Canvas, viewH: Float) {
        p.shader = LinearGradient(
            0f, FRAME_TOP * PH, 0f, FRAME_TOP * PH + viewH * 0.62f,
            intArrayOf(Color.argb(92, 8, 18, 14), Color.argb(36, 8, 18, 14), Color.TRANSPARENT),
            floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, FRAME_TOP * PH, PW.toFloat(), FRAME_TOP * PH + viewH * 0.62f, p)
        p.shader = null
    }

    private companion object {
        val SHORE_MIN = 468f
        val BANK_MAX = 1052f
    }
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun ch(v: Float) = (v * 255f).toInt().coerceIn(0, 255)
private fun smoothstep(e0: Float, e1: Float, x: Float): Float {
    val t = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/** يحمل الألواحَ عبر إعادة التركيب — و`remember` لا يُستدعى داخل الرسم. */
internal class Holder {
    private var r: TareeqRenderer? = null
    fun get(ctx: Context): TareeqRenderer =
        r ?: TareeqRenderer(Plates(ctx)).also { r = it }
}

/**
 * سماءُ الشاشة الرئيسية: الطريقُ على النهر.
 *
 * صورةٌ واحدةٌ في التطبيق — لا فيديو ولا مكتبةَ ثلاثيّة الأبعاد — وكلُّ
 * حركةٍ تُحسب فوقها. يعمل دون اتّصال تماماً؛ الطقسُ والريحُ وحدَهما
 * يأتيان من الشبكة، ولهما محفوظٌ يُقرأ فوراً.
 */
@Composable
fun Tareeq(
    sunAlt: Double,
    reducedMotion: Boolean,
    weather: SkyWeather = SkyWeather(),
    fade: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) { time = 6f; return@LaunchedEffect }
        val t0 = withFrameMillis { it }
        while (true) {
            withFrameMillis { ms -> time = (ms - t0) / 1000f }
            //  ستٌّ وعشرون صورةً في الثانية: الماءُ لا يحتاج أكثر،
            //  والبطاريةُ تحتاج أقلّ.
            delay(38)
        }
    }
    val ctx = LocalContext.current
    val holder = remember { Holder() }
    val fadeArgb = fade.toArgb()
    Canvas(modifier) {
        val pxW = size.width
        val pxH = size.height
        if (pxW < 8f || pxH < 8f) return@Canvas
        val r = holder.get(ctx)
        drawIntoCanvas { c ->
            val nc = c.nativeCanvas
            val sc = pxW / PW                       // العرضُ يملأ، والارتفاعُ يُقصّ
            val viewH = pxH / sc
            nc.save()
            nc.clipRect(0f, 0f, pxW, pxH)
            nc.scale(sc, sc)
            nc.translate(0f, -FRAME_TOP * PH)
            r.render(nc, TareeqState(
                time = time,
                sunAlt = sunAlt.toFloat(),
                weather = weather,
                still = reducedMotion,
            ), viewH)
            nc.restore()
            //  ذوبانُ الأسفل في الورقة: فلا يبقى قطعٌ حادٌّ بين
            //  الصورة وما تحتها.
            if (fadeArgb != 0) {
                val fp = Paint()
                fp.shader = LinearGradient(
                    0f, pxH * 0.80f, 0f, pxH,
                    intArrayOf(fadeArgb and 0x00FFFFFF, fadeArgb),
                    null, Shader.TileMode.CLAMP,
                )
                nc.drawRect(0f, pxH * 0.80f, pxW, pxH, fp)
            }
        }
    }
}
