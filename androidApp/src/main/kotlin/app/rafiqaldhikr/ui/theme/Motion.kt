package app.rafiqaldhikr.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/* ═══════════════════════════════════════════════════════════════════
   الحركة — مقصودة في مكان واحد، وتحترم إعداد المستخدم

   كان في التطبيق 20 نداء tween مقابل 5 spring، وحركة مكانية على tween
   غير قابلة للمقاطعة، وستّ حركات لانهائية تعمل معاً على الصفحة
   الرئيسية.

   والأهمّ: إعداد «تقليل الحركة» كان موجوداً في شاشة الإتاحة ولا يقرؤه
   أيّ أنيميشن في التطبيق. أي أن الإعداد كان يكذب على المستخدم — وهذا
   خلل إتاحة، لا نقص ميزة.

   القاعدة:
     • كل حركة مكانية = spring (قابلة للمقاطعة).
     • tween للّون والتقدّم العددي فقط.
     • عند تقليل الحركة ترجع كلّها snap().
═══════════════════════════════════════════════════════════════════ */

val LocalReducedMotion = compositionLocalOf { false }

/**
 * هل تُقلَّل الحركة؟ مصدران:
 *   ١) إعداد المستخدم داخل التطبيق (شاشة الإتاحة).
 *   ٢) إعداد النظام — ANIMATOR_DURATION_SCALE = 0، وهو ما يضبطه
 *      المستخدم من «خيارات المطوّر» أو من إعدادات الإتاحة في أندرويد.
 */
@Composable
fun rememberReducedMotion(userPreference: Boolean): Boolean {
    val context = LocalContext.current
    val systemOff = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    return userPreference || systemOff
}

/* ═══════════════════════════════════════════════════════════════════
   المواصفات الثلاث — لا رابعة
═══════════════════════════════════════════════════════════════════ */

/** ضغطة: ارتداد قصير محسوس. */
@Composable
@ReadOnlyComposable
fun <T> tapSpec(): FiniteAnimationSpec<T> =
    if (LocalReducedMotion.current) snap()
    else spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium)

/** دخول عنصر: هدوء بلا ارتداد ظاهر. */
@Composable
@ReadOnlyComposable
fun <T> enterSpec(): FiniteAnimationSpec<T> =
    if (LocalReducedMotion.current) snap()
    else spring(dampingRatio = 0.90f, stiffness = Spring.StiffnessLow)

/** تقدّم عددي أو تحوّل لوني — الموضع الوحيد المسموح فيه tween. */
@Composable
@ReadOnlyComposable
fun <T> progressSpec(durationMs: Int = 700): AnimationSpec<T> =
    if (LocalReducedMotion.current) snap()
    else tween(durationMs, easing = FastOutSlowInEasing)

/* ═══════════════════════════════════════════════════════════════════
   الحركات اللانهائية — تتوقّف عند تقليل الحركة

   كانت في التطبيق 19 حركة لانهائية، ولا واحدة منها تقرأ الإعداد.
   هذا البديل يرجع قيمة ثابتة عند التقليل، فتتجمّد الحركة بلا أي
   تغيير في بنية النداء.
═══════════════════════════════════════════════════════════════════ */

/**
 * قيمة متذبذبة لا نهائياً — أو ثابتة عند [LocalReducedMotion].
 *
 * @param restValue القيمة الساكنة عند التقليل. إن لم تُحدَّد:
 *        الذبذبة (Reverse) تسكن عند منتصفها — لأن طرفَيها حالتان
 *        متطرّفتان لا تصلح أيّهما وضعاً دائماً؛ والدورة (Restart)
 *        تسكن عند بدايتها.
 */
@Composable
fun stillableFloat(
    initialValue: Float,
    targetValue:  Float,
    durationMs:   Int,
    easing:       androidx.compose.animation.core.Easing = FastOutSlowInEasing,
    repeatMode:   androidx.compose.animation.core.RepeatMode =
        androidx.compose.animation.core.RepeatMode.Reverse,
    label:        String = "inf",
    restValue:    Float = Float.NaN,
): androidx.compose.runtime.State<Float> =
    if (LocalReducedMotion.current) {
        val rest = when {
            !restValue.isNaN() -> restValue
            repeatMode == androidx.compose.animation.core.RepeatMode.Reverse ->
                (initialValue + targetValue) / 2f
            else -> initialValue
        }
        androidx.compose.runtime.remember(rest) {
            androidx.compose.runtime.mutableFloatStateOf(rest)
        }
    } else {
        androidx.compose.animation.core.rememberInfiniteTransition(label = label)
            .animateFloat(
                initialValue  = initialValue,
                targetValue   = targetValue,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation  = tween(durationMs, easing = easing),
                    repeatMode = repeatMode,
                ),
                label = "${label}Value",
            )
    }
