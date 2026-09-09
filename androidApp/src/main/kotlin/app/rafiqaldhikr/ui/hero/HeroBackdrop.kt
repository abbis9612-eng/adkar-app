package app.rafiqaldhikr.ui.hero

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
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
import app.rafiqaldhikr.ui.sky.SkyWeather
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   خلفيّةُ الشاشة الأولى — تدرُّجٌ يتبع الساعة

   مرّت هذه الرقعةُ بأربعة أطوار: تدرُّجٌ رماديٌّ يقطعه شريطٌ داكن، ثمّ
   سماءٌ تُحسب على المعالج الرسوميّ، ثمّ بستانٌ مرسوم، ثمّ **صورةُ نهرٍ
   فوتوغرافيّة**. وسقطت الصورةُ لسببين قِيسا لا خُمِّنا:

     • **التباين.** الصورةُ لا تعطي خلف السطر الكبير إلّا ٦:١ بعد حجابٍ
       ثقيلٍ يُطفئ جمالَها، والتدرُّجُ يعطي **١٢:١ فأكثر** بحجابٍ خفيف.
     • **الوزن.** كانت ٣٠٨ كيلوبايت تُقصّ على كلّ مقاسِ شاشة. وهذا
       الملفُّ **سطورُ حساب**: يُرسم بدقّة أيّ شاشةٍ مهما كبرت، ويتبع
       ألوانَ الساعة بلا ملفٍّ ثانٍ.

   وما بقي من المشهد الحيّ حركةٌ واحدةٌ صادقة: **الضوءُ يزحف مع الساعة**
   من الشرق إلى الغرب كما يزحف ضوءٌ حقيقيّ، ونبضةٌ بطيئةٌ لا تكاد تُرى.
   ولا شيءَ يدور بلا توقّف — فما يتحرّك دائماً يُقرأ إعلاناً فيُهمَل.

   والبطاقةُ الواردةُ من الشبكة (`HeroCard`) تستطيع أن تضع **صورتَها**
   مكانَ التدرّج متى شاء صاحبُ التطبيق؛ وحينها تُرسم الصورةُ ملءَ
   الإطار ويبقى الحجابُ فوقها.
══════════════════════════════════════════════════════════════ */

private const val VW = 400f          // فضاءُ الرسم — يُمدَّد إلى عرض الشاشة

/** لونا التدرُّج عند ساعاتٍ مفصليّة، وبينها يُمزَج. */
private val PAL = arrayOf(
    floatArrayOf(0f, 10f, 22f, 32f, 4f, 10f, 16f),      // ليل
    floatArrayOf(5.2f, 27f, 43f, 51f, 10f, 22f, 31f),   // فجر
    floatArrayOf(7f, 44f, 58f, 46f, 19f, 32f, 25f),     // شروق
    floatArrayOf(12f, 23f, 64f, 47f, 11f, 35f, 26f),    // نهار
    floatArrayOf(17.5f, 46f, 58f, 42f, 20f, 31f, 23f),  // عصر
    floatArrayOf(19.4f, 42f, 42f, 51f, 16f, 16f, 24f),  // مغرب
    floatArrayOf(24f, 10f, 22f, 32f, 4f, 10f, 16f),
)

internal class Backdrop {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dst = RectF()

    fun render(
        c: android.graphics.Canvas,
        w: Float, h: Float,
        hour: Float, time: Float,
        weather: SkyWeather,
        bg: Bitmap?,
    ) {
        if (bg != null) cover(c, bg, w, h) else gradient(c, w, h, hour, time)
        veil(c, w, h, weather)
        scrim(c, w, h)
    }

    /* ── التدرُّج وضوءُ الساعة ─────────────────────────────── */
    private fun gradient(c: android.graphics.Canvas, w: Float, h: Float, hour: Float, t: Float) {
        var i = 0
        while (i < PAL.size - 2 && hour > PAL[i + 1][0]) i++
        val a = PAL[i]; val b = PAL[i + 1]
        val u = ((hour - a[0]) / (b[0] - a[0])).coerceIn(0f, 1f)
        val top = rgb(lerp(a[1], b[1], u), lerp(a[2], b[2], u), lerp(a[3], b[3], u))
        val bot = rgb(lerp(a[4], b[4], u), lerp(a[5], b[5], u), lerp(a[6], b[6], u))

        p.shader = LinearGradient(0f, 0f, w * 0.35f, h, top, bot, Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null

        /*  الضوءُ يعبر من الشرق إلى الغرب مع الساعة — حركةٌ صادقة:
         *  لا تتظاهر بشيءٍ لا يحدث في الواقع. والنبضةُ تحتها أبطأُ من
         *  أن تُلاحَظ، وهي التي تمنع المشهدَ أن يُقرأ صورةً ميّتة. */
        val ang = Math.PI.toFloat() * (0.18f + 0.64f * ((hour - 5.5f) / 13.5f).coerceIn(0f, 1f))
        val day = ((hour - 5f) / 2f).coerceIn(0f, 1f) * ((19.5f - hour) / 2f).coerceIn(0f, 1f)
        val lx = w * (0.5f + 0.42f * cos(ang))
        val ly = h * (0.10f + 0.10f * sin(ang))
        val glow = 0.10f + 0.10f * day + 0.012f * sin(t * 0.2f)
        p.shader = RadialGradient(
            lx, ly, h * 1.15f,
            Color.argb((glow * 255).toInt().coerceIn(0, 255), 255, 246, 222),
            Color.argb(0, 255, 246, 222), Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null

        p.shader = RadialGradient(
            w / 2f, h * 0.42f, h * 1.05f,
            intArrayOf(Color.argb(0, 0, 0, 0), Color.argb(0, 0, 0, 0), Color.argb(102, 0, 0, 0)),
            floatArrayOf(0f, 0.30f, 1f), Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null
    }

    /** صورةُ البطاقة تُملأ إلى الإطار وتُقصّ، ولا يُشوَّه نسبُها. */
    private fun cover(c: android.graphics.Canvas, b: Bitmap, w: Float, h: Float) {
        val s = max(w / b.width, h / b.height)
        val bw = b.width * s; val bh = b.height * s
        dst.set((w - bw) / 2f, (h - bh) / 2f, (w + bw) / 2f, (h + bh) / 2f)
        c.drawBitmap(b, null, dst, p)
    }

    /* ── مطرٌ وضباب ────────────────────────────────────────── */
    private fun veil(c: android.graphics.Canvas, w: Float, h: Float, wx: SkyWeather) {
        if (wx.fog > 0.04f) {
            val al = (wx.fog * 120f).toInt().coerceIn(0, 255)
            p.shader = LinearGradient(
                0f, h * 0.45f, 0f, h,
                Color.argb(0, 0xDC, 0xE4, 0xE6), Color.argb(al, 0xDC, 0xE4, 0xE6),
                Shader.TileMode.CLAMP,
            )
            c.drawRect(0f, h * 0.45f, w, h, p)
            p.shader = null
        }
        if (wx.rain > 0.04f) {
            p.style = Paint.Style.STROKE
            p.strokeWidth = w / VW * 1.5f
            p.color = Color.argb((wx.rain * 96f).toInt().coerceIn(0, 255), 0xD7, 0xE4, 0xF4)
            val n = (wx.rain * 90f).toInt().coerceIn(10, 90)
            var i = 0
            while (i < n) {
                val x = (i * 137 % 460) / 460f * w
                val y = (i * 53 % 300) / 300f * h
                c.drawLine(x, y, x - w / VW * 5f, y + h / 360f * 22f, p)
                i++
            }
            p.style = Paint.Style.FILL
        }
    }

    /* ── حجابُ الكلام ───────────────────────────────────────────
       رقمٌ لا ذوق: المطلوب ٣:١ تبايناً للنصّ الكبير، والمعيارُ حجابٌ
       أسودُ بين ٤٠٪ و٦٠٪. وقياسُ هذا التدرُّج بأسوأ بكسلٍ خلف السطر
       يعطي ما فوق ١٢:١ — بحجابٍ أخفَّ ممّا كانت تحتاجه الصورة.
    ───────────────────────────────────────────────────────────── */
    private fun scrim(c: android.graphics.Canvas, w: Float, h: Float) {
        p.shader = LinearGradient(
            0f, 0f, 0f, h * 0.82f,
            intArrayOf(
                Color.argb(153, 5, 10, 8), Color.argb(128, 5, 10, 8),
                Color.argb(46, 5, 10, 8), Color.argb(0, 5, 10, 8),
            ),
            floatArrayOf(0f, 0.52f, 0.76f, 1f), Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h * 0.82f, p)
        p.shader = null
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun rgb(r: Float, g: Float, b: Float) =
        Color.rgb(r.toInt().coerceIn(0, 255), g.toInt().coerceIn(0, 255), b.toInt().coerceIn(0, 255))
}

/**
 * خلفيّةُ الشاشة الأولى.
 *
 * لا صورةَ في المشروع ولا فيديو ولا مكتبة — والمشهدُ يعمل دون اتّصال
 * تماماً. الطقسُ وحدَه يأتي من الشبكة، وله محفوظٌ يُقرأ فوراً.
 */
@Composable
fun HeroBackdrop(
    sunAlt: Double,
    reducedMotion: Boolean,
    weather: SkyWeather = SkyWeather(),
    fade: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Transparent,
    /** صورةُ البطاقة الواردة، أو `null` فيُرسم التدرُّج. */
    background: Bitmap? = null,
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) { time = 6f; return@LaunchedEffect }
        val t0 = withFrameMillis { it }
        while (true) {
            withFrameMillis { ms -> time = (ms - t0) / 1000f }
            //  الضوءُ يزحف بمقدار الساعة لا بمقدار الإطار، فعشرُ صورٍ
            //  في الثانية تكفيه — والبطاريّةُ تشكر.
            delay(96)
        }
    }
    val hour = remember(sunAlt) { hourOfDay() }
    val d = remember { Backdrop() }
    val fadeArgb = fade.toArgb()
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (w < 8f || h < 8f) return@Canvas
        drawIntoCanvas { canvas ->
            val nc = canvas.nativeCanvas
            nc.save()
            nc.clipRect(0f, 0f, w, h)
            d.render(nc, w, h, hour, time, weather, background)
            //  ذوبانُ الأسفل في الورقة: فلا يبقى قطعٌ حادّ.
            if (fadeArgb != 0) {
                val fp = Paint()
                fp.shader = LinearGradient(
                    0f, h * 0.80f, 0f, h,
                    intArrayOf(fadeArgb and 0x00FFFFFF, fadeArgb), null, Shader.TileMode.CLAMP,
                )
                nc.drawRect(0f, h * 0.80f, w, h, fp)
            }
            nc.restore()
        }
    }
}

private fun hourOfDay(): Float {
    val c = java.util.Calendar.getInstance()
    return c.get(java.util.Calendar.HOUR_OF_DAY) + c.get(java.util.Calendar.MINUTE) / 60f
}
