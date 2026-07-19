package app.rafiqaldhikr.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlin.random.Random

/**
 * خلفية حيّة تتغيّر مع وقت اليوم — فجر ذهبي، صباح منعش، ظهر صافٍ، عصر دافئ،
 * مغرب أحمر، ليل هادئ بنجوم وهلال. مرسومة بالكود (offline) ومتكيّفة مع الثيم.
 *
 * تحافظ على هوية التطبيق (كريمي/زمردي/ذهبي) — تضيف توهّجاً وجرماً سماوياً
 * وحركة خفيفة فوق خلفية التطبيق، لا تستبدلها بسماء زرقاء.
 */
private enum class SkyPhase { DAWN, MORNING, DAY, AFTERNOON, SUNSET, NIGHT }

@Composable
fun LivingSkyBackground(
    modifier: Modifier = Modifier,
    hourOverride: Int? = null,
) {
    val rc = LocalRafiqColors.current

    val hour = hourOverride ?: remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val phase = when (hour) {
        in 4..5   -> SkyPhase.DAWN
        in 6..10  -> SkyPhase.MORNING
        in 11..14 -> SkyPhase.DAY
        in 15..16 -> SkyPhase.AFTERNOON
        in 17..18 -> SkyPhase.SUNSET
        else      -> SkyPhase.NIGHT
    }

    // لون التوهّج وشدّته والجرم السماوي حسب الوقت — كلها من الـ palette
    val glow: Color; val glowAlpha: Float; val body: Color; val night: Boolean
    when (phase) {
        SkyPhase.DAWN      -> { glow = rc.gold;         glowAlpha = 0.17f; body = rc.gold;        night = false }
        SkyPhase.MORNING   -> { glow = rc.emeraldLight; glowAlpha = 0.11f; body = rc.gold;        night = false }
        SkyPhase.DAY       -> { glow = rc.gold;         glowAlpha = 0.08f; body = rc.gold;        night = false }
        SkyPhase.AFTERNOON -> { glow = rc.accentOrange; glowAlpha = 0.12f; body = rc.gold;        night = false }
        SkyPhase.SUNSET    -> { glow = rc.accentOrange; glowAlpha = 0.20f; body = rc.accentOrange; night = false }
        SkyPhase.NIGHT     -> { glow = rc.purpleSleep;  glowAlpha = 0.16f; body = rc.eveningRing;  night = true  }
    }

    // حركة لطيفة: وميض النجوم + انجراف بطيء
    val tr = rememberInfiniteTransition(label = "sky")
    val twinkle by tr.animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle",
    )
    val drift by tr.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    // مواقع النجوم/الجزيئات ثابتة طوال الجلسة
    val motes = remember { List(30) { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) } }

    Canvas(modifier) {
        val w = size.width; val h = size.height

        // 1) أساس خلفية التطبيق
        drawRect(rc.bg)

        // 2) توهّج علوي ناعم يتبع وقت اليوم
        drawRect(
            Brush.radialGradient(
                colors = listOf(glow.copy(alpha = glowAlpha), Color.Transparent),
                center = Offset(w * 0.30f, h * 0.01f),
                radius = h * 0.58f,
            ),
        )

        if (night) {
            // 3) نجوم واميضة في النصف العلوي
            motes.forEach { (mx, my, ph) ->
                val a = (0.20f + 0.80f * twinkle) * (0.35f + 0.65f * ph)
                val r = 0.8f + 2.2f * ph
                drawCircle(body.copy(alpha = a * 0.85f), r, Offset(mx * w, my * h * 0.62f))
            }
            // 4) هلال متوهّج أعلى اليمين
            val mCx = w * 0.82f; val mCy = h * 0.09f
            drawCircle(
                Brush.radialGradient(listOf(body.copy(alpha = 0.35f), Color.Transparent),
                    Offset(mCx, mCy), h * 0.11f),
                radius = h * 0.11f, center = Offset(mCx, mCy),
            )
            drawCircle(body.copy(alpha = 0.92f), w * 0.03f, Offset(mCx, mCy))
            drawCircle(rc.bg, w * 0.026f, Offset(mCx + w * 0.016f, mCy - w * 0.007f))
        } else {
            // 3) شمس متوهّجة أعلى اليمين
            val sCx = w * 0.80f; val sCy = h * (0.06f + 0.02f * drift)
            drawCircle(
                Brush.radialGradient(listOf(body.copy(alpha = glowAlpha + 0.14f), Color.Transparent),
                    Offset(sCx, sCy), h * 0.13f),
                radius = h * 0.13f, center = Offset(sCx, sCy),
            )
            drawCircle(body.copy(alpha = 0.55f), w * 0.032f, Offset(sCx, sCy))
            // 4) ذرّات ضوء ناعمة تنجرف نهاراً
            motes.take(10).forEach { (mx, my, ph) ->
                val a = 0.05f + 0.06f * ph * twinkle
                drawCircle(body.copy(alpha = a), 1.5f + 2f * ph,
                    Offset(((mx + drift * 0.08f) % 1f) * w, my * h * 0.5f))
            }
        }
    }
}
