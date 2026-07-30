package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalMeeqat
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LocalReducedMotion

/**
 * شريط الميقات — عنصر التوقيع.
 *
 * خيط رفيع أسفل الرأس في كل شاشة، يمثّل اليوم من الفجر إلى العشاء:
 * الجزء المنقضي مصبوغ بضوء الوقت، وعليه علامات الصلوات الخمس،
 * وعلامة ذهبية بموضع اللحظة الحالية.
 *
 * هذا هو الموضع الوحيد في التطبيق الذي يحمل حركة لانهائية — نبض
 * علامة «الآن». وهي تتوقّف عند تفعيل «تقليل الحركة».
 *
 * لا يُرسم شيء قبل وصول المواقيت الحقيقية؛ لا نخترع وقتاً.
 */
@Composable
fun MeeqatBar(modifier: Modifier = Modifier) {
    val meeqat = LocalMeeqat.current
    val rc     = LocalRafiqColors.current
    val still  = LocalReducedMotion.current

    if (!meeqat.resolved) return

    val progress by animateFloatAsState(
        targetValue   = meeqat.dayProgress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "meeqatProgress",
    )

    val pulse by if (still) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "meeqat").animateFloat(
            initialValue  = 0.55f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse,
            ),
            label = "meeqatPulse",
        )
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(10.dp)
            .semantics {
                contentDescription = "مسار اليوم — الوقت الآن ${meeqat.phase.label}"
            }
    ) {
        val w  = size.width
        val cy = size.height / 2f
        val h  = 3.dp.toPx()
        val r  = CornerRadius(h / 2f, h / 2f)

        // المسار الكامل — اليوم كلّه
        drawRoundRect(
            color        = rc.gold.copy(alpha = 0.14f),
            topLeft      = Offset(0f, cy - h / 2f),
            size         = Size(w, h),
            cornerRadius = r,
        )

        // المنقضي — مصبوغ بضوء الوقت
        if (progress > 0.004f) {
            drawRoundRect(
                brush        = Brush.horizontalGradient(
                    listOf(rc.gold.copy(alpha = 0.45f), meeqat.tint),
                    startX = 0f, endX = (w * progress).coerceAtLeast(1f),
                ),
                topLeft      = Offset(0f, cy - h / 2f),
                size         = Size(w * progress, h),
                cornerRadius = r,
            )
        }

        // علامات الصلوات — شرطة رفيعة لكل ميقات
        meeqat.marks.forEach { mark ->
            val x = w * mark.at
            drawRoundRect(
                color        = rc.gold.copy(alpha = if (mark.at <= progress) 0.55f else 0.28f),
                topLeft      = Offset((x - 0.75.dp.toPx()).coerceIn(0f, w - 1.5.dp.toPx()), cy - h),
                size         = Size(1.5.dp.toPx(), h * 2f),
                cornerRadius = CornerRadius(1f, 1f),
            )
        }

        // علامة «الآن» — نقطة ذهبية بهالة تتنفّس
        val nx = (w * progress).coerceIn(3.dp.toPx(), w - 3.dp.toPx())
        drawCircle(rc.gold.copy(alpha = 0.22f * pulse), 5.dp.toPx(), Offset(nx, cy))
        drawCircle(rc.gold, 2.5.dp.toPx(), Offset(nx, cy))
        drawCircle(Color.White.copy(alpha = 0.55f), 1.dp.toPx(), Offset(nx, cy))
    }
}
