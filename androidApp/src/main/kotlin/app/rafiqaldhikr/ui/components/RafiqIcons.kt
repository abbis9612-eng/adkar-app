package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * ═══════════════════════════════════════════════════════════════
 *  RafiqIcons — مكتبة الأيقونات الموحّدة لرفيق الذكر
 *
 *  لغة واحدة لكل أيقونات التطبيق (امتداد لأسلوب IslamicIcons):
 *  خط بسماكة w*0.07 بأطراف دائرية، تعبئة شفافة 12% عند الحاجة،
 *  والألوان من RafiqPalette حصراً.
 *  ممنوع استخدام Icons.Default.* (أسلوب Material يكسر الهوية).
 * ═══════════════════════════════════════════════════════════════
 */

/* ── شريط التنقل والأساسيات ── */

@Composable
fun IcoHome(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // سقف بقوس إسلامي خفيف
        val roof = Path().apply {
            moveTo(w * 0.14f, h * 0.46f)
            cubicTo(w * 0.24f, h * 0.30f, w * 0.40f, h * 0.16f, w * 0.50f, h * 0.14f)
            cubicTo(w * 0.60f, h * 0.16f, w * 0.76f, h * 0.30f, w * 0.86f, h * 0.46f)
        }
        drawPath(roof, c, style = st)
        // الجدران
        drawLine(c, Offset(w * 0.22f, h * 0.46f), Offset(w * 0.22f, h * 0.84f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.78f, h * 0.46f), Offset(w * 0.78f, h * 0.84f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.16f, h * 0.84f), Offset(w * 0.84f, h * 0.84f), st.width, StrokeCap.Round)
        // باب بقوس
        val door = Path().apply {
            moveTo(w * 0.42f, h * 0.84f); lineTo(w * 0.42f, h * 0.66f)
            cubicTo(w * 0.42f, h * 0.58f, w * 0.58f, h * 0.58f, w * 0.58f, h * 0.66f)
            lineTo(w * 0.58f, h * 0.84f)
        }
        drawPath(door, c, style = st)
    }
}

@Composable
fun IcoQuran(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // غلاف المصحف
        drawRoundRect(
            c.copy(alpha = 0.10f),
            topLeft = Offset(w * 0.20f, h * 0.14f), size = Size(w * 0.60f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.08f)
        )
        drawRoundRect(
            c,
            topLeft = Offset(w * 0.20f, h * 0.14f), size = Size(w * 0.60f, h * 0.72f),
            cornerRadius = CornerRadius(w * 0.08f), style = st
        )
        // زخرفة مركزية — معيّن (نجمة ثمانية مبسطة)
        val d = Path().apply {
            moveTo(w * 0.50f, h * 0.36f); lineTo(w * 0.62f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.64f); lineTo(w * 0.38f, h * 0.50f); close()
        }
        drawPath(d, c, style = Stroke(w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(c, w * 0.035f, Offset(w * 0.50f, h * 0.50f))
    }
}

@Composable
fun IcoMisbaha(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val cx = w * 0.50f; val cy = h * 0.42f; val r = w * 0.28f
        // حبات السبحة — دائرة حبات مع فتحة أسفل
        for (i in 0 until 8) {
            if (i == 4) continue // فتحة عند الأسفل للشرّابة
            val a = (i * 45 - 90) * PI.toFloat() / 180f
            drawCircle(c, w * 0.055f, Offset(cx + r * cos(a), cy + r * sin(a)))
        }
        // الشرّابة
        drawLine(c, Offset(cx, cy + r * 0.82f), Offset(cx, h * 0.82f), w * 0.06f, StrokeCap.Round)
        drawCircle(c, w * 0.065f, Offset(cx, h * 0.88f))
    }
}

@Composable
fun IcoHeart(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald, filled: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = Path().apply {
            moveTo(w * 0.50f, h * 0.80f)
            cubicTo(w * 0.14f, h * 0.52f, w * 0.16f, h * 0.22f, w * 0.38f, h * 0.22f)
            cubicTo(w * 0.46f, h * 0.22f, w * 0.50f, h * 0.30f, w * 0.50f, h * 0.34f)
            cubicTo(w * 0.50f, h * 0.30f, w * 0.54f, h * 0.22f, w * 0.62f, h * 0.22f)
            cubicTo(w * 0.84f, h * 0.22f, w * 0.86f, h * 0.52f, w * 0.50f, h * 0.80f)
            close()
        }
        if (filled) drawPath(p, c) else { drawPath(p, c.copy(alpha = 0.10f)); drawPath(p, c, style = st) }
    }
}

@Composable
fun IcoPerson(s: Dp = 24.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(c, w * 0.15f, Offset(w * 0.50f, h * 0.30f), style = st)
        val sh = Path().apply {
            moveTo(w * 0.20f, h * 0.82f)
            cubicTo(w * 0.20f, h * 0.56f, w * 0.80f, h * 0.56f, w * 0.80f, h * 0.82f)
        }
        drawPath(sh, c, style = st)
    }
}

/* ── أدوات عامة ── */

@Composable
fun IcoSearch(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawCircle(c, w * 0.24f, Offset(w * 0.42f, h * 0.42f), style = Stroke(w * 0.07f, cap = StrokeCap.Round))
        drawLine(c, Offset(w * 0.60f, h * 0.60f), Offset(w * 0.82f, h * 0.82f), w * 0.07f, StrokeCap.Round)
    }
}

@Composable
fun IcoShare(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round)
        val a = Offset(w * 0.26f, h * 0.50f); val b = Offset(w * 0.72f, h * 0.22f); val d = Offset(w * 0.72f, h * 0.78f)
        drawLine(c, a, b, st.width, StrokeCap.Round)
        drawLine(c, a, d, st.width, StrokeCap.Round)
        drawCircle(c, w * 0.10f, a, style = st); drawCircle(c, w * 0.10f, b, style = st); drawCircle(c, w * 0.10f, d, style = st)
    }
}

@Composable
fun IcoCopy(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(c, topLeft = Offset(w * 0.32f, h * 0.14f), size = Size(w * 0.48f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.07f), style = st)
        drawRoundRect(c.copy(alpha = 0.10f), topLeft = Offset(w * 0.18f, h * 0.32f), size = Size(w * 0.48f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.07f))
        drawRoundRect(c, topLeft = Offset(w * 0.18f, h * 0.32f), size = Size(w * 0.48f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.07f), style = st)
    }
}

@Composable
fun IcoBookmark(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold, filled: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = Path().apply {
            moveTo(w * 0.28f, h * 0.16f); lineTo(w * 0.72f, h * 0.16f); lineTo(w * 0.72f, h * 0.84f)
            lineTo(w * 0.50f, h * 0.66f); lineTo(w * 0.28f, h * 0.84f); close()
        }
        if (filled) drawPath(p, c) else { drawPath(p, c.copy(alpha = 0.10f)); drawPath(p, c, style = st) }
    }
}

@Composable
fun IcoTrash(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.error) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawLine(c, Offset(w * 0.22f, h * 0.28f), Offset(w * 0.78f, h * 0.28f), st.width, StrokeCap.Round)
        val handle = Path().apply {
            moveTo(w * 0.42f, h * 0.28f); lineTo(w * 0.44f, h * 0.16f)
            lineTo(w * 0.56f, h * 0.16f); lineTo(w * 0.58f, h * 0.28f)
        }
        drawPath(handle, c, style = st)
        val body = Path().apply {
            moveTo(w * 0.28f, h * 0.28f); lineTo(w * 0.32f, h * 0.84f)
            lineTo(w * 0.68f, h * 0.84f); lineTo(w * 0.72f, h * 0.28f)
        }
        drawPath(body, c, style = st)
        drawLine(c, Offset(w * 0.44f, h * 0.40f), Offset(w * 0.45f, h * 0.72f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.56f, h * 0.40f), Offset(w * 0.55f, h * 0.72f), st.width, StrokeCap.Round)
    }
}

@Composable
fun IcoRefresh(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = w * 0.07f
        drawArc(c, -45f, 270f, false, Offset(w * 0.20f, h * 0.20f), Size(w * 0.60f, h * 0.60f),
            style = Stroke(st, cap = StrokeCap.Round))
        // رأس السهم عند بداية القوس
        drawLine(c, Offset(w * 0.71f, h * 0.29f), Offset(w * 0.84f, h * 0.31f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.71f, h * 0.29f), Offset(w * 0.73f, h * 0.15f), st, StrokeCap.Round)
    }
}

@Composable
fun IcoPlus(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawLine(c, Offset(w * 0.50f, h * 0.20f), Offset(w * 0.50f, h * 0.80f), w * 0.08f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.20f, h * 0.50f), Offset(w * 0.80f, h * 0.50f), w * 0.08f, StrokeCap.Round)
    }
}

@Composable
fun IcoClose(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.inkMed) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawLine(c, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), w * 0.08f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), w * 0.08f, StrokeCap.Round)
    }
}

@Composable
fun IcoSend(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = Path().apply {
            moveTo(w * 0.16f, h * 0.46f); lineTo(w * 0.84f, h * 0.18f)
            lineTo(w * 0.60f, h * 0.84f); lineTo(w * 0.46f, h * 0.56f); close()
        }
        drawPath(p, c.copy(alpha = 0.10f)); drawPath(p, c, style = st)
        drawLine(c, Offset(w * 0.46f, h * 0.56f), Offset(w * 0.84f, h * 0.18f), st.width, StrokeCap.Round)
    }
}

@Composable
fun IcoMail(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(c, topLeft = Offset(w * 0.14f, h * 0.24f), size = Size(w * 0.72f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.08f), style = st)
        val flap = Path().apply {
            moveTo(w * 0.16f, h * 0.30f); lineTo(w * 0.50f, h * 0.54f); lineTo(w * 0.84f, h * 0.30f)
        }
        drawPath(flap, c, style = st)
    }
}

/* ── حالات ورسائل ── */

@Composable
fun IcoWarning(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.warning) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val tri = Path().apply {
            moveTo(w * 0.50f, h * 0.16f); lineTo(w * 0.88f, h * 0.80f); lineTo(w * 0.12f, h * 0.80f); close()
        }
        drawPath(tri, c.copy(alpha = 0.10f)); drawPath(tri, c, style = st)
        drawLine(c, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.50f, h * 0.58f), st.width, StrokeCap.Round)
        drawCircle(c, w * 0.045f, Offset(w * 0.50f, h * 0.70f))
    }
}

@Composable
fun IcoAlert(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.error) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        drawCircle(c, w * 0.36f, Offset(w * 0.50f, h * 0.50f), style = Stroke(w * 0.07f, cap = StrokeCap.Round))
        drawLine(c, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.50f, h * 0.54f), w * 0.07f, StrokeCap.Round)
        drawCircle(c, w * 0.045f, Offset(w * 0.50f, h * 0.68f))
    }
}

@Composable
fun IcoInbox(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.inkLight) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val box = Path().apply {
            moveTo(w * 0.14f, h * 0.52f); lineTo(w * 0.22f, h * 0.24f); lineTo(w * 0.78f, h * 0.24f)
            lineTo(w * 0.86f, h * 0.52f); lineTo(w * 0.86f, h * 0.80f); lineTo(w * 0.14f, h * 0.80f); close()
        }
        drawPath(box, c, style = st)
        val tray = Path().apply {
            moveTo(w * 0.14f, h * 0.52f); lineTo(w * 0.34f, h * 0.52f); lineTo(w * 0.40f, h * 0.62f)
            lineTo(w * 0.60f, h * 0.62f); lineTo(w * 0.66f, h * 0.52f); lineTo(w * 0.86f, h * 0.52f)
        }
        drawPath(tray, c, style = st)
    }
}

/* ── موقع وقبلة ── */

@Composable
fun IcoPin(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald, off: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(c, w * 0.20f, Offset(w * 0.50f, h * 0.36f), style = st)
        drawCircle(c, w * 0.07f, Offset(w * 0.50f, h * 0.36f))
        drawLine(c, Offset(w * 0.34f, h * 0.50f), Offset(w * 0.50f, h * 0.84f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.66f, h * 0.50f), Offset(w * 0.50f, h * 0.84f), st.width, StrokeCap.Round)
        if (off) drawLine(c, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), st.width, StrokeCap.Round)
    }
}

@Composable
fun IcoCompass(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald, off: Boolean = false) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(c, w * 0.36f, Offset(w * 0.50f, h * 0.50f), style = st)
        val needle = Path().apply {
            moveTo(w * 0.50f, h * 0.26f); lineTo(w * 0.58f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.74f); lineTo(w * 0.42f, h * 0.50f); close()
        }
        drawPath(needle, c.copy(alpha = 0.12f)); drawPath(needle, c, style = st)
        drawCircle(c, w * 0.04f, Offset(w * 0.50f, h * 0.50f))
        if (off) drawLine(c, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.84f), st.width, StrokeCap.Round)
    }
}

/* ── بيانات وملفات ── */

@Composable
fun IcoDownload(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = w * 0.07f
        drawLine(c, Offset(w * 0.50f, h * 0.14f), Offset(w * 0.50f, h * 0.60f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.34f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.66f, h * 0.46f), Offset(w * 0.50f, h * 0.62f), st, StrokeCap.Round)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.66f); lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f); lineTo(w * 0.82f, h * 0.66f)
        }
        drawPath(tray, c, style = Stroke(st, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IcoUpload(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = w * 0.07f
        drawLine(c, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.50f, h * 0.16f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.34f, h * 0.30f), Offset(w * 0.50f, h * 0.14f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.66f, h * 0.30f), Offset(w * 0.50f, h * 0.14f), st, StrokeCap.Round)
        val tray = Path().apply {
            moveTo(w * 0.18f, h * 0.66f); lineTo(w * 0.18f, h * 0.84f)
            lineTo(w * 0.82f, h * 0.84f); lineTo(w * 0.82f, h * 0.66f)
        }
        drawPath(tray, c, style = Stroke(st, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/* ── إنجاز وحماس ── */

@Composable
fun IcoFlame(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.accentOrange) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val p = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            cubicTo(w * 0.62f, h * 0.28f, w * 0.80f, h * 0.42f, w * 0.80f, h * 0.60f)
            cubicTo(w * 0.80f, h * 0.78f, w * 0.66f, h * 0.88f, w * 0.50f, h * 0.88f)
            cubicTo(w * 0.34f, h * 0.88f, w * 0.20f, h * 0.78f, w * 0.20f, h * 0.60f)
            cubicTo(w * 0.20f, h * 0.48f, w * 0.30f, h * 0.36f, w * 0.38f, h * 0.28f)
            cubicTo(w * 0.42f, h * 0.24f, w * 0.46f, h * 0.18f, w * 0.50f, h * 0.12f)
            close()
        }
        drawPath(p, c.copy(alpha = 0.12f)); drawPath(p, c, style = st)
        // لهب داخلي
        val inner = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            cubicTo(w * 0.58f, h * 0.60f, w * 0.60f, h * 0.68f, w * 0.56f, h * 0.74f)
            cubicTo(w * 0.52f, h * 0.80f, w * 0.44f, h * 0.78f, w * 0.42f, h * 0.70f)
        }
        drawPath(inner, c, style = Stroke(w * 0.05f, cap = StrokeCap.Round))
    }
}

@Composable
fun IcoTrophy(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // الكأس
        val cup = Path().apply {
            moveTo(w * 0.30f, h * 0.16f); lineTo(w * 0.70f, h * 0.16f); lineTo(w * 0.68f, h * 0.42f)
            cubicTo(w * 0.64f, h * 0.56f, w * 0.36f, h * 0.56f, w * 0.32f, h * 0.42f)
            close()
        }
        drawPath(cup, c.copy(alpha = 0.10f)); drawPath(cup, c, style = st)
        // المقبضان
        drawArc(c, -90f, -160f, false, Offset(w * 0.12f, h * 0.18f), Size(w * 0.20f, h * 0.24f),
            style = Stroke(st.width, cap = StrokeCap.Round))
        drawArc(c, -90f, 160f, false, Offset(w * 0.68f, h * 0.18f), Size(w * 0.20f, h * 0.24f),
            style = Stroke(st.width, cap = StrokeCap.Round))
        // الساق والقاعدة
        drawLine(c, Offset(w * 0.50f, h * 0.56f), Offset(w * 0.50f, h * 0.70f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.36f, h * 0.76f), Offset(w * 0.64f, h * 0.76f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.30f, h * 0.84f), Offset(w * 0.70f, h * 0.84f), st.width, StrokeCap.Round)
    }
}

/* ── تصنيفات دلالية إضافية ── */

/** طعام — صحن مع بخار (أدعية الطعام) */
@Composable
fun IcoFood(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // صحن نصف دائري
        val plate = Path().apply {
            moveTo(w * 0.16f, h * 0.56f)
            cubicTo(w * 0.16f, h * 0.80f, w * 0.84f, h * 0.80f, w * 0.84f, h * 0.56f)
            close()
        }
        drawPath(plate, c.copy(alpha = 0.12f)); drawPath(plate, c, style = st)
        drawLine(c, Offset(w * 0.10f, h * 0.56f), Offset(w * 0.90f, h * 0.56f), st.width, StrokeCap.Round)
        // بخار
        for (dx in listOf(0.38f, 0.50f, 0.62f)) {
            val p = Path().apply {
                moveTo(w * dx, h * 0.44f)
                cubicTo(w * (dx + 0.05f), h * 0.38f, w * (dx - 0.05f), h * 0.34f, w * dx, h * 0.28f)
            }
            drawPath(p, c.copy(alpha = 0.6f), style = Stroke(w * 0.045f, cap = StrokeCap.Round))
        }
    }
}

/** صحة/شفاء — قلب بنبضة (أدعية المرض) */
@Composable
fun IcoHealth(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val heart = Path().apply {
            moveTo(w * 0.50f, h * 0.80f)
            cubicTo(w * 0.16f, h * 0.54f, w * 0.18f, h * 0.24f, w * 0.38f, h * 0.24f)
            cubicTo(w * 0.46f, h * 0.24f, w * 0.50f, h * 0.32f, w * 0.50f, h * 0.36f)
            cubicTo(w * 0.50f, h * 0.32f, w * 0.54f, h * 0.24f, w * 0.62f, h * 0.24f)
            cubicTo(w * 0.82f, h * 0.24f, w * 0.84f, h * 0.54f, w * 0.50f, h * 0.80f)
            close()
        }
        drawPath(heart, c.copy(alpha = 0.12f)); drawPath(heart, c, style = st)
        // خط النبض
        val pulse = Path().apply {
            moveTo(w * 0.26f, h * 0.50f); lineTo(w * 0.40f, h * 0.50f); lineTo(w * 0.46f, h * 0.40f)
            lineTo(w * 0.54f, h * 0.60f); lineTo(w * 0.60f, h * 0.50f); lineTo(w * 0.74f, h * 0.50f)
        }
        drawPath(pulse, c, style = Stroke(w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** غروب — شمس نصفها تحت الأفق مع سهم نزول (أذكار المساء) */
@Composable
fun IcoSunset(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.eveningRing) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = w * 0.06f
        drawArc(c, 180f, 180f, false, Offset(w * 0.32f, h * 0.30f), Size(w * 0.36f, w * 0.36f),
            style = Stroke(st, cap = StrokeCap.Round))
        for (i in 0 until 5) {
            val a = (180 + i * 45) * (PI.toFloat() / 180f)
            val cx = w * 0.5f; val cy = h * 0.48f
            drawLine(c, Offset(cx + w * 0.24f * cos(a), cy + w * 0.24f * sin(a)),
                Offset(cx + w * 0.33f * cos(a), cy + w * 0.33f * sin(a)), st, StrokeCap.Round)
        }
        drawLine(c, Offset(w * 0.10f, h * 0.52f), Offset(w * 0.90f, h * 0.52f), st, StrokeCap.Round)
        // سهم نزول صغير
        drawLine(c, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.78f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.43f, h * 0.71f), Offset(w * 0.5f, h * 0.78f), st, StrokeCap.Round)
        drawLine(c, Offset(w * 0.57f, h * 0.71f), Offset(w * 0.5f, h * 0.78f), st, StrokeCap.Round)
    }
}

@Composable
fun IcoBulb(s: Dp = 22.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(c, w * 0.22f, Offset(w * 0.50f, h * 0.38f), style = st)
        drawLine(c, Offset(w * 0.42f, h * 0.58f), Offset(w * 0.42f, h * 0.72f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.58f, h * 0.58f), Offset(w * 0.58f, h * 0.72f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.58f, h * 0.72f), st.width, StrokeCap.Round)
        drawLine(c, Offset(w * 0.45f, h * 0.80f), Offset(w * 0.55f, h * 0.80f), st.width, StrokeCap.Round)
        // شعاع خفيف
        drawLine(c, Offset(w * 0.50f, h * 0.08f), Offset(w * 0.50f, h * 0.02f + h * 0.10f), st.width * 0.8f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.22f, h * 0.20f), Offset(w * 0.17f, h * 0.15f), st.width * 0.8f, StrokeCap.Round)
        drawLine(c, Offset(w * 0.78f, h * 0.20f), Offset(w * 0.83f, h * 0.15f), st.width * 0.8f, StrokeCap.Round)
    }
}
