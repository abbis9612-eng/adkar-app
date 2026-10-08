package app.rafiqaldhikr.ui.mushaf

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * شريطُ السطوع — داخل صفحة المصحف
 * ═══════════════════════════════
 *
 * من قرأ ليلاً ضربت الشاشةُ عينَه، فكان عليه أن **يخرج من التطبيق**،
 * ويسحب شريطَ الإشعارات، ويُنزل سطوعَ الجهاز كلِّه، ثمّ يرجع — ثمّ
 * يُعيدها كلَّها حين يخرج. أربعُ خطواتٍ لتُظلم صفحةً.
 *
 * ═══ سطوعُ النافذة لا سطوعُ النظام ═══
 *
 * `WindowManager.LayoutParams.screenBrightness` يسري **ما دامت هذه
 * الشاشةُ ظاهرةً** ويرجع الجهازُ إلى حاله فورَ الخروج منها. ولو غُيّر
 * سطوعُ النظام لخرج أثرُه إلى التطبيقات كلِّها ولاحتاج إذنَ
 * `WRITE_SETTINGS` — وذاك إذنٌ ثقيلٌ لأمرٍ لا يستحقّه، وليس ما يطلبه من
 * أراد أن يُظلم صفحةَ قراءته.
 *
 * ═══ ولا يُمسّ سطوعُ أحدٍ حتى يطلبه ═══
 *
 * القيمةُ الأصل سالبةٌ أي `BRIGHTNESS_OVERRIDE_NONE` — فالجهازُ على
 * حاله حتى يسحب صاحبُه الشريط. ومن سحبه مرّةً حُفظ اختيارُه فلا يُعيده
 * في كل فتحة.
 *
 * والحدُّ الأدنى ٠٫٠٥ لا صفر: الصفرُ يُطفئ الشاشةَ على بعض الأجهزة
 * فيظنّها صاحبُها تعطّلت ولا يجد الشريطَ ليرفعه.
 */

/** أدنى سطوعٍ مسموح — الصفرُ يُطفئ الشاشة على بعض الأجهزة. */
private const val MIN = 0.05f

/**
 * يُطبّق [value] على نافذة النشاط، ويرجع بالجهاز إلى حاله عند الخروج.
 *
 * @param value سالبٌ = اتبع الجهاز
 */
@Composable
fun ApplyWindowBrightness(value: Float) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    LaunchedEffect(value, activity) {
        activity?.window?.let { w ->
            w.attributes = w.attributes.apply {
                screenBrightness = if (value < 0f) {
                    android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                } else {
                    value.coerceIn(MIN, 1f)
                }
            }
        }
    }
    //  الخروجُ من الشاشة يُرجع الجهازَ إلى حاله — ولو بقي المصحفُ مظلماً
    //  لخرج ظلامُه إلى بقيّة التطبيق.
    DisposableEffect(activity) {
        onDispose {
            activity?.window?.let { w ->
                w.attributes = w.attributes.apply {
                    screenBrightness =
                        android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
            }
        }
    }
}

/**
 * الشريطُ العموديُّ على طرف الصفحة.
 *
 * @param value سالبٌ = لم يُضبط بعد؛ فيُعرض الشريطُ ممتلئاً
 */
@Composable
fun BrightnessRail(
    value: Float,
    ink: Color,
    accent: Color,
    surface: Color,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shown = if (value < 0f) 1f else value.coerceIn(MIN, 1f)
    BoxWithConstraints(
        modifier
            .width(44.dp)
            .height(168.dp)
            .background(surface.copy(alpha = 0.94f), RoundedCornerShape(22.dp))
            .border(1.dp, ink.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .padding(vertical = 10.dp, horizontal = 13.dp),
    ) {
        val hPx = with(LocalDensity.current) { maxHeight.toPx() }
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${(shown * 100).toInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = ink.copy(alpha = 0.7f),
            )
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .width(18.dp)
                    .height(118.dp)
                    .background(ink.copy(alpha = 0.10f), CircleShape)
                    .pointerInput(hPx) {
                        detectVerticalDragGestures { change, _ ->
                            //  الأعلى أسطع: موضعُ الإصبع من الأعلى مقلوباً
                            val f = 1f - (change.position.y / size.height.toFloat())
                            onChange(f.coerceIn(MIN, 1f))
                        }
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(118.dp * shown)
                        .background(accent, CircleShape),
                )
            }
        }
    }
}
