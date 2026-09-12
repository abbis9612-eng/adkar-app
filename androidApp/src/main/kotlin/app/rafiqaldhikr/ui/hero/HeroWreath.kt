package app.rafiqaldhikr.ui.hero

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import app.rafiqaldhikr.R
import kotlin.math.pow
import kotlin.math.sin

/* ══════════════════════════════════════════════════════════════
   الإكليل — غصنا زيتونٍ ينحنيان

   **الرسمُ رسمُك، ونحن نُحرّكه فقط.** جُرّب أن يُرسم الغصنُ بمعادلةٍ
   فخرج مخطَّطاً لا فنّاً — لا ظلَّ ولا تداخلَ ورقٍ ولا تنوّعَ لون —
   وقلتَها صريحةً: «جودة الغصن جدّاً بدائيّة». فالصوابُ ألّا يُستبدل
   الرسمُ بل يُنحنى.

   **الغصنُ ذراعٌ مثبّتةٌ من مغرزها:** ما قرُب منه لا يكاد يتحرّك وما
   بعُد ينحني أكثر. فتُقسَّم الصورةُ ستّةَ عشرَ حزاماً حَلَقيّاً حسب
   البُعد عن المغرز، ويُدار كلُّ حزامٍ حوله بزاويةٍ تكبر مع بُعده —
   فيخرج انحناءٌ **متّصلٌ لا كَسر**، والفنُّ باقٍ بظلاله كلِّها.
   وتتداخل الأحزمةُ أربعةَ عشرَ بكسلاً فلا يظهر حدٌّ بينها.

   واللونان من صورةٍ واحدة: طبقةٌ لكلّ غصنٍ ثمّ مستطيلٌ بـ`SRC_ATOP`
   يمسّ ما رُسم ولا يتعدّاه — الأماميُّ يدفأ بضوء النهار، والخلفيُّ
   يزرقّ ويغمق فيتأخّر إلى الخلف، وذاك ما يفعله الهواءُ بين جسمين
   متباعدين.

   **والدقّة:** الملفُّ ٤٨٧×٦٢٨ بكسلاً حقيقيّة بعد قصّ الفراغ الشفّاف
   (كان ٧٦٠×٧٦٠ نصفُه خالٍ). والغصنُ يُرسم بعرض ١٥٢dp، أي ٤٥٨ بكسلاً
   على هاتفٍ ٣× — **أقلُّ من الأصل**، فهو تصغيرٌ حادٌّ لا تكبيرٌ يميّع.
══════════════════════════════════════════════════════════════ */

/*  الإحداثيّاتُ من النموذج المعتمَد، ومرجعُها لوحٌ عرضُه ٤٠٠ — تُضرب
 *  في `size.width / 400` فتتبع كلَّ عرضِ شاشة. */
private const val REF_W = 400f

private const val BANDS = 16
private const val ANCHOR_U = 34f / 760f      // مغرزُ الغصن في الصورة
private const val ANCHOR_V = -16f / 760f
private const val DMAX_U = 1080f / 760f      // أبعدُ نقطةٍ عن المغرز
private const val ART_W = 487f / 760f        // ما تشغله الصورةُ من المربّع
private const val ART_H = 628f / 760f
private const val OVERLAP = 14f              // تداخلُ الأحزمة، فلا حدَّ يظهر

/** غصنٌ واحد. [x] و[y] و[w] بمقياس اللوح ٤٠٠. */
private class Branch(
    val x: Float, val y: Float, val w: Float,
    val flip: Boolean, val rot: Float,
    val alpha: Float, val phase: Float, val tint: Int,
)

//  الخلفيُّ أوّلاً ثمّ الأماميُّ فوقه — والفجوةُ بينهما متنفَّسُ الإكليل.
private val WREATH = listOf(
    Branch(424f, -30f, 238f, flip = true,  rot = -0.10f, alpha = 0.80f, phase = 3.4f, tint = 0x571E3C4C),
    Branch(-24f, -30f, 238f, flip = false, rot = 0.08f,  alpha = 0.92f, phase = 0f,   tint = 0x1FFFECBE),
)

private const val WIND = 0.5f

//  يُبنى مرّةً لا في كلّ إطار.
private val ATOP = android.graphics.PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

/**
 * يُرسم **فوق حجاب [HeroBackdrop] لا تحته**: الحجابُ يُطفئ ما تحته
 * ليُقرأ الكلام، وقد ابتلع الغصنَ حين كان أسفلَه.
 */
@Composable
fun HeroWreath(reducedMotion: Boolean, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val art: Bitmap? = remember {
        runCatching { BitmapFactory.decodeResource(ctx.resources, R.drawable.olive) }.getOrNull()
    }
    art ?: return

    val wind = if (reducedMotion) 0f else WIND
    //  ساعةُ الإطار لا مؤقّتٌ دوريّ: تتوقّف حين تخرج الشاشةُ من التركيب،
    //  وتحترم تعطيلَ الحركة في إعدادات النظام.
    val t by produceState(0f, wind) {
        if (wind == 0f) return@produceState
        val t0 = withInfiniteAnimationFrameMillis { it }
        while (true) withInfiniteAnimationFrameMillis { value = (it - t0) / 1000f }
    }

    val img = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true } }
    val skin = remember { Paint() }
    val dst = remember { RectF() }
    val ring = remember { android.graphics.Path().apply { fillType = android.graphics.Path.FillType.EVEN_ODD } }

    Canvas(modifier) { drawWreath(art, img, skin, dst, ring, t, wind) }
}

private fun DrawScope.drawWreath(
    art: Bitmap,
    img: Paint,
    skin: Paint,
    dst: RectF,
    ring: android.graphics.Path,
    t: Float,
    wind: Float,
) {
    val u = size.width / REF_W
    drawIntoCanvas { c ->
        val nc = c.nativeCanvas
        for (g in WREATH) {
            val x = g.x * u
            val y = g.y * u
            val w = g.w * u
            val ax = x + (if (g.flip) -1f else 1f) * ANCHOR_U * w
            val ay = y + ANCHOR_V * w
            val dmax = DMAX_U * w

            val layer = nc.saveLayerAlpha(0f, 0f, size.width, size.height, (g.alpha * 255).toInt())
            dst.set(0f, 0f, ART_W * w, ART_H * w)

            fun place(deg: Float) {
                nc.rotate(deg, ax, ay)
                nc.translate(x, y)
                if (g.flip) nc.scale(-1f, 1f)
                nc.drawBitmap(art, null, dst, img)
            }

            if (wind == 0f) {
                //  بلا ريحٍ لا حاجةَ إلى الأحزمة: رسمةٌ واحدةٌ بلا قصٍّ ولا حوافّ.
                nc.save()
                place(Math.toDegrees(g.rot.toDouble()).toFloat())
                nc.restore()
            } else {
                for (k in 0 until BANDS) {
                    val d0 = (k.toFloat() / BANDS) * dmax
                    val d1 = ((k + 1f) / BANDS) * dmax + OVERLAP * u
                    val q = (k + 0.5f) / BANDS
                    //  الأساسُ يميل ببطء، والطرفُ يرفرف فوقه بترددٍ أعلى — كالغصن
                    val th = wind * (
                        0.055f * q.pow(1.7f) * sin(t * 0.9f + g.phase) +
                            0.022f * q.pow(2.6f) * sin(t * 2.3f + g.phase * 1.7f)
                        )
                    nc.save()
                    ring.rewind()
                    ring.addCircle(ax, ay, d1, android.graphics.Path.Direction.CW)
                    ring.addCircle(ax, ay, d0, android.graphics.Path.Direction.CW)
                    nc.clipPath(ring)
                    place(Math.toDegrees((th + g.rot).toDouble()).toFloat())
                    nc.restore()
                }
            }

            //  الصبغة: تمسّ ما رُسم في هذه الطبقة ولا تتعدّاه
            skin.color = g.tint
            skin.xfermode = ATOP
            nc.drawRect(0f, 0f, size.width, size.height, skin)
            skin.xfermode = null
            nc.restoreToCount(layer)
        }
    }
}
