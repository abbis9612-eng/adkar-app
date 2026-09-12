package app.rafiqaldhikr.ui.hero

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp

/* ══════════════════════════════════════════════════════════════
   الأقلام الأربعة — كيف يُكتب سطرُ الشاشة الأولى

   **القاعدةُ التي تحكمها كلَّها: الحرفُ العربيُّ لا يُفصل.** هو موصولٌ
   بما قبله وبعده، ولكلّ حرفٍ **أربعُ صورٍ** حسب موضعه في الكلمة. فلو
   طار كلُّ حرفٍ وحدَه لظهرت «ا ب د أ» بدل «ابدأ» — كلمةً مكسورة. وهذا
   عائقٌ موثَّقٌ في أبحاث تحريك الخطّ العربيّ لا رأياً.

   فالكلمةُ تُرسم **كاملةً** ثمّ يمرّ عليها قناع: العينُ ترى قلماً
   يكتب، والوصلُ سليمٌ تماماً.

   **ويبقى النصُّ نصّاً.** الحركةُ كلُّها `Modifier` فوق `Text` حقيقيّ —
   فيكبر مع خطّ المستخدم، ويقرؤه التدقيقُ الصوتيّ، ويُترجَم. ولو رُسم
   صورةً لضاع الثلاثة.

   **وتُعزف مرّةً عند الفتح ثمّ تسكن.** بدايةُ الحركة هي التي تخطف
   الانتباه؛ أمّا ما يتحرّك بلا توقّفٍ فيُقرأ إعلاناً فيُهمَل.
══════════════════════════════════════════════════════════════ */

enum class HeroAnim(internal val ms: Int) {
    /** قناعٌ يمرّ من اليمين كما يمرّ القلم، وخيطٌ ذهبيٌّ يتبعه. */
    PEN(2000),

    /** يظهر غائمَ الحوافّ ثمّ ينشف ويحدّ — كحبرٍ يشرب في الورق. */
    INK(1700),

    /** يقترب من بعيدٍ قليلاً ويستقرّ. أهدؤها وأرخصُها. */
    BREATH(1900),

    /** يطلع من تحت حافّةٍ ناعمةٍ كأنّه يظهر من الخلفيّة نفسِها. */
    FOLD(1800),

    NONE(0),
    ;

    internal companion object {
        fun of(name: String): HeroAnim = when (name) {
            "ink" -> INK
            "breath" -> BREATH
            "fold" -> FOLD
            "none" -> NONE
            else -> PEN
        }
    }
}

private val Settle = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * تقدُّمُ الكتابة من صفرٍ إلى واحد. يُعاد من أوّله كلّما تغيّر `key` —
 * أي كلّما تبدّلت البطاقة.
 *
 * ومع «تقليل الحركة» يقفز إلى واحدٍ فوراً: النصُّ كاملٌ بلا حركة.
 */
@Composable
fun rememberHeroEntrance(key: Any?, anim: HeroAnim, reducedMotion: Boolean): State<Float> {
    val p = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(key, anim, reducedMotion) {
        if (reducedMotion || anim == HeroAnim.NONE) { p.snapTo(1f); return@LaunchedEffect }
        p.snapTo(0f)
        p.animateTo(1f, tween(anim.ms, easing = Settle))
    }
    return p.asState()
}

/**
 * حركةُ دخولِ السطر الكبير.
 *
 * @param p التقدُّم من [rememberHeroEntrance].
 * @param rule خيطٌ ذهبيٌّ تحت الكلمة — للقلم وحدَه.
 */
fun Modifier.heroPen(anim: HeroAnim, p: Float, rule: Color = Color(0x9EC9A227)): Modifier =
    when (anim) {
        HeroAnim.NONE -> this

        //  القناعُ يُرسم بـ`DstIn` داخل طبقةٍ مستقلّة: فلا يمسح ما تحته
        //  من الخلفيّة، ولا يحتاج صورةَ قناعٍ كما يحتاج القماشُ في المتصفّح.
        HeroAnim.PEN -> this
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val cut = (1f - p) * (size.width + 26f)
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        (cut / size.width).coerceIn(0f, 1f) to Color.Transparent,
                        ((cut + 26f) / size.width).coerceIn(0f, 1f) to Color.Black,
                        1f to Color.Black,
                    ),
                    blendMode = BlendMode.DstIn,
                )
                /*  الخيطُ **يتبع القلمَ ثمّ يذهب معه**.
                 *
                 *  كان يبقى بعد انتهاء الكتابة: عند `p = 1` يصير
                 *  `cut = 0` فيُرسم من الطرف إلى الطرف — خطٌّ ذهبيٌّ
                 *  دائمٌ تحت السطر لا أحدَ طلبه، وقد ظهر في الجهاز.
                 *  فيخفت في آخر خُمسٍ من الحركة ولا يبقى منه شيء. */
                val trail = (p / 0.02f).coerceIn(0f, 1f) *
                    ((1f - p) / 0.20f).coerceIn(0f, 1f)
                if (trail > 0f) {
                    drawLine(
                        color = rule,
                        alpha = trail,
                        start = Offset(size.width, size.height - 2f),
                        end = Offset(cut.coerceAtMost(size.width), size.height - 2f),
                        strokeWidth = 1.6f * density,
                    )
                }
            }

        //  الطمسُ يحتاج أندرويد ١٢ فأعلى؛ وتحته يبقى الظهورُ بالشفافيّة
        //  وحدَها — أهدأُ قليلاً ولا شيءَ يُكسر.
        HeroAnim.INK -> this
            .blur((10f * (1f - p)).dp)
            .graphicsLayer { alpha = p }

        HeroAnim.BREATH -> this.graphicsLayer {
            alpha = p
            val k = 1.06f - 0.06f * p
            scaleX = k; scaleY = k
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
        }

        HeroAnim.FOLD -> this
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                translationY = (1f - p) * 34f * density
                alpha = (p * 2.2f).coerceIn(0f, 1f)
            }
            .drawWithContent {
                drawContent()
                val edge = (1f - p) * 0.9f
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Black,
                        (0.55f - edge * 0.55f) to Color.Black,
                        (0.95f - edge * 0.55f).coerceAtMost(1f) to Color.Transparent,
                        1f to Color.Transparent,
                    ),
                    blendMode = BlendMode.DstIn,
                )
            }
    }
