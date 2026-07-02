package app.rafiqaldhikr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GeomStar(s: Dp = 60.dp, color: Color = LocalRafiqColors.current.gold, op: Float = 0.18f, m: Modifier = Modifier) {
    Canvas(m.size(s).graphicsLayer { alpha = op }) {
        val w = size.width; val cx = w / 2f; val cy = w / 2f
        val hex1 = Path().apply {
            for (i in 0 until 6) { val a = (i * 60 - 90) * PI.toFloat() / 180f; val r = w * 0.47f
                if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a)) else lineTo(cx + r * cos(a), cy + r * sin(a)) }; close() }
        drawPath(hex1, color, style = Stroke(1.2f))
        val hex2 = Path().apply {
            for (i in 0 until 6) { val a = (i * 60 - 60) * PI.toFloat() / 180f; val r = w * 0.30f
                if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a)) else lineTo(cx + r * cos(a), cy + r * sin(a)) }; close() }
        drawPath(hex2, color, style = Stroke(0.8f))
        for (i in 0 until 3) { val a = (i * 60 - 90) * PI.toFloat() / 180f
            drawLine(color, Offset(cx + w * 0.03f * cos(a), cy + w * 0.03f * sin(a)),
                Offset(cx + w * 0.47f * cos(a), cy + w * 0.47f * sin(a)), 0.5f)
            val a2 = a + PI.toFloat()
            drawLine(color, Offset(cx + w * 0.03f * cos(a2), cy + w * 0.03f * sin(a2)),
                Offset(cx + w * 0.47f * cos(a2), cy + w * 0.47f * sin(a2)), 0.5f) }
        drawCircle(color, w * 0.08f, Offset(cx, cy), style = Stroke(0.8f))
        drawCircle(color, w * 0.03f, Offset(cx, cy))
    }
}

@Composable
fun IcoBell(s: Dp = 19.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val st = Stroke(w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val bell = Path().apply { moveTo(w * 0.23f, h * 0.73f); lineTo(w * 0.29f, h * 0.38f)
            cubicTo(w * 0.35f, h * 0.27f, w * 0.65f, h * 0.27f, w * 0.71f, h * 0.38f)
            lineTo(w * 0.77f, h * 0.73f); close() }
        drawPath(bell, c.copy(alpha = 0.12f)); drawPath(bell, c, style = st)
        val clap = Path().apply { moveTo(w * 0.40f, h * 0.73f)
            cubicTo(w * 0.40f, h * 0.83f, w * 0.60f, h * 0.83f, w * 0.60f, h * 0.73f) }
        drawPath(clap, c, style = st) 
    } 
}

@Composable
fun IcoGear(s: Dp = 19.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val cx = w / 2f; val cy = w / 2f
        drawCircle(c, w * 0.15f, Offset(cx, cy), style = Stroke(w * 0.07f))
        val hex = Path().apply { for (i in 0 until 6) { val a = (i * 60 - 30) * PI.toFloat() / 180f; val r = w * 0.42f
            if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a)) else lineTo(cx + r * cos(a), cy + r * sin(a)) }; close() }
        drawPath(hex, c, style = Stroke(w * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)) 
    } 
}

@Composable
fun IcoCheck(s: Dp = 14.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val p = Path().apply { moveTo(w * 0.20f, h * 0.52f); lineTo(w * 0.40f, h * 0.73f); lineTo(w * 0.80f, h * 0.28f) }
        drawPath(p, c, style = Stroke(w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)) 
    } 
}

@Composable
fun IcoSun(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val cx = w / 2f; val cy = w / 2f
        drawCircle(c, w * 0.18f, Offset(cx, cy), style = Stroke(w * 0.05f, cap = StrokeCap.Round))
        for (i in 0 until 8) { val a = (i * 45) * PI.toFloat() / 180f
            drawLine(c, Offset(cx + w * 0.28f * cos(a), cy + w * 0.28f * sin(a)),
                Offset(cx + w * 0.40f * cos(a), cy + w * 0.40f * sin(a)), w * 0.05f, cap = StrokeCap.Round) } 
    } 
}

@Composable
fun IcoMoon(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.purple) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val cx = w * 0.42f; val cy = w * 0.48f
        val moon = Path().apply { addOval(Rect(Offset(cx, cy), w * 0.34f)) }
        val cut = Path().apply { addOval(Rect(Offset(cx + w * 0.22f, cy - w * 0.06f), w * 0.26f)) }
        val cr = Path().apply { op(moon, cut, PathOperation.Difference) }
        drawPath(cr, c, style = Stroke(w * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(c, Offset(w * 0.76f, w * 0.17f), Offset(w * 0.76f, w * 0.28f), w * 0.04f, cap = StrokeCap.Round)
        drawLine(c, Offset(w * 0.70f, w * 0.23f), Offset(w * 0.82f, w * 0.23f), w * 0.04f, cap = StrokeCap.Round) 
    } 
}

@Composable
fun IcoStar(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.purpleSleep) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val cx = w / 2f; val cy = w / 2f
        val st = Stroke(w * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val star = Path().apply {
            val p = listOf(Offset(cx, cy - w * 0.42f), Offset(cx + w * 0.14f, cy - w * 0.14f),
                Offset(cx + w * 0.42f, cy - w * 0.12f), Offset(cx + w * 0.14f, cy + w * 0.07f),
                Offset(cx + w * 0.42f, cy + w * 0.28f), Offset(cx, cy + w * 0.20f),
                Offset(cx - w * 0.42f, cy + w * 0.28f), Offset(cx - w * 0.14f, cy + w * 0.07f),
                Offset(cx - w * 0.42f, cy - w * 0.12f), Offset(cx - w * 0.14f, cy - w * 0.14f))
            moveTo(p[0].x, p[0].y); p.drop(1).forEach { lineTo(it.x, it.y) }; close() }
        drawPath(star, c.copy(alpha = 0.10f)); drawPath(star, c, style = st)
        drawCircle(c, w * 0.12f, Offset(cx, cy), style = Stroke(w * 0.045f)) 
    } 
}

@Composable
fun IcoDua(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emeraldMed) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height; val st = Stroke(w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val l = Path().apply { moveTo(w*0.29f,h*0.71f); cubicTo(w*0.21f,h*0.59f,w*0.21f,h*0.44f,w*0.21f,h*0.38f)
            cubicTo(w*0.21f,h*0.26f,w*0.32f,h*0.21f,w*0.38f,h*0.21f); cubicTo(w*0.44f,h*0.21f,w*0.44f,h*0.26f,w*0.44f,h*0.29f)
            lineTo(w*0.44f,h*0.56f) }; drawPath(l, c, style = st)
        val r = Path().apply { moveTo(w*0.71f,h*0.71f); cubicTo(w*0.79f,h*0.59f,w*0.79f,h*0.44f,w*0.79f,h*0.38f)
            cubicTo(w*0.79f,h*0.26f,w*0.68f,h*0.21f,w*0.62f,h*0.21f); cubicTo(w*0.56f,h*0.21f,w*0.56f,h*0.26f,w*0.56f,h*0.29f)
            lineTo(w*0.56f,h*0.56f) }; drawPath(r, c, style = st)
        val cup = Path().apply { moveTo(w*0.44f,h*0.56f); cubicTo(w*0.44f,h*0.62f,w*0.56f,h*0.62f,w*0.56f,h*0.56f) }
        drawPath(cup, c, style = st)
        val bt = Path().apply { moveTo(w*0.29f,h*0.71f); cubicTo(w*0.38f,h*0.82f,w*0.62f,h*0.82f,w*0.71f,h*0.71f) }
        drawPath(bt, c, style = st); drawCircle(c, w * 0.04f, Offset(w * 0.50f, h * 0.42f)) 
    } 
}

/* ═══ أيقونات محطات «رفيق اليوم» — مرسومة يدوياً بهوية التطبيق ═══ */

@Composable
fun IcoMosque(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // القبة
        val dome = Path().apply {
            moveTo(w * 0.28f, h * 0.55f)
            cubicTo(w * 0.28f, h * 0.28f, w * 0.72f, h * 0.28f, w * 0.72f, h * 0.55f)
        }
        drawPath(dome, c, style = st)
        // الهلال فوق القبة
        drawLine(c, Offset(w * 0.5f, h * 0.28f), Offset(w * 0.5f, h * 0.16f), st.width)
        drawCircle(c, w * 0.045f, Offset(w * 0.5f, h * 0.12f))
        // القاعدة
        drawLine(c, Offset(w * 0.16f, h * 0.55f), Offset(w * 0.84f, h * 0.55f), st.width)
        drawLine(c, Offset(w * 0.20f, h * 0.55f), Offset(w * 0.20f, h * 0.85f), st.width)
        drawLine(c, Offset(w * 0.80f, h * 0.55f), Offset(w * 0.80f, h * 0.85f), st.width)
        drawLine(c, Offset(w * 0.14f, h * 0.85f), Offset(w * 0.86f, h * 0.85f), st.width)
        // الباب
        val door = Path().apply {
            moveTo(w * 0.42f, h * 0.85f)
            lineTo(w * 0.42f, h * 0.70f)
            cubicTo(w * 0.42f, h * 0.62f, w * 0.58f, h * 0.62f, w * 0.58f, h * 0.70f)
            lineTo(w * 0.58f, h * 0.85f)
        }
        drawPath(door, c, style = st)
    }
}

@Composable
fun IcoSunrise(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.gold) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = w * 0.07f
        // نصف الشمس فوق الأفق
        drawArc(c, 180f, 180f, false,
            Offset(w * 0.30f, h * 0.38f), androidx.compose.ui.geometry.Size(w * 0.40f, w * 0.40f),
            style = Stroke(st, cap = StrokeCap.Round))
        // الأشعة
        for (i in 0 until 5) {
            val a = (180 + i * 45) * (kotlin.math.PI.toFloat() / 180f)
            val cx = w * 0.5f; val cy = h * 0.58f
            drawLine(c,
                Offset(cx + w * 0.26f * kotlin.math.cos(a), cy + w * 0.26f * kotlin.math.sin(a)),
                Offset(cx + w * 0.36f * kotlin.math.cos(a), cy + w * 0.36f * kotlin.math.sin(a)),
                st, StrokeCap.Round)
        }
        // الأفق
        drawLine(c, Offset(w * 0.12f, h * 0.62f), Offset(w * 0.88f, h * 0.62f), st, StrokeCap.Round)
        drawLine(c.copy(alpha = 0.5f), Offset(w * 0.22f, h * 0.76f), Offset(w * 0.78f, h * 0.76f), st, StrokeCap.Round)
    }
}

@Composable
fun IcoBook(s: Dp = 28.dp, c: Color = LocalRafiqColors.current.emerald) {
    Canvas(Modifier.size(s)) {
        val w = size.width; val h = size.height
        val st = Stroke(w * 0.07f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // كتاب مفتوح
        val left = Path().apply {
            moveTo(w * 0.5f, h * 0.30f)
            cubicTo(w * 0.38f, h * 0.20f, w * 0.20f, h * 0.20f, w * 0.12f, h * 0.26f)
            lineTo(w * 0.12f, h * 0.74f)
            cubicTo(w * 0.20f, h * 0.68f, w * 0.38f, h * 0.68f, w * 0.5f, h * 0.78f)
            close()
        }
        val right = Path().apply {
            moveTo(w * 0.5f, h * 0.30f)
            cubicTo(w * 0.62f, h * 0.20f, w * 0.80f, h * 0.20f, w * 0.88f, h * 0.26f)
            lineTo(w * 0.88f, h * 0.74f)
            cubicTo(w * 0.80f, h * 0.68f, w * 0.62f, h * 0.68f, w * 0.5f, h * 0.78f)
            close()
        }
        drawPath(left, c.copy(alpha = 0.12f)); drawPath(left, c, style = st)
        drawPath(right, c.copy(alpha = 0.12f)); drawPath(right, c, style = st)
    }
}

/** أيقونة محطة رفيق اليوم حسب معرّفها — بديل الإيموجي */
@Composable
fun StationIcon(stationId: String, s: Dp = 28.dp) {
    val rc = LocalRafiqColors.current
    when (stationId) {
        "wake"         -> IcoSunrise(s, rc.morningRing)
        "fajr_morning" -> IcoSun(s, rc.gold)
        "duha"         -> IcoSunrise(s, rc.goldLight)
        "dhuhr"        -> IcoMosque(s, rc.emerald)
        "asr_evening"  -> IcoStar(s, rc.eveningRing)
        "maghrib"      -> IcoMosque(s, rc.accentOrange)
        "isha"         -> IcoMosque(s, rc.purpleSleep)
        "sleep"        -> IcoMoon(s, rc.sleepRing)
        "friday_kahf"  -> IcoBook(s, rc.emerald)
        else           -> IcoDua(s, rc.emeraldMed)
    }
}
