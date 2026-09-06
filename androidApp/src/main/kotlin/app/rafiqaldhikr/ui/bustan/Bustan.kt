package app.rafiqaldhikr.ui.bustan

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.graphics.toArgb
import app.rafiqaldhikr.ui.sky.MoonPhase
import app.rafiqaldhikr.ui.sky.SkyWeather
import app.rafiqaldhikr.ui.sky.skyColors
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/* ══════════════════════════════════════════════════════════════
   بستان الأهوار — الرسم

   ثلاثةُ ألواحٍ للعمق، ولكلٍّ بؤرتُه وضبابُه ولونُ ساعته:

     • **البعيدُ** يُرسم في صورةٍ تُحدَّث كلَّ خمسة إطارات ثمّ تُطمس
       بتصغيرٍ وتكبير. وهو الصحيح: ما بَعُد لا يتحرّك إلّا قليلاً،
       وإعادةُ رسمه ستّين مرّةً في الثانية هدرٌ خالص.
     • **الوسطُ** كلَّ إطارين، وفيه المضيف.
     • **القريبُ** كلَّ إطار، وهو وحدَه الحادّ.

   ولونُ الساعة يُضرب في اللوح كلِّه بـ`MULTIPLY`. وهذه دقّةٌ في أندرويد
   تُغني عن حيلةٍ كاملة: ضربُ بورتر-دَف يضرب الشفافيةَ أيضاً، فالفارغُ
   يبقى فارغاً ولا يحتاج قناعاً كما يحتاج القماشُ في المتصفّح.

   والانعكاسُ يُبنى من صورتَي البعيد والوسط مقلوبتَين، ثمّ يُقطَّع
   شرائحَ تُزاح كلُّ واحدةٍ بموجها. فحين تتمايل النخلةُ تتمايل صورتُها
   في الماء — وهو ما لا يُنال برسم خطوطٍ على الماء.
══════════════════════════════════════════════════════════════ */

/** ما يتغيّر بين إطارٍ وإطار. */
internal class BustanState(
    val time: Float,
    val sunAlt: Float,
    val sunAz: Float,
    val moonLit: Float,
    val moonWaxing: Boolean,
    val weather: SkyWeather,
    val windKmh: Float,
    val still: Boolean,
)

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun clamp(v: Float, a: Float, b: Float) = max(a, min(b, v))
private fun smoothstep(a: Float, b: Float, x: Float): Float {
    val t = clamp((x - a) / (b - a), 0f, 1f); return t * t * (3 - 2 * t)
}
private fun mixColor(a: Int, b: Int, t: Float): Int = Color.rgb(
    lerp(Color.red(a).toFloat(), Color.red(b).toFloat(), t).toInt(),
    lerp(Color.green(a).toFloat(), Color.green(b).toFloat(), t).toInt(),
    lerp(Color.blue(a).toFloat(), Color.blue(b).toFloat(), t).toInt(),
)
private fun withAlpha(c: Int, a: Float) =
    Color.argb((clamp(a, 0f, 1f) * 255).toInt(), Color.red(c), Color.green(c), Color.blue(c))

internal class BustanRenderer(val scene: Scene) {

    private val w = scene.w.toInt()
    private val h = scene.h.toInt()

    private val far = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    private val mid = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    private val refl = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    private val small = Bitmap.createBitmap(max(1, w / 3), max(1, h / 3), Bitmap.Config.ARGB_8888)
    private val farC = android.graphics.Canvas(far)
    private val midC = android.graphics.Canvas(mid)
    private val reflC = android.graphics.Canvas(refl)
    private val smallC = android.graphics.Canvas(small)

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val filter = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val add = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }
    private val atop = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP) }
    private val srcRect = Rect()
    private val dstRect = RectF()
    private var frame = 0
    private val unit = h / 744f

    /** حَبُّ الورق: يُخبز مرّةً ويُبلَّط. بدونه يبدو الرسمُ بلاستيكاً. */
    private val grain: Bitmap = run {
        val b = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
        val r = Rng(5)
        val px = IntArray(96 * 96)
        for (i in px.indices) {
            val v = r.next()
            px[i] = if (v < 0.5f) Color.argb((14 * (1 - v * 2)).toInt(), 0, 0, 0)
            else Color.argb((14 * (v * 2 - 1)).toInt(), 255, 255, 255)
        }
        b.setPixels(px, 0, 96, 0, 0, 96, 96)
        b
    }
    private val grainPaint = Paint().apply {
        shader = android.graphics.BitmapShader(grain, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    /** موضعُ الشمس على الشاشة من سَمْتها — كما في نسخة المتصفّح. */
    private fun sunScreenX(az: Float) = scene.w * (1f - ((az - 70f) / 220f))

    private fun sway(windKmh: Float, still: Boolean): Float =
        if (still) 0f else Math.pow((windKmh / 60f).toDouble(), 0.72).toFloat() * 1.5f + 0.14f

    /** يرسم شجرةً ومعها حافّتُها المضيئة إن وقفت الشمسُ خلفها. */
    private fun tree(
        c: android.graphics.Canvas, t: Tree, s: BustanState, sway: Float,
        sunX: Float, day: Float,
    ) {
        val lean = sin(s.time * (0.85f + t.depth * 0.5f) + t.phase) *
            sway * (0.030f + 0.028f * (1f - t.depth))
        c.save()
        c.translate(t.x, t.y)
        c.drawBitmap(t.trunk, -t.trunkAx, -t.trunkAy, null)
        if (t.kind == Kind.PALM) {
            c.save()
            c.translate(0f, -t.height * 0.78f)
            c.rotate(Math.toDegrees((lean * 0.7f).toDouble()).toFloat())
            for (f in t.fronds) drawFrond(c, p, f, s.time, sway, Pal.palmDark, unit)
            p.color = Pal.trunkDark
            c.drawCircle(0f, 0f, t.height * 0.028f, p)
            c.restore()
        } else {
            val can = t.canopy ?: return c.restore()
            c.save()
            c.translate(0f, -t.height * (if (t.kind == Kind.CYPRESS) 0.20f else 0.44f))
            c.rotate(Math.toDegrees(lean.toDouble()).toFloat())
            c.translate(0f, -t.height * 0.62f * 0.34f)
            c.drawBitmap(can, -can.width / 2f, -can.height / 2f, null)
            if (s.sunAlt > -2f) {
                val d = abs(t.x - sunX)
                val reach = scene.w * 0.37f
                if (d < reach) {
                    val k = (1f - d / reach) * day * 0.17f
                    val dir = if (sunX > t.x) 1f else -1f
                    t.rim?.let {
                        add.alpha = (k * 255).toInt()
                        c.drawBitmap(it, -it.width / 2f + dir * 2.4f * unit,
                            -it.height / 2f - 1.6f * unit, add)
                    }
                }
            }
            c.restore()
        }
        c.restore()
    }

    /** ظلٌّ على الأرض: طولُه ‎1/ظل(ارتفاع الشمس)، وجهتُه عكسُ الشمس. */
    private fun shadow(
        c: android.graphics.Canvas, t: Tree, sunX: Float, sunAlt: Float, day: Float, cloud: Float,
    ) {
        if (sunAlt < 1f || day <= 0.02f) return
        val l = clamp((1.0 / tan(Math.toRadians(max(sunAlt, 4f).toDouble()))).toFloat(), 0.35f, 5.2f)
        val side = if (t.x < sunX) -1f else 1f
        val len = t.height * 0.30f * l
        val wid = t.height * 0.30f
        val a = (0.30f - 0.16f * clamp(l / 5f, 0f, 1f)) * day * (1f - cloud * 0.75f)
        val cx = t.x + side * len * 0.42f
        val rx = max(len * 0.7f, wid * 0.6f)
        val ry = wid * 0.20f
        p.shader = RadialGradient(cx, t.y + 3f, max(rx, 1f),
            withAlpha(Color.rgb(14, 26, 20), a), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.save(); c.translate(cx, t.y + 3f); c.scale(1f, ry / max(rx, 1f)); c.translate(-cx, -(t.y + 3f))
        c.drawCircle(cx, t.y + 3f, rx, p)
        c.restore()
        p.shader = null
    }

    /** يصبغ لوحاً بلون الساعة ثمّ يغرقه في هواء المسافة. */
    private fun grade(c: android.graphics.Canvas, tint: Int, haze: Float, bottom: Int, top: Float) {
        c.drawColor(tint, PorterDuff.Mode.MULTIPLY)
        if (haze > 0f) {
            atop.shader = LinearGradient(0f, top, 0f, scene.riverBottom + 40f * unit,
                withAlpha(bottom, haze), withAlpha(bottom, haze * 0.15f), Shader.TileMode.CLAMP)
            c.drawRect(0f, 0f, scene.w, scene.h, atop)
            atop.shader = null
        }
    }

    fun render(g: android.graphics.Canvas, s: BustanState) {
        frame++
        val sunX = sunScreenX(s.sunAz)
        val sunY = scene.horizon - max(s.sunAlt, -4f) / 72f * scene.horizon * 0.92f
        val (topC, botC) = skyColors(s.sunAlt)
        val top = topC.toArgb(); val bot = botC.toArgb()
        val night = clamp((-s.sunAlt - 2f) / 10f, 0f, 1f)
        val day = smoothstep(-6f, 10f, s.sunAlt)
        val cloud = when {
            s.weather.rain > 0.05f -> 0.94f
            s.weather.fog > 0.05f -> 0.55f
            else -> max(0.10f, s.weather.cloud)
        }
        val swy = sway(s.windKmh, s.still)
        val tint = mixColor(
            mixColor(Color.rgb(0x5C, 0x6E, 0x9E), Color.rgb(0xFF, 0xB2, 0x7A), smoothstep(-8f, 4f, s.sunAlt)),
            Color.WHITE, smoothstep(2f, 24f, s.sunAlt),
        ).let { mixColor(it, Color.rgb(0x2A, 0x3A, 0x6B), night * 0.86f) }

        drawSky(g, s, top, bot, night, day, cloud, sunX, sunY)

        if (frame % 5 == 1) {
            far.eraseColor(Color.TRANSPARENT)
            drawFarLayer(farC, s, swy, sunX, day)
            grade(farC, tint, 0.34f, bot, scene.horizon - 60f * unit)
        }
        if (frame % 2 == 1) {
            mid.eraseColor(Color.TRANSPARENT)
            drawMidLayer(midC, s, swy, sunX, day, cloud)
            grade(midC, tint, 0.13f, bot, scene.horizon + 10f * unit)
        }

        //  طمسُ البعيد: تصغيرٌ ثمّ تكبير. أرخصُ طمسٍ يعمل على كلّ جهاز.
        small.eraseColor(Color.TRANSPARENT)
        srcRect.set(0, 0, w, h)
        smallC.drawBitmap(far, srcRect, RectF(0f, 0f, small.width.toFloat(), small.height.toFloat()), filter)
        srcRect.set(0, 0, small.width, small.height)
        dstRect.set(0f, 0f, scene.w, scene.h)
        g.drawBitmap(small, srcRect, dstRect, filter)
        g.drawBitmap(mid, 0f, 0f, filter)

        drawWater(g, s, top, bot, day, swy, sunX, tint)
        drawNearLayer(g, s, swy, sunX, day, cloud, tint)
        if (s.sunAlt > 2f && s.weather.rain <= 0.05f) drawShafts(g, s, sunX, sunY, day)
        drawLife(g, s, night, day, bot, top)
        drawWeather(g, s, bot)
        drawPost(g, day)
    }

    private fun drawSky(
        g: android.graphics.Canvas, s: BustanState, top: Int, bot: Int,
        night: Float, day: Float, cloud: Float, sunX: Float, sunY: Float,
    ) {
        p.shader = LinearGradient(0f, 0f, 0f, scene.horizon + 10f * unit,
            top, bot, Shader.TileMode.CLAMP)
        g.drawRect(0f, 0f, scene.w, scene.h, p)
        p.shader = null
        if (night > 0.03f) {
            for (st in scene.stars) {
                val b = Math.pow(10.0, (-0.4 * (1 + st.mag * 4)).toDouble()).toFloat()
                val a = clamp(b * 7 * night * (0.82f + 0.18f * sin(s.time * 2.2f + st.phase)), 0f, 1f)
                p.color = withAlpha(Color.rgb(0xEA, 0xF0, 0xFF), a)
                val sz = (1.2f + b * 3f) * unit
                g.drawRect(st.x, st.y, st.x + sz, st.y + sz, p)
            }
        }
        if (s.sunAlt > -4f) {
            val r = 250f * unit
            p.shader = RadialGradient(sunX, sunY, r,
                withAlpha(Color.rgb(0xFF, 0xD9, 0x8A), 0.30f + 0.36f * day),
                Color.TRANSPARENT, Shader.TileMode.CLAMP)
            g.drawCircle(sunX, sunY, r, p)
            p.shader = null
            p.color = withAlpha(mixColor(Color.rgb(0xE3, 0xC0, 0x5B), Color.rgb(255, 252, 238),
                0.55f + 0.35f * day), 0.96f)
            g.drawCircle(sunX, sunY, 19f * unit, p)
        } else if (night > 0.3f) {
            val mx = scene.w * 0.22f; val my = scene.horizon * 0.24f
            val r = 20f * unit
            g.saveLayer(mx - r * 2, my - r * 2, mx + r * 2, my + r * 2, null)
            p.color = withAlpha(Color.rgb(240, 244, 255), 0.92f * night)
            g.drawCircle(mx, my, r, p)
            //  الهلالُ يُقطع من القرص بحسب النسبة المضاءة وجهةِ التزايد.
            val cut = (1f - s.moonLit) * r * 1.9f
            p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            g.drawCircle(mx + (if (s.moonWaxing) -cut else cut), my, r * 0.95f, p)
            p.xfermode = null
            g.restore()
        }
        for (c in scene.clouds) {
            val cx = (c.x + s.time * c.speed * 10f * unit) % (scene.w + 320f * unit) - 160f * unit
            val a = cloud * (0.30f + 0.28f * sin(c.phase))
            if (a < 0.02f) continue
            val lit = mixColor(mixColor(bot, Color.rgb(0xFF, 0xE9, 0xC8), 0.42f * day),
                Color.rgb(26, 32, 52), night * 0.7f)
            for (k in 0 until 4) {
                val rr = c.r * (1 - k * 0.18f)
                val ox = sin(c.phase + k * 2.1f) * c.r * 0.52f
                val oy = cos(c.phase + k) * c.r * 0.15f
                p.shader = RadialGradient(cx + ox, c.y + oy, max(rr, 1f),
                    withAlpha(lit, a * 0.5f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                g.drawCircle(cx + ox, c.y + oy, rr, p)
                p.shader = null
            }
        }
    }

    private fun drawFarLayer(
        c: android.graphics.Canvas, s: BustanState, swy: Float, sunX: Float, day: Float,
    ) {
        p.color = Color.rgb(0x3A, 0x65, 0x52)
        val path = Path()
        path.moveTo(-40f * unit, scene.horizon + 8f * unit)
        var x = -40f * unit
        while (x <= scene.w + 40f * unit) {
            path.lineTo(x, scene.horizon + 3f * unit -
                sin(x * 0.0055f / unit + 1.2f) * 15f * unit - sin(x * 0.012f / unit) * 7f * unit)
            x += 32f * unit
        }
        path.lineTo(scene.w + 40f * unit, scene.horizon + 40f * unit)
        path.lineTo(-40f * unit, scene.horizon + 40f * unit)
        path.close()
        c.drawPath(path, p)
        drawMinaret(c, p, scene.minaretX, scene.horizon + 9f * unit, 120f * unit)
        for (t in scene.far) tree(c, t, s, swy * 0.55f, sunX, day)
    }

    private fun drawMidLayer(
        c: android.graphics.Canvas, s: BustanState, swy: Float, sunX: Float, day: Float, cloud: Float,
    ) {
        p.color = Color.rgb(0x41, 0x69, 0x4B)
        c.drawRect(0f, scene.horizon + 14f * unit, scene.w, scene.riverTop, p)
        drawMudhif(c, p, scene.mudhifX, scene.riverTop - 2f * unit, 52f * unit, 46f * unit)
        for (t in scene.mid) shadow(c, t, sunX, s.sunAlt, day, cloud)
        for (t in scene.mid) tree(c, t, s, swy * 0.85f, sunX, day)
    }

    private fun drawWater(
        g: android.graphics.Canvas, s: BustanState, top: Int, bot: Int,
        day: Float, swy: Float, sunX: Float, tint: Int,
    ) {
        //  الانعكاس: البعيدُ والوسطُ مقلوبان حول سطح الماء
        refl.eraseColor(Color.TRANSPARENT)
        reflC.save()
        reflC.translate(0f, scene.riverTop * 2f)
        reflC.scale(1f, -1f)
        reflC.drawBitmap(far, 0f, 0f, filter)
        reflC.drawBitmap(mid, 0f, 0f, filter)
        reflC.restore()

        g.save()
        g.clipRect(0f, scene.riverTop, scene.w, scene.riverBottom)
        p.shader = LinearGradient(0f, scene.riverTop, 0f, scene.riverBottom,
            intArrayOf(
                mixColor(mixColor(bot, top, 0.12f), Color.rgb(0x16, 0x30, 0x2A), 0.30f),
                mixColor(mixColor(top, bot, 0.4f), Color.rgb(0x0E, 0x24, 0x1F), 0.50f),
                mixColor(top, Color.rgb(0x0A, 0x1A, 0x16), 0.70f),
            ),
            floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        g.drawRect(0f, scene.riverTop, scene.w, scene.riverBottom, p)
        p.shader = null

        filter.alpha = 87
        var y = scene.riverTop
        val step = 3f * unit
        while (y < scene.riverBottom) {
            val u = (y - scene.riverTop) / (scene.riverBottom - scene.riverTop)
            val off = sin(y * 0.16f / unit + s.time * 1.5f) * (1.2f + u * 6.5f) * unit * (0.5f + swy * 0.5f)
            //  قصُّ الشريحة داخل حدود الصورة: `drawBitmap` بمستطيلٍ
            //  خارجَها سلوكٌ غيرُ معرَّف.
            val y0 = y.toInt().coerceIn(0, h - 1)
            val y1 = (y + step).toInt().coerceIn(y0 + 1, h)
            srcRect.set(0, y0, w, y1)
            dstRect.set(off, y, scene.w + off, y + step)
            g.drawBitmap(refl, srcRect, dstRect, filter)
            y += step
        }
        filter.alpha = 255

        p.color = withAlpha(mixColor(top, Color.rgb(0x0C, 0x1E, 0x1A), 0.55f), 0.34f)
        g.drawRect(0f, scene.riverTop, scene.w, scene.riverBottom, p)

        p.style = Paint.Style.STROKE
        for (i in 0 until 15) {
            val yy = scene.riverTop + 6f * unit + i * ((scene.riverBottom - scene.riverTop - 8f * unit) / 15f)
            val dep = i / 15f
            p.color = withAlpha(mixColor(Color.rgb(0xFF, 0xF3, 0xD6), top, 0.35f),
                (0.05f + 0.16f * dep) * (0.4f + day))
            p.strokeWidth = (1f + dep * 1.7f) * unit
            val wave = Path()
            var wx = 0f
            while (wx <= scene.w) {
                val wy = yy + sin(wx * 0.021f / unit + s.time * (0.55f + dep * 1.5f) + i * 1.3f) *
                    (1.1f + dep * 2.4f) * unit
                if (wx == 0f) wave.moveTo(wx, wy) else wave.lineTo(wx, wy)
                wx += 12f * unit
            }
            g.drawPath(wave, p)
        }
        p.style = Paint.Style.FILL

        if (s.sunAlt > -2f) {
            p.shader = LinearGradient(0f, scene.riverTop, 0f, scene.riverBottom,
                withAlpha(Color.rgb(0xFF, 0xE6, 0xAE), (0.16f * day + 0.05f) * 0.6f),
                Color.TRANSPARENT, Shader.TileMode.CLAMP)
            val glit = Path()
            glit.moveTo(sunX - 8f * unit, scene.riverTop)
            glit.lineTo(sunX + 8f * unit, scene.riverTop)
            glit.lineTo(sunX + 32f * unit, scene.riverBottom)
            glit.lineTo(sunX - 32f * unit, scene.riverBottom)
            glit.close()
            g.drawPath(glit, p)
            p.shader = null
        }

        for (d in scene.ducks) {
            val span = scene.w + 80f * unit
            var dx = (d.x + s.time * d.speed) % span
            if (dx < 0) dx += span
            dx -= 40f * unit
            val dy = d.y + sin(s.time * 0.8f + d.phase) * 1.5f * unit
            val sgn = if (d.speed > 0) 1f else -1f
            p.style = Paint.Style.STROKE
            p.strokeWidth = 1.1f * unit
            p.color = withAlpha(mixColor(Color.rgb(0xFF, 0xF6, 0xE0), top, 0.35f), 0.18f)
            for (sg in intArrayOf(-1, 1)) {
                g.drawLine(dx, dy + unit, dx - sgn * 32f * unit, dy + unit + sg * 8f * unit, p)
            }
            p.style = Paint.Style.FILL
            p.color = mixColor(Pal.leafDark, tint, 0.35f)
            g.drawOval(dx - 6.4f * unit, dy - 2.9f * unit, dx + 6.4f * unit, dy + 2.9f * unit, p)
            g.drawOval(dx + sgn * 5f * unit - 2.2f * unit, dy - 6.5f * unit,
                dx + sgn * 5f * unit + 2.2f * unit, dy - 0.9f * unit, p)
            p.color = Color.rgb(0xD8, 0xA8, 0x3C)
            g.drawRect(dx + sgn * 7f * unit, dy - 4.1f * unit,
                dx + sgn * 7f * unit + 2.4f * unit, dy - 2.7f * unit, p)
        }

        //  المشحوف
        val mx = ((s.time * 7.5f * unit + 120f * unit) % (scene.w + 220f * unit)) - 110f * unit
        val my = scene.riverTop + (scene.riverBottom - scene.riverTop) * 0.52f + sin(s.time * 0.7f) * 1.4f * unit
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.2f * unit
        p.color = withAlpha(mixColor(Color.rgb(0xFF, 0xF6, 0xE0), top, 0.4f), 0.16f)
        for (sg in intArrayOf(-1, 1)) {
            g.drawLine(mx - 30f * unit, my + unit, mx - 72f * unit, my + unit + sg * 11f * unit, p)
        }
        p.style = Paint.Style.FILL
        drawMashhoof(g, p, mx, my, 64f * unit, mixColor(Pal.hull, tint, 0.30f))
        g.restore()
    }

    private fun drawNearLayer(
        g: android.graphics.Canvas, s: BustanState, swy: Float, sunX: Float,
        day: Float, cloud: Float, tint: Int,
    ) {
        val bounds = RectF(0f, 0f, scene.w, scene.h)
        g.saveLayer(bounds, null)
        p.shader = LinearGradient(0f, scene.riverBottom - 6f * unit, 0f, scene.h,
            Color.rgb(0x3C, 0x66, 0x40), Color.rgb(0x1F, 0x3A, 0x24), Shader.TileMode.CLAMP)
        val bank = Path()
        bank.moveTo(-40f * unit, scene.riverBottom)
        var x = -40f * unit
        while (x <= scene.w + 40f * unit) {
            bank.lineTo(x, scene.riverBottom - 4f * unit + sin(x * 0.016f / unit) * 5f * unit)
            x += 28f * unit
        }
        bank.lineTo(scene.w + 40f * unit, scene.h)
        bank.lineTo(-40f * unit, scene.h)
        bank.close()
        g.drawPath(bank, p)
        p.shader = null

        for (t in scene.near) shadow(g, t, sunX, s.sunAlt, day, cloud)
        for (t in scene.near) tree(g, t, s, swy, sunX, day)

        for (r in scene.reeds) {
            val a = sin(s.time * 2.1f + r.phase) * swy * 0.12f + r.lean * 0.1f
            val path = Path()
            path.moveTo(r.x - 1.7f * unit, r.y)
            path.quadTo(r.x + a * r.h * 0.4f, r.y - r.h * 0.6f, r.x + a * r.h + r.lean * 5f * unit, r.y - r.h)
            path.quadTo(r.x + a * r.h * 0.4f + 2f * unit, r.y - r.h * 0.6f, r.x + 1.7f * unit, r.y)
            path.close()
            p.color = if (r.near) Pal.reed else mixColor(Pal.reed, Pal.cypDark, 0.35f)
            g.drawPath(path, p)
        }
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * unit
        p.color = withAlpha(Pal.leafDark, 0.75f)
        for (u in scene.tufts) {
            val a = sin(s.time * 2.4f + u.phase) * swy * 0.10f
            for (k in -1..1) {
                val blade = Path()
                blade.moveTo(u.x + k * 2.4f * unit, u.y)
                blade.quadTo(u.x + k * 3f * unit + a * u.s, u.y - u.s * 0.6f,
                    u.x + k * 4.5f * unit + a * u.s * 2.2f, u.y - u.s)
                g.drawPath(blade, p)
            }
        }
        p.style = Paint.Style.FILL
        for (f in scene.flowers) {
            val a = sin(s.time * 2.2f + f.phase) * swy * 3.2f * unit
            p.color = when (f.kind) {
                0 -> Color.rgb(0xD8, 0xB2, 0x3A)
                1 -> Color.rgb(0xC9, 0x55, 0x6A)
                else -> Color.rgb(0xE8, 0xE2, 0xD0)
            }
            for (k in 0 until 5) {
                val an = k / 5f * 6.2832f
                g.drawCircle(f.x + a + cos(an) * 2.4f * unit, f.y + sin(an) * 2.4f * unit, 1.7f * unit, p)
            }
            p.color = Color.rgb(0xF0, 0xDC, 0x9A)
            g.drawCircle(f.x + a, f.y, 1.3f * unit, p)
        }
        drawHeron(g, p, scene.heronX, scene.riverBottom + 9f * unit, 60f * unit,
            mixColor(Pal.heron, Color.rgb(0x7A, 0x8A, 0x86), 0.35f))
        g.drawColor(tint, PorterDuff.Mode.MULTIPLY)
        g.restore()
    }

    private fun drawShafts(
        g: android.graphics.Canvas, s: BustanState, sunX: Float, sunY: Float, day: Float,
    ) {
        val k = smoothstep(2f, 26f, s.sunAlt)
        g.save()
        g.translate(sunX, sunY)
        for (i in 0 until 11) {
            val a = (i / 11f - 0.5f) * 1.05f + sin(s.time * 0.08f + i) * 0.03f + (Math.PI / 2).toFloat()
            val wd = 0.013f + 0.019f * abs(sin(i * 2.3f + s.time * 0.15f))
            add.color = Color.rgb(0xFF, 0xE7, 0xB4)
            add.alpha = ((0.012f + 0.014f * abs(sin(s.time * 0.4f + i * 1.7f))) * day * k * 255).toInt()
            val ray = Path()
            ray.moveTo(0f, 0f)
            ray.lineTo(cos(a - wd) * 860f * unit, sin(a - wd) * 860f * unit)
            ray.lineTo(cos(a + wd) * 860f * unit, sin(a + wd) * 860f * unit)
            ray.close()
            g.drawPath(ray, add)
        }
        g.restore()
        for (m in scene.motes) {
            var mx = (m.x + sin(s.time * 0.28f + m.phase) * 22f * unit + s.time * m.speed * 6f * unit) % scene.w
            if (mx < 0) mx += scene.w
            var my = (m.y + cos(s.time * 0.21f + m.phase * 1.4f) * 16f * unit - s.time * m.speed * 4f * unit) % scene.h
            if (my < 0) my += scene.h
            if (my < scene.horizon * 0.5f || my > scene.riverBottom + 70f * unit) continue
            val d = abs(mx - sunX) / scene.w
            val a = clamp(1f - d * 1.7f, 0f, 1f) * day * 0.28f * (0.5f + 0.5f * sin(s.time * 1.4f + m.phase))
            if (a <= 0.02f) continue
            add.color = Color.rgb(0xFF, 0xF0, 0xC8)
            add.alpha = (a * 255).toInt()
            g.drawCircle(mx, my, m.r, add)
        }
    }

    private fun drawLife(
        g: android.graphics.Canvas, s: BustanState, night: Float, day: Float, bot: Int, top: Int,
    ) {
        val raining = s.weather.rain > 0.05f
        val ink = mixColor(mixColor(bot, Color.rgb(18, 24, 22), 0.74f),
            Color.rgb(0xFF, 0xD9, 0xA0), day * 0.18f)
        if (night < 0.45f && !raining) {
            p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
            for ((i, b) in scene.birds.withIndex()) {
                val span = scene.w + 120f * unit
                var x = (s.time * b.speed + b.phase) % span - 60f * unit
                if (x < -60f * unit) x += span
                val y = b.y + sin(s.time * 0.62f + i) * 13f * unit
                val f = sin(s.time * b.flap + i) * 0.95f
                p.color = withAlpha(ink, clamp(1f - night * 2.2f, 0f, 1f) * 0.9f)
                p.strokeWidth = max(1.2f * unit, b.size * 0.32f)
                val wing = Path()
                wing.moveTo(x - b.size * 1.8f, y - f * b.size * 0.9f)
                wing.quadTo(x - b.size * 0.6f, y + b.size * 0.5f, x, y)
                wing.quadTo(x + b.size * 0.6f, y + b.size * 0.5f, x + b.size * 1.8f, y - f * b.size * 0.9f)
                g.drawPath(wing, p)
            }
            p.style = Paint.Style.FILL
            for (i in 0 until 7) {
                val f = scene.flowers.getOrNull(i * 4) ?: continue
                val bx = f.x + sin(s.time * 0.9f + i) * 26f * unit
                val by = f.y - 18f * unit + sin(s.time * 1.7f + i * 2) * 11f * unit
                val ww = abs(sin(s.time * 9f + i)) * 4.4f * unit + 1.2f * unit
                p.color = withAlpha(mixColor(Color.rgb(0xE9, 0xD0, 0x7A), Color.WHITE, 0.3f),
                    0.9f * clamp(1f - night * 2f, 0f, 1f))
                g.drawOval(bx - 2.4f * unit - ww, by - 3.1f * unit, bx - 2.4f * unit + ww, by + 3.1f * unit, p)
                g.drawOval(bx + 2.4f * unit - ww, by - 3.1f * unit, bx + 2.4f * unit + ww, by + 3.1f * unit, p)
            }
        } else if (!raining) {
            for (m in scene.motes.take(30)) {
                val x = m.x + cos(s.time * 0.5f + m.phase) * 26f * unit
                val y = scene.riverBottom + 20f * unit + sin(s.time * 0.71f + m.phase * 1.7f) * 26f * unit
                val a = max(0f, sin(s.time * 1.9f + m.phase)) * night
                if (a < 0.05f) continue
                p.shader = RadialGradient(x, y, 8f * unit,
                    withAlpha(Color.rgb(0xE4, 0xF2, 0x98), a * 0.95f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
                g.drawCircle(x, y, 8f * unit, p)
                p.shader = null
            }
        }
    }

    private fun drawWeather(g: android.graphics.Canvas, s: BustanState, bot: Int) {
        if (s.weather.rain > 0.05f) {
            p.color = Color.argb(51, 48, 60, 70)
            g.drawRect(0f, 0f, scene.w, scene.h, p)
            p.style = Paint.Style.STROKE; p.strokeWidth = 1.3f * unit
            p.color = Color.argb(112, 206, 222, 240)
            for (d in scene.drops) {
                var y = (d.y + s.time * 640f * unit * d.speed) % scene.h
                if (y < 0) y += scene.h
                val x = d.x + y * 0.12f
                g.drawLine(x, y, x - 3.4f * unit, y + d.len, p)
            }
            p.style = Paint.Style.FILL
        }
        if (s.weather.fog > 0.05f) {
            val fogTop = scene.horizon - 80f * unit
            p.shader = LinearGradient(0f, fogTop, 0f, scene.h,
                withAlpha(mixColor(bot, Color.rgb(228, 232, 236), 0.55f), 0f),
                withAlpha(mixColor(bot, Color.rgb(228, 232, 236), 0.72f), 0.86f * s.weather.fog),
                Shader.TileMode.CLAMP)
            g.drawRect(0f, fogTop, scene.w, scene.h, p)
            p.shader = null
        }
    }

    private fun drawPost(g: android.graphics.Canvas, day: Float) {
        grainPaint.alpha = 255
        g.drawRect(0f, 0f, scene.w, scene.h, grainPaint)
        p.shader = RadialGradient(scene.w * 0.5f, scene.horizon * 0.78f, scene.w * 0.95f,
            intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(87, 5, 9, 11)),
            floatArrayOf(0f, 0.17f, 1f), Shader.TileMode.CLAMP)
        g.drawRect(0f, 0f, scene.w, scene.h, p)
        p.shader = null
    }
}

/**
 * يحفظ المُصيِّرَ عبر الإطارات ويعيد بناءه إن تغيّر المقاس وحدَه.
 *
 * وخبزُ التيجان ثقيلٌ فلا يجوز أن يقع في كلّ إطار — ولا `remember`
 * يعمل داخل لمبدا الرسم لأنّها ليست تركيباً.
 */
private class RendererHolder {
    private var w = 0
    private var h = 0
    private var r: BustanRenderer? = null
    fun get(sw: Float, sh: Float): BustanRenderer {
        val iw = sw.toInt(); val ih = sh.toInt()
        val cur = r
        if (cur != null && iw == w && ih == h) return cur
        w = iw; h = ih
        return BustanRenderer(Scene(sw, sh)).also { r = it }
    }
}

/**
 * سماءُ الشاشة الرئيسية: بستانُ أهوارٍ حيّ.
 *
 * لا صورةَ في المشروع ولا فيديو ولا مكتبة — والمشهدُ كلُّه يعمل دون
 * اتّصال. الطقسُ والريحُ وحدَهما يأتيان من الشبكة، ولهما محفوظٌ يُقرأ
 * فوراً، وبدونهما تبقى السماءُ صحواً ساكنة.
 */
@Composable
fun Bustan(
    sunAlt: Double,
    sunAz: Double,
    moon: MoonPhase,
    reducedMotion: Boolean,
    weather: SkyWeather = SkyWeather(),
    modifier: Modifier = Modifier,
) {
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) { time = 12f; return@LaunchedEffect }
        val t0 = withFrameMillis { it }
        while (true) {
            withFrameMillis { ms -> time = (ms - t0) / 1000f }
            //  ستٌّ وعشرون صورةً في الثانية: السعفُ لا يحتاج أكثر،
            //  والبطاريةُ تحتاج أقلّ.
            delay(38)
        }
    }
    val holder = remember { RendererHolder() }
    Canvas(modifier) {
        val pxW = size.width
        val pxH = size.height
        if (pxW < 8f || pxH < 8f) return@Canvas
        //  المشهدُ يُرسم بعرضٍ ثابتٍ ثمّ يُمدَّد: فلا يثقل على شاشةٍ
        //  عالية الكثافة، ولا يختلف شكلُه بين جهازٍ وجهاز.
        val sw = min(pxW, 620f)
        val sh = sw * (pxH / pxW)
        val r = holder.get(sw, sh)
        drawIntoCanvas { c ->
            val nc = c.nativeCanvas
            nc.save()
            nc.scale(pxW / sw, pxH / sh)
            r.render(nc, BustanState(
                time = time,
                sunAlt = sunAlt.toFloat(),
                sunAz = sunAz.toFloat(),
                moonLit = moon.illumination.toFloat(),
                moonWaxing = moon.waxing,
                weather = weather,
                windKmh = weather.windKmh,
                still = reducedMotion,
            ))
            nc.restore()
        }
    }
}
