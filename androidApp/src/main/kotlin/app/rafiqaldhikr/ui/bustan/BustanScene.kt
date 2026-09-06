package app.rafiqaldhikr.ui.bustan

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   بستان الأهوار — الأشكالُ والتخطيط

   ما في هذا الملفّ يُحسب **مرّةً واحدةً عند فتح الشاشة** ثمّ لا يُمسّ:
   مواضعُ الشجر، وصورُ التيجان المخبوزة، وقناعُ الحافّة المضيئة. أمّا
   الحركةُ فتقع في `Bustan.kt` على هذه المخبوزات.

   ولماذا يُخبز التاجُ صورةً بدل أن يُرسم كلَّ إطار؟ لأنّ التاجَ الواحد
   نحوُ سبعين قرصاً وثلاثين نقطةَ ورق. أربعون شجرةً في الإطار تعني
   أربعةَ آلاف رسمة في كلّ إطار — وهذا ما لا يحتمله هاتف. فتُرسم مرّةً
   في صورةٍ صغيرة، ثمّ يُدار الرسمُ بزاويةٍ واحدةٍ كلَّ إطار.

   والمعالمُ — المضيفُ والمشحوفُ والبلشونُ والمنارة — تُرسم مباشرةً؛
   فهي واحدةٌ لا أربعون.
══════════════════════════════════════════════════════════════ */

/** مولِّدٌ ثابتُ البذرة: نفسُ البستان في كلّ فتحةٍ للتطبيق. */
internal class Rng(private var s: Long = 1L) {
    fun next(): Float { s = (s * 16807) % 2147483647; return s / 2147483647f }
    fun range(a: Float, b: Float) = a + next() * (b - a)
}

internal object Pal {
    val leafDark = Color.rgb(0x1B, 0x3E, 0x2C)
    val leafMid = Color.rgb(0x32, 0x68, 0x4A)
    val leafLit = Color.rgb(0x5E, 0x9A, 0x63)
    val palmDark = Color.rgb(0x20, 0x4A, 0x36)
    val palmMid = Color.rgb(0x3B, 0x75, 0x48)
    val cypDark = Color.rgb(0x14, 0x2F, 0x24)
    val cypMid = Color.rgb(0x1F, 0x48, 0x35)
    val cypLit = Color.rgb(0x2F, 0x64, 0x48)
    val trunk = Color.rgb(0x7A, 0x5C, 0x38)
    val trunkDark = Color.rgb(0x40, 0x2B, 0x18)
    val reed = Color.rgb(0x4C, 0x74, 0x40)
    val mud = Color.rgb(0x9A, 0x7B, 0x4E)
    val mudDark = Color.rgb(0x5E, 0x48, 0x2A)
    val minaret = Color.rgb(0xC6, 0xB1, 0x89)
    val minaretDark = Color.rgb(0x8B, 0x75, 0x50)
    val fruit = Color.rgb(0xA8, 0x30, 0x3C)
    val fruitDark = Color.rgb(0x7C, 0x1E, 0x28)
    val heron = Color.rgb(0xD8, 0xDA, 0xD2)
    val hull = Color.rgb(0x3B, 0x2A, 0x18)
    val rim = Color.rgb(0xFF, 0xE0, 0xA8)
}

internal enum class Kind { PALM, ROUND, CYPRESS }

/** سعفةٌ واحدة — زاويتُها وطولُها وطورُها في الريح. */
internal class Frond(val angle: Float, val len: Float, val phase: Float, val side: Float)

/** شجرةٌ مخبوزة: جذعٌ وصورةُ تاجٍ وقناعُ حافّة. */
internal class Tree(
    val kind: Kind,
    val height: Float,
    val x: Float,
    val y: Float,
    val phase: Float,
    val depth: Float,
    val trunk: Bitmap,
    val trunkAx: Float,
    val trunkAy: Float,
    val canopy: Bitmap?,
    val rim: Bitmap?,
    val fronds: List<Frond>,
)

internal class Reed(val x: Float, val y: Float, val h: Float, val phase: Float,
                    val lean: Float, val near: Boolean)
internal class Tuft(val x: Float, val y: Float, val s: Float, val phase: Float)
internal class Flower(val x: Float, val y: Float, val kind: Int, val phase: Float)
internal class Bird(val y: Float, val speed: Float, val phase: Float, val size: Float, val flap: Float)
internal class Mote(val x: Float, val y: Float, val phase: Float, val r: Float, val speed: Float)
internal class Duck(val x: Float, val y: Float, val speed: Float, val phase: Float)
internal class Cloud(val x: Float, val y: Float, val r: Float, val speed: Float, val phase: Float)
internal class Star(val x: Float, val y: Float, val mag: Float, val phase: Float)
internal class Drop(val x: Float, val y: Float, val speed: Float, val len: Float)

/* ── خبزُ الأشكال ───────────────────────────────────────── */

private fun bmp(w: Int, h: Int): Bitmap =
    Bitmap.createBitmap(maxOf(1, w), maxOf(1, h), Bitmap.Config.ARGB_8888)

/**
 * تاجٌ من أقراص: الداكنُ في جهة الظلّ والفاتحُ في جهة الشمس، ثمّ نُقَطُ
 * ورقٍ فوقه. ومحيطُه مكسورٌ بضجيجٍ خفيفٍ فلا يُقرأ دائرةً.
 */
private fun bakeCanopy(rw: Float, rh: Float, tones: IntArray, fruit: Boolean, rng: Rng): Bitmap {
    val b = bmp((rw * 2 + 16).toInt(), (rh * 2 + 16).toInt())
    val c = Canvas(b)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = b.width / 2f; val cy = b.height / 2f
    val n = (rw * rh / 30f).toInt() + 40
    repeat(n) {
        val a = rng.next() * 6.2832f
        val r = rng.next().pow(0.55f)
        val wob = 1f + 0.18f * sin(a * 3.1f) + 0.12f * sin(a * 5.9f)
        val px = cx + cos(a) * r * rw * wob
        val py = cy + sin(a) * r * rh * 0.94f * wob
        val rad = rw * 0.19f * (0.28f + rng.next().pow(1.7f) * 1.65f)
        val lit = (-(py - cy) / rh) * 0.62f + ((px - cx) / rw) * 0.5f + (rng.next() - 0.5f) * 0.5f
        p.color = tones[if (lit < -0.18f) 0 else if (lit < 0.24f) 1 else 2]
        c.drawCircle(px, py, rad, p)
    }
    repeat((rw * 2.1f).toInt()) {
        val a = rng.next() * 6.2832f
        val r = rng.next().pow(0.5f)
        p.color = tones[if (rng.next() < 0.5f) 0 else 2]
        p.alpha = 128
        c.save()
        c.translate(cx + cos(a) * r * rw * 0.94f, cy + sin(a) * r * rh * 0.88f)
        c.rotate(rng.next() * 180f)
        c.drawOval(-1.4f - rng.next() * 2f, -0.9f - rng.next() * 1.3f,
            1.4f + rng.next() * 2f, 0.9f + rng.next() * 1.3f, p)
        c.restore()
        p.alpha = 255
    }
    if (fruit) repeat(8) {
        val a = rng.next() * 6.2832f
        val r = 0.3f + rng.next() * 0.6f
        val fx = cx + cos(a) * r * rw * 0.9f
        val fy = cy + sin(a) * r * rh * 0.8f
        p.color = Pal.fruit; c.drawCircle(fx, fy, 3.1f, p)
        p.color = Pal.fruitDark; c.drawCircle(fx + 0.8f, fy + 0.9f, 1.9f, p)
    }
    return b
}

/**
 * قناعُ الحافّة المضيئة: نفسُ صورة التاج مصبوغةً بلونٍ دافئٍ واحد.
 *
 * حين تقف الشمسُ خلف الشجرة يُرسم هذا القناعُ فوقها بجمعٍ ضوئيّ، فتتوهّج
 * حافّتُها كما يفعل الورقُ الحقيقيّ حين ينفذ منه الضوء. وهو — في تجربتي
 * على هذا المشهد — أكثرُ ما يفصل «رسمَ شجرة» عن «شجرة».
 */
private fun bakeRim(src: Bitmap): Bitmap {
    val b = bmp(src.width, src.height)
    val c = Canvas(b)
    c.drawBitmap(src, 0f, 0f, null)
    val p = Paint()
    p.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    p.color = Pal.rim
    c.drawRect(0f, 0f, b.width.toFloat(), b.height.toFloat(), p)
    return b
}

private fun bakeTrunk(h: Float, w0: Float, w1: Float, bend: Float): Triple<Bitmap, Float, Float> {
    val b = bmp((abs(bend) * 2 + w0 * 3 + 12).toInt(), (h + 10).toInt())
    val c = Canvas(b)
    val bx = b.width / 2f - bend / 2f
    val ay = b.height - 5f
    fun px(t: Float) = bx + bend * t * t
    fun py(t: Float) = ay - t * h
    val path = Path()
    for (i in 0..10) {
        val t = i / 10f; val w = w0 + (w1 - w0) * t
        if (i == 0) path.moveTo(px(t) - w, py(t)) else path.lineTo(px(t) - w, py(t))
    }
    for (i in 10 downTo 0) {
        val t = i / 10f; val w = w0 + (w1 - w0) * t
        path.lineTo(px(t) + w, py(t))
    }
    path.close()
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.shader = LinearGradient(bx - w0, 0f, bx + w0, 0f,
        intArrayOf(Pal.trunkDark, Pal.trunk, Pal.trunkDark), floatArrayOf(0f, 0.5f, 1f),
        Shader.TileMode.CLAMP)
    c.drawPath(path, p)
    p.shader = null
    p.style = Paint.Style.STROKE; p.strokeWidth = 1.1f
    p.color = Pal.trunkDark; p.alpha = 128
    for (i in 1 until 14) {
        val t = i / 14f; val w = w0 + (w1 - w0) * t
        c.drawLine(px(t) - w, py(t), px(t) + w, py(t) + 1f, p)
    }
    return Triple(b, bx, ay)
}

private fun makeTree(kind: Kind, h: Float, x: Float, y: Float, depth: Float,
                     fruit: Boolean, rng: Rng): Tree {
    val phase = rng.next() * 6.2832f
    return when (kind) {
        Kind.PALM -> {
            val (tb, ax, ay) = bakeTrunk(h * 0.78f, h * 0.026f, h * 0.016f, h * 0.10f * (rng.next() - 0.5f))
            val n = 11
            val fr = (0 until n).map { j ->
                Frond(
                    (PI * (0.05 + 0.90 * (j / (n - 1.0)))).toFloat() + PI.toFloat(),
                    h * (0.34f + 0.09f * sin(j * 1.9f)),
                    rng.next() * 6.2832f,
                    if (j < n / 2) -1f else 1f,
                )
            }
            Tree(kind, h, x, y, phase, depth, tb, ax, ay, null, null, fr)
        }
        Kind.CYPRESS -> {
            val (tb, ax, ay) = bakeTrunk(h * 0.20f, h * 0.022f, h * 0.018f, 0f)
            val can = bakeCanopy(h * 0.16f, h * 0.46f,
                intArrayOf(Pal.cypDark, Pal.cypMid, Pal.cypLit), false, rng)
            Tree(kind, h, x, y, phase, depth, tb, ax, ay, can, bakeRim(can), emptyList())
        }
        Kind.ROUND -> {
            val (tb, ax, ay) = bakeTrunk(h * 0.46f, h * 0.030f, h * 0.020f, h * 0.06f * (rng.next() - 0.5f))
            val can = bakeCanopy(h * 0.40f, h * 0.32f,
                intArrayOf(Pal.leafDark, Pal.leafMid, Pal.leafLit), fruit, rng)
            Tree(kind, h, x, y, phase, depth, tb, ax, ay, can, bakeRim(can), emptyList())
        }
    }
}

/* ── التخطيط ───────────────────────────────────────────── */

/**
 * البستانُ كلُّه بأحداثيّاتِ مشهدٍ عرضُه [w] وارتفاعُه [h]، لا بالبكسل.
 *
 * فيُخبز مرّةً لمقاس الشاشة، ويبقى نفسَه مهما تغيّرت الساعةُ أو الطقس.
 */
internal class Scene(val w: Float, val h: Float) {
    val horizon = h * 0.545f
    val riverTop = horizon + h * 0.056f
    val riverBottom = horizon + h * 0.204f

    val far = ArrayList<Tree>()
    val mid = ArrayList<Tree>()
    val near = ArrayList<Tree>()
    val reeds = ArrayList<Reed>()
    val tufts = ArrayList<Tuft>()
    val flowers = ArrayList<Flower>()
    val birds = ArrayList<Bird>()
    val motes = ArrayList<Mote>()
    val ducks = ArrayList<Duck>()
    val clouds = ArrayList<Cloud>()
    val stars = ArrayList<Star>()
    val drops = ArrayList<Drop>()

    /** المضيفُ والبلشونُ والمنارة — مواضعُها نسبةٌ من العرض لا رقمٌ ثابت. */
    val minaretX = w * 0.755f
    val mudhifX = w * 0.30f
    val heronX = w * 0.70f

    init {
        val s = h / 744f                       // معاملُ القياس عن مقاس التصميم
        val r = Rng(4041)
        repeat(26) {
            val hh = (40f + r.next() * 40f) * s
            val k = r.next()
            far += makeTree(
                if (k < 0.5f) Kind.ROUND else if (k < 0.8f) Kind.CYPRESS else Kind.PALM,
                hh, r.next() * w, horizon + (4f + r.next() * 14f) * s, 0.85f, false, r,
            )
        }
        repeat(11) {
            val hh = (96f + r.next() * 70f) * s
            val k = r.next()
            mid += makeTree(
                if (k < 0.5f) Kind.PALM else if (k < 0.8f) Kind.ROUND else Kind.CYPRESS,
                hh, r.next() * w, horizon + (24f + r.next() * 12f) * s, 0.5f, r.next() < 0.5f, r,
            )
        }
        near += makeTree(Kind.PALM, 336f * s, -20f * s, h + 50f * s, 0.03f, false, r)
        near += makeTree(Kind.PALM, 280f * s, w + 22f * s, h + 38f * s, 0.05f, false, r)
        near += makeTree(Kind.ROUND, 160f * s, w * 0.16f, riverBottom + 66f * s, 0.13f, true, r)
        near += makeTree(Kind.CYPRESS, 190f * s, w * 0.87f, riverBottom + 58f * s, 0.15f, false, r)
        near += makeTree(Kind.ROUND, 120f * s, w * 0.63f, riverBottom + 50f * s, 0.12f, true, r)

        val r2 = Rng(88)
        repeat(150) {
            val n = r2.next() < 0.55f
            reeds += Reed(
                r2.next() * w,
                if (n) riverBottom + (4f + r2.next() * 16f) * s else riverTop - (2f + r2.next() * 10f) * s,
                ((if (n) 26f else 14f) + r2.next() * (if (n) 26f else 13f)) * s,
                r2.next() * 6.2832f, (r2.next() - 0.5f) * 0.5f, n,
            )
        }
        val r3 = Rng(99)
        repeat(230) {
            tufts += Tuft(r3.next() * w, riverBottom + 10f * s + r3.next() * (h - riverBottom - 10f * s),
                (5f + r3.next() * 10f) * s, r3.next() * 6.2832f)
        }
        val r4 = Rng(55)
        repeat(38) {
            flowers += Flower(r4.next() * w, riverBottom + 26f * s + r4.next() * (h - riverBottom - 30f * s),
                if (r4.next() < 0.4f) 0 else if (r4.next() < 0.75f) 1 else 2, r4.next() * 6.2832f)
        }
        val r5 = Rng(7)
        repeat(7) {
            birds += Bird((32f + r5.next() * 150f) * s, (20f + r5.next() * 32f) * s,
                r5.next() * 900f, (3f + r5.next() * 3f) * s, 6f + r5.next() * 4f)
        }
        val r6 = Rng(131)
        repeat(90) {
            motes += Mote(r6.next() * w, r6.next() * h, r6.next() * 6.2832f,
                (0.7f + r6.next() * 1.5f) * s, 0.25f + r6.next() * 0.6f)
        }
        val r7 = Rng(71)
        repeat(3) {
            ducks += Duck(r7.next() * w,
                riverTop + (riverBottom - riverTop) * (0.30f + r7.next() * 0.45f),
                (if (r7.next() < 0.5f) 1f else -1f) * (4.5f + r7.next() * 6f) * s,
                r7.next() * 6.2832f)
        }
        val r8 = Rng(63)
        repeat(9) {
            clouds += Cloud(r8.next() * w, (30f * s) + r8.next() * (horizon * 0.52f),
                (54f + r8.next() * 88f) * s, 0.1f + r8.next() * 0.2f, r8.next() * 6.2832f)
        }
        val r9 = Rng(3)
        repeat(210) {
            stars += Star(r9.next() * w, r9.next() * horizon * 0.94f,
                r9.next().pow(0.4f), r9.next() * 9f)
        }
        val r10 = Rng(29)
        repeat(170) {
            drops += Drop(r10.next() * w * 1.25f - w * 0.12f, r10.next() * h,
                0.7f + r10.next() * 0.7f, (14f + r10.next() * 22f) * s)
        }
    }
}

/* ── معالمُ الهور: تُرسم مباشرةً ──────────────────────────── */

/** سعفةٌ واحدةٌ بوُريقاتٍ مسنَّنة، تُرسم في مبدأٍ منقولٍ إلى رأس الجذع. */
internal fun drawFrond(
    c: Canvas, p: Paint, f: Frond, t: Float, sway: Float, dark: Int, lit: Float,
) {
    val a = f.angle + sin(t * 1.9f + f.phase) * sway * 0.075f +
        sin(t * 4.6f + f.phase * 1.7f) * 0.028f * sway
    val n = 9
    val xs = FloatArray(n + 1); val ys = FloatArray(n + 1)
    for (i in 0..n) {
        val u = i / n.toFloat()
        val dr = a + f.side * 0.95f * u * u
        xs[i] = cos(dr) * f.len * u; ys[i] = sin(dr) * f.len * u
    }
    for (side in intArrayOf(-1, 1)) {
        val path = Path()
        path.moveTo(xs[0], ys[0])
        for (i in 1..n) {
            val u = i / n.toFloat()
            val lw = f.len * 0.17f * sin(PI.toFloat() * u.pow(0.7f)) * (1f - u * 0.2f)
            var nx = -(ys[i] - ys[i - 1]); val ny = (xs[i] - xs[i - 1])
            val m = hypot(nx, ny).takeIf { it > 0.0001f } ?: 1f
            val j = if (i % 2 == 1) 0.82f else 1f
            path.lineTo(xs[i] + side * nx / m * lw * j, ys[i] + side * ny / m * lw * j)
        }
        for (i in n downTo 0) path.lineTo(xs[i], ys[i])
        path.close()
        p.style = Paint.Style.FILL
        p.color = if (side < 0) dark else Pal.palmMid
        c.drawPath(path, p)
    }
    p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * lit; p.color = dark
    val spine = Path(); spine.moveTo(xs[0], ys[0])
    for (i in 1..n) spine.lineTo(xs[i], ys[i])
    c.drawPath(spine, p)
    p.style = Paint.Style.FILL
}

/** منارةٌ بشرفةِ أذانٍ وقبّةٍ — تقف في آخر الأفق فتُعرف المدينة. */
internal fun drawMinaret(c: Canvas, p: Paint, bx: Float, by: Float, h: Float) {
    val w = h * 0.085f
    p.style = Paint.Style.FILL
    p.color = Pal.minaret
    c.drawRect(bx - w, by - h, bx + w, by, p)
    p.color = Pal.minaretDark
    c.drawRect(bx - w * 1.5f, by - h * 0.62f, bx + w * 1.5f, by - h * 0.57f, p)
    p.color = Pal.minaret
    val cap = Path()
    cap.moveTo(bx - w * 1.35f, by - h * 0.62f); cap.lineTo(bx + w * 1.35f, by - h * 0.62f)
    cap.lineTo(bx + w, by - h * 0.70f); cap.lineTo(bx - w, by - h * 0.70f); cap.close()
    c.drawPath(cap, p)
    c.drawArc(bx - w * 1.15f, by - h - h * 0.10f, bx + w * 1.15f, by - h + h * 0.10f,
        180f, 180f, true, p)
    c.drawRect(bx - 1f, by - h - h * 0.155f, bx + 1f, by - h, p)
    c.drawCircle(bx, by - h - h * 0.17f, h * 0.018f, p)
    p.color = Pal.minaretDark
    for (i in 0..2) c.drawRect(bx - w * 0.35f, by - h * 0.52f + i * h * 0.14f,
        bx + w * 0.35f, by - h * 0.46f + i * h * 0.14f, p)
}

/**
 * المضيف: قوسُ قصبٍ مكافئٌ بأضلاعٍ ظاهرةٍ وبابٍ مقوَّس.
 *
 * وهو ما يجعل هذا المشهدَ لنا: بستانٌ فيه نخلٌ وماءٌ قد يكون في أيّ
 * مكان، أمّا المضيفُ فلا يقف إلّا على ضفّةِ هورٍ عراقيّ.
 */
internal fun drawMudhif(c: Canvas, p: Paint, bx: Float, by: Float, w: Float, h: Float) {
    val path = Path()
    path.moveTo(bx - w, by)
    for (i in 0..24) {
        val u = i / 24f
        path.lineTo(bx - w + 2 * w * u, by - h * (1f - abs(u * 2 - 1f).pow(1.7f)))
    }
    path.lineTo(bx + w, by); path.close()
    p.style = Paint.Style.FILL; p.color = Pal.mud
    c.drawPath(path, p)
    p.style = Paint.Style.STROKE; p.strokeWidth = 1.3f
    p.color = Pal.mudDark; p.alpha = 140
    for (i in 1 until 9) {
        val u = i / 9f; val px = bx - w + 2 * w * u
        c.drawLine(px, by, px, by - h * (1f - abs(u * 2 - 1f).pow(1.7f)) + 2f, p)
    }
    p.alpha = 255; p.style = Paint.Style.FILL; p.color = Pal.mudDark
    val door = Path()
    door.moveTo(bx - w * 0.19f, by); door.lineTo(bx - w * 0.19f, by - h * 0.42f)
    door.quadTo(bx, by - h * 0.62f, bx + w * 0.19f, by - h * 0.42f)
    door.lineTo(bx + w * 0.19f, by); door.close()
    c.drawPath(door, p)
}

/** المشحوف: زورقُ الأهوار — نحيلٌ ومقدّمتُه ومؤخّرتُه مرفوعتان. */
internal fun drawMashhoof(c: Canvas, p: Paint, cx: Float, cy: Float, len: Float, color: Int) {
    val path = Path()
    path.moveTo(cx - len * 0.5f, cy)
    path.quadTo(cx - len * 0.5f, cy - 6.5f, cx - len * 0.42f, cy - 8.5f)
    path.quadTo(cx - len * 0.2f, cy - 3.4f, cx, cy - 3.0f)
    path.quadTo(cx + len * 0.2f, cy - 3.4f, cx + len * 0.42f, cy - 9.5f)
    path.quadTo(cx + len * 0.5f, cy - 7.5f, cx + len * 0.5f, cy)
    path.quadTo(cx, cy + 5.2f, cx - len * 0.5f, cy)
    path.close()
    p.style = Paint.Style.FILL; p.color = color
    c.drawPath(path, p)
}

/** بلشونٌ واقفٌ في الضحضاح: ساقان وعنقٌ منحنٍ ومنقار. */
internal fun drawHeron(c: Canvas, p: Paint, bx: Float, by: Float, s: Float, color: Int) {
    p.color = color
    p.style = Paint.Style.STROKE; p.strokeCap = Paint.Cap.ROUND
    p.strokeWidth = maxOf(1f, s * 0.05f)
    c.drawLine(bx - s * 0.06f, by, bx - s * 0.06f, by - s * 0.34f, p)
    c.drawLine(bx + s * 0.07f, by, bx + s * 0.05f, by - s * 0.34f, p)
    p.style = Paint.Style.FILL
    c.save(); c.translate(bx, by - s * 0.44f); c.rotate(-9f)
    c.drawOval(-s * 0.17f, -s * 0.11f, s * 0.17f, s * 0.11f, p)
    c.restore()
    p.style = Paint.Style.STROKE; p.strokeWidth = maxOf(1f, s * 0.055f)
    val neck = Path()
    neck.moveTo(bx + s * 0.06f, by - s * 0.50f)
    neck.quadTo(bx + s * 0.22f, by - s * 0.62f, bx + s * 0.13f, by - s * 0.78f)
    c.drawPath(neck, p)
    c.drawLine(bx + s * 0.13f, by - s * 0.79f, bx + s * 0.30f, by - s * 0.83f, p)
    p.style = Paint.Style.FILL
}
