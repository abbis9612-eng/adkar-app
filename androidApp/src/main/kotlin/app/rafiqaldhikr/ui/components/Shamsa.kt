package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * حالة الشَّمْسة — نفس المفردة تُستخدم لمحطّات اليوم (وردية ثمانية) وللفئات (دائرة مصمتة).
 * [Neutral] هي حالة الفئات التي لا تحمل معنى "تقدّم" — تُرسم دائرة متدرّجة بلا بتلات،
 * حفاظاً على الشكل المعتمد للفئات دون تغيير بصري غير ضروري.
 */
enum class ShamsaState { Done, Now, Upcoming, Neutral }

/**
 * الشَّمْسة — المفردة التوقيعية الثانية من نظام التذهيب (بعد العُنوان والجَدْوَل).
 * وردية ثمانية البتلات حول قرص مركزي، تُستخدم كعلامة حالة (محطّات اليوم، الميقات)
 * أو كبَدَل موحَّد لِـ CategoryBadge السابق (بحالة [ShamsaState.Neutral]).
 */
@Composable
fun Shamsa(
    state: ShamsaState,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    icon: (@Composable () -> Unit)? = null,
) {
    val rc = LocalRafiqColors.current
    val infinitePulse by rememberInfiniteTransition(label = "shamsa").animateFloat(
        0.35f, 0.85f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "shamsaPulse",
    )
    val pulse = if (state == ShamsaState.Now) infinitePulse else 0f

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (state == ShamsaState.Neutral) {
            Box(
                Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF23976E), Color(0xFF0C5E43)))),
                contentAlignment = Alignment.Center,
            ) {
                icon?.invoke()
            }
            return@Box
        }

        Canvas(Modifier.size(size)) {
            when (state) {
                ShamsaState.Now -> {
                    drawCircle(rc.emerald.copy(alpha = pulse * 0.4f), radius = this.size.minDimension * 0.62f)
                    drawShamsaPetals(fillColor = rc.emerald, strokeColor = rc.emerald, filled = true)
                    drawShamsaCore(fillColor = rc.emerald, strokeColor = rc.gold, filled = true, ringWidth = 1.6.dp)
                }
                ShamsaState.Done -> {
                    drawShamsaPetals(fillColor = rc.gold, strokeColor = rc.gold, filled = true)
                    drawShamsaCore(fillColor = rc.gold, strokeColor = rc.gold, filled = true)
                }
                ShamsaState.Upcoming -> {
                    drawShamsaPetals(fillColor = rc.inkLight, strokeColor = rc.inkLight, filled = false, alpha = 0.45f)
                    drawShamsaCore(fillColor = Color.Transparent, strokeColor = rc.inkLight, filled = false, alpha = 0.45f)
                }
                ShamsaState.Neutral -> Unit
            }
        }
        icon?.invoke()
    }
}

private fun DrawScope.drawShamsaPetals(
    fillColor: Color,
    strokeColor: Color,
    filled: Boolean,
    alpha: Float = 0.9f,
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val half = size.minDimension / 2f
    val r1 = half * 0.35f
    val r2 = half * 0.65f
    val r3 = half * 0.90f
    for (k in 0 until 8) {
        val a = (k * kotlin.math.PI / 4).toFloat()
        val start = Offset(cx + cos(a) * r1, cy + sin(a) * r1)
        val tipA = a + 0.30f
        val tipB = a - 0.30f
        val tip = Offset(cx + cos(a) * r3, cy + sin(a) * r3)
        val ctrl1 = Offset(cx + cos(tipA) * r2, cy + sin(tipA) * r2)
        val ctrl2 = Offset(cx + cos(tipB) * r2, cy + sin(tipB) * r2)
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticBezierTo(ctrl1.x, ctrl1.y, tip.x, tip.y)
            quadraticBezierTo(ctrl2.x, ctrl2.y, start.x, start.y)
            close()
        }
        if (filled) {
            drawPath(path, color = fillColor, alpha = alpha)
        } else {
            drawPath(path, color = strokeColor, alpha = alpha, style = Stroke(width = 0.8.dp.toPx()))
        }
    }
}

private fun DrawScope.drawShamsaCore(
    fillColor: Color,
    strokeColor: Color,
    filled: Boolean,
    alpha: Float = 1f,
    ringWidth: Dp = 0.dp,
) {
    val r = size.minDimension * 0.30f
    val center = Offset(size.width / 2f, size.height / 2f)
    if (filled) {
        drawCircle(fillColor, radius = r, center = center, alpha = alpha)
    }
    drawCircle(strokeColor, radius = r, center = center, alpha = alpha, style = Stroke(width = 1.1.dp.toPx()))
    if (ringWidth > 0.dp) {
        drawCircle(strokeColor, radius = r + ringWidth.toPx(), center = center, style = Stroke(width = ringWidth.toPx() * 0.5f))
    }
}
