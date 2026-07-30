package app.rafiqaldhikr.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
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
