package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * مشهد سماء بطاقة الصلاة — يتبع «الصلاة القادمة» نفسها (وليس ساعة الجهاز)
 * فيتطابق اللون مع اسم الصلاة المعروض دائماً.
 *
 * كل فترة: تدرّج عميق مضمون التباين مع النص الأبيض + عنصر سماوي مرسوم
 * بشفافية أنيقة (هلال ونجوم ليلاً، شمس نهاراً، شمس على الأفق شروقاً وغروباً)
 * + توهّج ذهبي واحد يتنفس بهدوء.
 */

private data class Scene(
    val top: Color,
    val mid: Color,
    val bottom: Color,
    val glow: Color,
    val decor: Decor,
)

private enum class Decor { CRESCENT_STARS, SUN_HORIZON_RISE, SUN_HIGH, SUN_LOW, SUN_HORIZON_SET }

private fun sceneFor(prayerName: String): Scene = when (prayerName) {
    "الفجر" -> Scene(              // فجر بنفسجي يتنفس أول الضوء
        Color(0xFF141A38), Color(0xFF3A3464), Color(0xFF7A5C8E),
        Color(0xFFE8B84D), Decor.CRESCENT_STARS
    )
    "الشروق" -> Scene(             // شمس تولد على الأفق
        Color(0xFF1E3252), Color(0xFF5C4A62), Color(0xFFB86A3C),
        Color(0xFFF0C96E), Decor.SUN_HORIZON_RISE
    )
    "الظهر" -> Scene(              // نهار صافٍ عميق
        Color(0xFF0E4A78), Color(0xFF2C6E9E), Color(0xFF4A90B8),
        Color(0xFFFFF3C4), Decor.SUN_HIGH
    )
    "العصر" -> Scene(              // عصر ذهبي دافئ
        Color(0xFF6E4A14), Color(0xFF9A6A20), Color(0xFFC08A34),
        Color(0xFFFFE8A0), Decor.SUN_LOW
    )
    "المغرب" -> Scene(             // غروب أرجواني ناري
        Color(0xFF3A1838), Color(0xFF743050), Color(0xFFB05038),
        Color(0xFFF0A85C), Decor.SUN_HORIZON_SET
    )
    else -> Scene(                 // العشاء — ليل ساكن
        Color(0xFF0C1026), Color(0xFF1C2244), Color(0xFF2E3260),
        Color(0xFFC5BAE8), Decor.CRESCENT_STARS
    )
}

@Composable
fun PrayerScene(
    prayerName: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scene = sceneFor(prayerName)

    val transition = rememberInfiniteTransition(label = "sceneGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier) {
        Canvas(Modifier.matchParentSize()) {
            // ═══ السماء ═══
            drawRect(Brush.verticalGradient(listOf(scene.top, scene.mid, scene.bottom)))

            // ═══ توهّج يتنفس أعلى اليسار (بعيداً عن اسم الصلاة يمينَ RTL) ═══
            val glowCenter = Offset(size.width * 0.22f, size.height * 0.18f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(scene.glow.copy(alpha = glowAlpha), Color.Transparent),
                    center = glowCenter,
                    radius = size.width * 0.40f
                ),
                radius = size.width * 0.40f,
                center = glowCenter
            )

            // ═══ العنصر السماوي ═══
            when (scene.decor) {
                Decor.CRESCENT_STARS   -> drawCrescentAndStars(scene.glow)
                Decor.SUN_HORIZON_RISE -> drawHorizonSun(scene.glow, rising = true)
                Decor.SUN_HORIZON_SET  -> drawHorizonSun(scene.glow, rising = false)
                Decor.SUN_HIGH         -> drawSun(scene.glow, cx = 0.20f, cy = 0.22f)
                Decor.SUN_LOW          -> drawSun(scene.glow, cx = 0.18f, cy = 0.42f)
            }
        }

        content()
    }
}

/* هلال رفيع وثلاث نجوم متلألئة — للفجر والعشاء */
private fun DrawScope.drawCrescentAndStars(tint: Color) {
    val cx = size.width * 0.18f
    val cy = size.height * 0.26f
    val r = size.width * 0.055f
    val moon = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r, cy - r), r * 2))
    }
    val cut = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r * 0.45f, cy - r * 1.15f), r * 1.75f))
    }
    val crescent = Path().apply {
        op(moon, cut, androidx.compose.ui.graphics.PathOperation.Difference)
    }
    drawPath(crescent, tint.copy(alpha = 0.55f))

    // نجوم (نقاط ماسية صغيرة)
    listOf(
        Offset(size.width * 0.32f, size.height * 0.18f) to 0.010f,
        Offset(size.width * 0.09f, size.height * 0.14f) to 0.007f,
        Offset(size.width * 0.27f, size.height * 0.42f) to 0.006f,
        Offset(size.width * 0.40f, size.height * 0.30f) to 0.008f,
    ).forEach { (pos, rf) ->
        drawCircle(tint.copy(alpha = 0.65f), size.width * rf, pos)
    }
}

/* شمس كاملة بأشعة هادئة — للظهر والعصر */
private fun DrawScope.drawSun(tint: Color, cx: Float, cy: Float) {
    val c = Offset(size.width * cx, size.height * cy)
    val r = size.width * 0.05f
    drawCircle(tint.copy(alpha = 0.50f), r, c)
    drawCircle(tint.copy(alpha = 0.25f), r * 1.6f, c)
    for (i in 0 until 8) {
        val a = (i * 45f) * (Math.PI.toFloat() / 180f)
        drawLine(
            tint.copy(alpha = 0.40f),
            Offset(c.x + r * 1.9f * cos(a), c.y + r * 1.9f * sin(a)),
            Offset(c.x + r * 2.5f * cos(a), c.y + r * 2.5f * sin(a)),
            size.width * 0.006f, StrokeCap.Round
        )
    }
}

/* نصف شمس على خط الأفق — للشروق والمغرب */
private fun DrawScope.drawHorizonSun(tint: Color, rising: Boolean) {
    val horizonY = size.height * 0.62f
    val c = Offset(size.width * 0.20f, horizonY)
    val r = size.width * 0.065f
    // نصف القرص فوق الأفق
    drawArc(
        color = tint.copy(alpha = if (rising) 0.55f else 0.45f),
        startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(c.x - r, c.y - r),
        size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
    )
    // خط الأفق
    drawLine(
        tint.copy(alpha = 0.35f),
        Offset(size.width * 0.06f, horizonY),
        Offset(size.width * 0.44f, horizonY),
        size.width * 0.005f, StrokeCap.Round
    )
    // أشعة نصف علوية
    for (i in 1..5) {
        val a = (180f + i * 30f) * (Math.PI.toFloat() / 180f)
        drawLine(
            tint.copy(alpha = 0.35f),
            Offset(c.x + r * 1.35f * cos(a), c.y + r * 1.35f * sin(a)),
            Offset(c.x + r * 1.85f * cos(a), c.y + r * 1.85f * sin(a)),
            size.width * 0.005f, StrokeCap.Round
        )
    }
}
