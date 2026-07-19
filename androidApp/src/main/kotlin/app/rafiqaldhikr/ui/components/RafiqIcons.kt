package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * ═══════════════════════════════════════════════════════════════
 *  RafiqIcons — نظام الأيقونات «Rafiq Duotone»
 *
 *  لغة بصرية واحدة فاخرة لكل التطبيق:
 *   • جسم ممتلئ بتدرّج لوني ناعم (من اللون → أفتح) يعطي عمقاً
 *   • حدود واضحة بأطراف دائرية بسماكة w*0.075
 *   • لمسة إبراز صغيرة (نقطة/خط) تضيف حياة
 *  الألوان دائماً من RafiqPalette (تتكيّف مع الوضع الفاتح/الداكن).
 * ═══════════════════════════════════════════════════════════════
 */

/** تعبئة جسم الأيقونة بتدرّج ناعم — أساس أسلوب Duotone. */
private fun DrawScope.duoFill(path: Path, c: Color) {
    drawPath(path, Brush.verticalGradient(listOf(c.copy(alpha = 0.26f), c.copy(alpha = 0.07f))))
}

/** جسم ممتلئ + حدّ واضح — الوصفة الأساسية. */
private fun DrawScope.duo(path: Path, c: Color, sw: Float) {
    duoFill(path, c)
    drawPath(path, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/* ══════════ شريط التنقل والأساسيات ══════════ */

@Composable
fun IcoHome(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        // جسم البيت بقوس إسلامي مدبّب
        val body = Path().apply {
            moveTo(w * 0.20f, h * 0.86f)
            lineTo(w * 0.20f, h * 0.48f)
            cubicTo(w * 0.28f, h * 0.34f, w * 0.42f, h * 0.20f, w * 0.50f, h * 0.16f)
            cubicTo(w * 0.58f, h * 0.20f, w * 0.72f, h * 0.34f, w * 0.80f, h * 0.48f)
            lineTo(w * 0.80f, h * 0.86f)
            close()
        }
        duo(body, c, sw)
        // باب بقوس
        val door = Path().apply {
            moveTo(w * 0.43f, h * 0.86f); lineTo(w * 0.43f, h * 0.64f)
            cubicTo(w * 0.43f, h * 0.55f, w * 0.57f, h * 0.55f, w * 0.57f, h * 0.64f)
            lineTo(w * 0.57f, h * 0.86f)
        }
        drawPath(door, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // هلال صغير أعلى القبة
        drawCircle(c, w * 0.035f, Offset(w * 0.50f, h * 0.10f))
    }
}

@Composable
fun IcoQuran(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        // غلاف المصحف
        val cover = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    w * 0.20f, h * 0.16f, w * 0.80f, h * 0.84f,
                    CornerRadius(w * 0.10f, w * 0.10f)
                )
            )
        }
        duo(cover, c, sw)
        // كعب المصحف
        drawLine(c, Offset(w * 0.50f, h * 0.20f), Offset(w * 0.50f, h * 0.80f),
            sw * 0.7f, StrokeCap.Round)
        // نجمة ثمانية ذهبية بالوسط
        val star = starPath(w * 0.50f, h * 0.50f, w * 0.15f, w * 0.075f, 8)
        drawPath(star, c, style = Stroke(w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IcoMisbaha(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val cx = w * 0.50f; val cy = h * 0.44f; val r = w * 0.27f
        // حبات ممتلئة حول حلقة، مع فتحة أسفل للشرّابة
        for (i in 0 until 9) {
            if (i == 5) continue
            val a = (i * 40 - 90) * PI.toFloat() / 180f
            val bx = cx + r * cos(a); val by = cy + r * sin(a)
            drawCircle(c.copy(alpha = 0.22f), w * 0.075f, Offset(bx, by))
            drawCircle(c, w * 0.06f, Offset(bx, by))
        }
        // الشرّابة
        drawLine(c, Offset(cx, cy + r * 0.9f), Offset(cx, h * 0.82f), w * 0.055f, StrokeCap.Round)
        val tassel = Path().apply {
            moveTo(cx, h * 0.82f)
            cubicTo(w * 0.42f, h * 0.90f, w * 0.58f, h * 0.90f, cx, h * 0.94f)
        }
        duoFill(tassel, c)
        drawCircle(c, w * 0.05f, Offset(cx, h * 0.88f))
    }
}

@Composable
fun IcoHeart(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald, filled: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        val p = heartPath(w, h)
        if (filled) {
            drawPath(p, Brush.verticalGradient(listOf(c, c.copy(alpha = 0.72f))))
            drawPath(p, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else duo(p, c, sw)
    }
}

@Composable
fun IcoPerson(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        val head = Path().apply { addOval(Rect(Offset(w * 0.50f, h * 0.30f), w * 0.15f)) }
        duo(head, c, sw)
        val body = Path().apply {
            moveTo(w * 0.19f, h * 0.84f)
            cubicTo(w * 0.19f, h * 0.58f, w * 0.81f, h * 0.58f, w * 0.81f, h * 0.84f)
            close()
        }
        duo(body, c, sw)
    }
}

/* ══════════ تصنيفات دلالية فاخرة ══════════ */

@Composable
fun IcoFood(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        // صحن مغطّى (dome)
        val dome = Path().apply {
            moveTo(w * 0.16f, h * 0.62f)
            cubicTo(w * 0.16f, h * 0.34f, w * 0.84f, h * 0.34f, w * 0.84f, h * 0.62f)
            close()
        }
        duo(dome, c, sw)
        drawLine(c, Offset(w * 0.10f, h * 0.62f), Offset(w * 0.90f, h * 0.62f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.18f, h * 0.72f), Offset(w * 0.82f, h * 0.72f), sw, StrokeCap.Round)
        // مقبض علوي
        drawCircle(c, w * 0.045f, Offset(w * 0.50f, h * 0.30f))
    }
}

@Composable
fun IcoHealth(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val heart = heartPath(w, h)
        duo(heart, c, sw)
        // نبضة بيضاء واضحة داخل القلب
        val pulse = Path().apply {
            moveTo(w * 0.26f, h * 0.50f); lineTo(w * 0.40f, h * 0.50f); lineTo(w * 0.46f, h * 0.40f)
            lineTo(w * 0.54f, h * 0.60f); lineTo(w * 0.60f, h * 0.50f); lineTo(w * 0.74f, h * 0.50f)
        }
        drawPath(pulse, c, style = Stroke(w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IcoSunset(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.eveningRing) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        // قرص الشمس نصفه فوق الأفق — ممتلئ بتدرّج
        val sun = Path().apply {
            addArc(Rect(Offset(w * 0.30f, h * 0.28f), Offset(w * 0.70f, h * 0.68f)), 180f, 180f)
            close()
        }
        duoFill(sun, c)
        drawArc(c, 180f, 180f, false, Offset(w * 0.30f, h * 0.28f), Size(w * 0.40f, w * 0.40f),
            style = Stroke(sw, cap = StrokeCap.Round))
        for (i in 0 until 5) {
            val a = (180 + i * 45) * (PI.toFloat() / 180f)
            val cx = w * 0.5f; val cy = h * 0.48f
            drawLine(c, Offset(cx + w * 0.26f * cos(a), cy + w * 0.26f * sin(a)),
                Offset(cx + w * 0.35f * cos(a), cy + w * 0.35f * sin(a)), sw, StrokeCap.Round)
        }
        drawLine(c, Offset(w * 0.10f, h * 0.48f), Offset(w * 0.90f, h * 0.48f), sw, StrokeCap.Round)
        // سهم نزول
        drawLine(c, Offset(w * 0.5f, h * 0.64f), Offset(w * 0.5f, h * 0.82f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.42f, h * 0.74f), Offset(w * 0.5f, h * 0.82f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.58f, h * 0.74f), Offset(w * 0.5f, h * 0.82f), sw, StrokeCap.Round)
    }
}

@Composable
fun IcoCompass(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald, off: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val ring = Path().apply { addOval(Rect(Offset(w * 0.14f, h * 0.14f), Offset(w * 0.86f, h * 0.86f))) }
        duo(ring, c, sw)
        // إبرة معيّنية
        val needle = Path().apply {
            moveTo(w * 0.50f, h * 0.24f); lineTo(w * 0.60f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.76f); lineTo(w * 0.40f, h * 0.50f); close()
        }
        drawPath(needle, c)
        drawCircle(c, w * 0.05f, Offset(w * 0.50f, h * 0.50f))
        if (off) drawLine(c, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), sw, StrokeCap.Round)
    }
}

/* ══════════ أدوات (أخف — للأزرار) ══════════ */

@Composable
fun IcoSearch(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.08f
        val lens = Path().apply { addOval(Rect(Offset(w * 0.18f, h * 0.18f), Offset(w * 0.66f, h * 0.66f))) }
        duoFill(lens, c)
        drawCircle(c, w * 0.24f, Offset(w * 0.42f, h * 0.42f), style = Stroke(sw, cap = StrokeCap.Round))
        drawLine(c, Offset(w * 0.60f, h * 0.60f), Offset(w * 0.84f, h * 0.84f), sw, StrokeCap.Round)
    }
}

@Composable
fun IcoShare(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val a = Offset(w * 0.28f, h * 0.50f); val b = Offset(w * 0.72f, h * 0.24f); val d = Offset(w * 0.72f, h * 0.76f)
        drawLine(c, a, b, sw, StrokeCap.Round)
        drawLine(c, a, d, sw, StrokeCap.Round)
        for (pt in listOf(a, b, d)) {
            drawCircle(c.copy(alpha = 0.22f), w * 0.11f, pt)
            drawCircle(c, w * 0.06f, pt)
        }
    }
}

@Composable
fun IcoCopy(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val back = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(w * 0.32f, h * 0.14f, w * 0.82f, h * 0.68f, CornerRadius(w * 0.08f, w * 0.08f))) }
        drawPath(back, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val front = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(w * 0.18f, h * 0.32f, w * 0.68f, h * 0.86f, CornerRadius(w * 0.08f, w * 0.08f))) }
        duo(front, c, sw)
    }
}

@Composable
fun IcoBookmark(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold, filled: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        val p = Path().apply {
            moveTo(w * 0.28f, h * 0.16f); lineTo(w * 0.72f, h * 0.16f); lineTo(w * 0.72f, h * 0.84f)
            lineTo(w * 0.50f, h * 0.66f); lineTo(w * 0.28f, h * 0.84f); close()
        }
        if (filled) {
            drawPath(p, Brush.verticalGradient(listOf(c, c.copy(alpha = 0.7f))))
            drawPath(p, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        } else duo(p, c, sw)
    }
}

@Composable
fun IcoTrash(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.error) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val body = Path().apply {
            moveTo(w * 0.28f, h * 0.30f); lineTo(w * 0.32f, h * 0.84f)
            lineTo(w * 0.68f, h * 0.84f); lineTo(w * 0.72f, h * 0.30f); close()
        }
        duo(body, c, sw)
        drawLine(c, Offset(w * 0.20f, h * 0.28f), Offset(w * 0.80f, h * 0.28f), sw, StrokeCap.Round)
        val handle = Path().apply {
            moveTo(w * 0.42f, h * 0.28f); lineTo(w * 0.44f, h * 0.16f)
            lineTo(w * 0.56f, h * 0.16f); lineTo(w * 0.58f, h * 0.28f)
        }
        drawPath(handle, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(c, Offset(w * 0.44f, h * 0.42f), Offset(w * 0.45f, h * 0.72f), sw * 0.8f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.56f, h * 0.42f), Offset(w * 0.55f, h * 0.72f), sw * 0.8f, StrokeCap.Round)
    }
}

@Composable
fun IcoRefresh(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.08f
        drawArc(c, -45f, 270f, false, Offset(w * 0.20f, h * 0.20f), Size(w * 0.60f, h * 0.60f),
            style = Stroke(sw, cap = StrokeCap.Round))
        drawLine(c, Offset(w * 0.71f, h * 0.29f), Offset(w * 0.86f, h * 0.31f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.71f, h * 0.29f), Offset(w * 0.73f, h * 0.13f), sw, StrokeCap.Round)
    }
}

@Composable
fun IcoPlus(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawLine(c, Offset(w * 0.50f, h * 0.20f), Offset(w * 0.50f, h * 0.80f), w * 0.09f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.20f, h * 0.50f), Offset(w * 0.80f, h * 0.50f), w * 0.09f, StrokeCap.Round)
    }
}

@Composable
fun IcoClose(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.inkMed) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawLine(c, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), w * 0.09f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), w * 0.09f, StrokeCap.Round)
    }
}

@Composable
fun IcoSend(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val p = Path().apply {
            moveTo(w * 0.14f, h * 0.46f); lineTo(w * 0.86f, h * 0.16f)
            lineTo(w * 0.58f, h * 0.86f); lineTo(w * 0.46f, h * 0.56f); close()
        }
        duo(p, c, sw)
        drawLine(c, Offset(w * 0.46f, h * 0.56f), Offset(w * 0.86f, h * 0.16f), sw, StrokeCap.Round)
    }
}

@Composable
fun IcoMail(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val env = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(w * 0.14f, h * 0.24f, w * 0.86f, h * 0.76f, CornerRadius(w * 0.08f, w * 0.08f))) }
        duo(env, c, sw)
        val flap = Path().apply {
            moveTo(w * 0.17f, h * 0.30f); lineTo(w * 0.50f, h * 0.54f); lineTo(w * 0.83f, h * 0.30f)
        }
        drawPath(flap, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ══════════ حالات ورسائل ══════════ */

@Composable
fun IcoWarning(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.warning) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        val tri = Path().apply {
            moveTo(w * 0.50f, h * 0.14f); lineTo(w * 0.90f, h * 0.82f); lineTo(w * 0.10f, h * 0.82f); close()
        }
        duo(tri, c, sw)
        drawLine(c, Offset(w * 0.50f, h * 0.40f), Offset(w * 0.50f, h * 0.60f), sw, StrokeCap.Round)
        drawCircle(c, w * 0.05f, Offset(w * 0.50f, h * 0.71f))
    }
}

@Composable
fun IcoAlert(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.error) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.075f
        val ring = Path().apply { addOval(Rect(Offset(w * 0.14f, h * 0.14f), Offset(w * 0.86f, h * 0.86f))) }
        duo(ring, c, sw)
        drawLine(c, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.50f, h * 0.55f), sw, StrokeCap.Round)
        drawCircle(c, w * 0.05f, Offset(w * 0.50f, h * 0.68f))
    }
}

@Composable
fun IcoInbox(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.inkLight) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val box = Path().apply {
            moveTo(w * 0.14f, h * 0.52f); lineTo(w * 0.24f, h * 0.24f); lineTo(w * 0.76f, h * 0.24f)
            lineTo(w * 0.86f, h * 0.52f); lineTo(w * 0.86f, h * 0.80f); lineTo(w * 0.14f, h * 0.80f); close()
        }
        duo(box, c, sw)
        val tray = Path().apply {
            moveTo(w * 0.14f, h * 0.52f); lineTo(w * 0.34f, h * 0.52f); lineTo(w * 0.40f, h * 0.62f)
            lineTo(w * 0.60f, h * 0.62f); lineTo(w * 0.66f, h * 0.52f); lineTo(w * 0.86f, h * 0.52f)
        }
        drawPath(tray, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ══════════ موقع ══════════ */

@Composable
fun IcoPin(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald, off: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val pin = Path().apply {
            moveTo(w * 0.50f, h * 0.86f)
            cubicTo(w * 0.24f, h * 0.60f, w * 0.22f, h * 0.44f, w * 0.22f, h * 0.38f)
            cubicTo(w * 0.22f, h * 0.20f, w * 0.78f, h * 0.20f, w * 0.78f, h * 0.38f)
            cubicTo(w * 0.78f, h * 0.44f, w * 0.76f, h * 0.60f, w * 0.50f, h * 0.86f)
            close()
        }
        duo(pin, c, sw)
        drawCircle(c, w * 0.08f, Offset(w * 0.50f, h * 0.38f))
        if (off) drawLine(c, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), sw, StrokeCap.Round)
    }
}

/* ══════════ بيانات ══════════ */

@Composable
fun IcoDownload(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.08f
        drawLine(c, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.60f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.34f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.66f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), sw, StrokeCap.Round)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.66f); lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f); lineTo(w * 0.82f, h * 0.66f)
        }
        drawPath(tray, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IcoUpload(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.08f
        drawLine(c, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.50f, h * 0.16f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.34f, h * 0.30f), Offset(w * 0.50f, h * 0.14f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.66f, h * 0.30f), Offset(w * 0.50f, h * 0.14f), sw, StrokeCap.Round)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.66f); lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f); lineTo(w * 0.82f, h * 0.66f)
        }
        drawPath(tray, c, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ══════════ إنجاز ══════════ */

@Composable
fun IcoFlame(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.accentOrange) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val p = Path().apply {
            moveTo(w * 0.50f, h * 0.10f)
            cubicTo(w * 0.64f, h * 0.28f, w * 0.82f, h * 0.42f, w * 0.82f, h * 0.60f)
            cubicTo(w * 0.82f, h * 0.79f, w * 0.67f, h * 0.90f, w * 0.50f, h * 0.90f)
            cubicTo(w * 0.33f, h * 0.90f, w * 0.18f, h * 0.79f, w * 0.18f, h * 0.60f)
            cubicTo(w * 0.18f, h * 0.47f, w * 0.30f, h * 0.36f, w * 0.38f, h * 0.26f)
            cubicTo(w * 0.43f, h * 0.20f, w * 0.47f, h * 0.16f, w * 0.50f, h * 0.10f)
            close()
        }
        duo(p, c, sw)
        val inner = Path().apply {
            moveTo(w * 0.50f, h * 0.54f)
            cubicTo(w * 0.60f, h * 0.62f, w * 0.62f, h * 0.72f, w * 0.56f, h * 0.78f)
            cubicTo(w * 0.50f, h * 0.83f, w * 0.42f, h * 0.79f, w * 0.42f, h * 0.70f)
        }
        drawPath(inner, c, style = Stroke(w * 0.055f, cap = StrokeCap.Round))
    }
}

@Composable
fun IcoTrophy(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val cup = Path().apply {
            moveTo(w * 0.30f, h * 0.16f); lineTo(w * 0.70f, h * 0.16f); lineTo(w * 0.67f, h * 0.44f)
            cubicTo(w * 0.63f, h * 0.58f, w * 0.37f, h * 0.58f, w * 0.33f, h * 0.44f)
            close()
        }
        duo(cup, c, sw)
        drawArc(c, -90f, -160f, false, Offset(w * 0.12f, h * 0.18f), Size(w * 0.20f, h * 0.24f),
            style = Stroke(sw, cap = StrokeCap.Round))
        drawArc(c, -90f, 160f, false, Offset(w * 0.68f, h * 0.18f), Size(w * 0.20f, h * 0.24f),
            style = Stroke(sw, cap = StrokeCap.Round))
        drawLine(c, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.50f, h * 0.70f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.36f, h * 0.76f), Offset(w * 0.64f, h * 0.76f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.30f, h * 0.84f), Offset(w * 0.70f, h * 0.84f), sw, StrokeCap.Round)
    }
}

@Composable
fun IcoBulb(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        val glass = Path().apply { addOval(Rect(Offset(w * 0.28f, h * 0.16f), Offset(w * 0.72f, h * 0.60f))) }
        duo(glass, c, sw)
        drawLine(c, Offset(w * 0.42f, h * 0.60f), Offset(w * 0.42f, h * 0.74f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.58f, h * 0.60f), Offset(w * 0.58f, h * 0.74f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.42f, h * 0.74f), Offset(w * 0.58f, h * 0.74f), sw, StrokeCap.Round)
        drawLine(c, Offset(w * 0.45f, h * 0.82f), Offset(w * 0.55f, h * 0.82f), sw, StrokeCap.Round)
    }
}

/* ══════════ مسجد (نسخة فاخرة تُستخدم بالتصنيفات) ══════════ */

@Composable
fun IcoMosque(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val sw = w * 0.07f
        // قبة + جسم المسجد ممتلئ
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.86f)
            lineTo(w * 0.22f, h * 0.52f)
            cubicTo(w * 0.22f, h * 0.34f, w * 0.78f, h * 0.34f, w * 0.78f, h * 0.52f)
            lineTo(w * 0.78f, h * 0.86f)
            close()
        }
        duo(body, c, sw)
        drawLine(c, Offset(w * 0.14f, h * 0.86f), Offset(w * 0.86f, h * 0.86f), sw, StrokeCap.Round)
        // هلال أعلى القبة
        drawLine(c, Offset(w * 0.50f, h * 0.34f), Offset(w * 0.50f, h * 0.22f), sw, StrokeCap.Round)
        drawCircle(c, w * 0.045f, Offset(w * 0.50f, h * 0.17f))
        // باب بقوس (فتحة)
        val door = Path().apply {
            moveTo(w * 0.43f, h * 0.86f); lineTo(w * 0.43f, h * 0.66f)
            cubicTo(w * 0.43f, h * 0.57f, w * 0.57f, h * 0.57f, w * 0.57f, h * 0.66f)
            lineTo(w * 0.57f, h * 0.86f)
        }
        drawPath(door, c, style = Stroke(sw * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ══════════ أدوات هندسية مشتركة ══════════ */

private fun heartPath(w: Float, h: Float): Path = Path().apply {
    moveTo(w * 0.50f, h * 0.82f)
    cubicTo(w * 0.14f, h * 0.54f, w * 0.16f, h * 0.22f, w * 0.38f, h * 0.22f)
    cubicTo(w * 0.46f, h * 0.22f, w * 0.50f, h * 0.30f, w * 0.50f, h * 0.34f)
    cubicTo(w * 0.50f, h * 0.30f, w * 0.54f, h * 0.22f, w * 0.62f, h * 0.22f)
    cubicTo(w * 0.84f, h * 0.22f, w * 0.86f, h * 0.54f, w * 0.50f, h * 0.82f)
    close()
}

/** نجمة إسلامية بعدد رؤوس محدد (للمصحف والزخارف). */
private fun starPath(cx: Float, cy: Float, outer: Float, inner: Float, points: Int): Path = Path().apply {
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outer else inner
        val a = (i * (180f / points) - 90f) * PI.toFloat() / 180f
        val px = cx + r * cos(a); val py = cy + r * sin(a)
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}
