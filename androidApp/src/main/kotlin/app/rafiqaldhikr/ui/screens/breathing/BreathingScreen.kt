package app.rafiqaldhikr.ui.screens.breathing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LocalReducedMotion
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.BAQIYAT
import kotlinx.coroutines.delay

/* ═══════════════════════════════════════════════════════════════════
   التنفّس مع الذكر

   كانت الدائرةُ والنصُّ ساعتين مختلفتين تسيران معاً:

     • الدائرة: `tween(4000)` + `RepeatMode.Reverse` ← دورتُها **٨** ثوانٍ.
     • النصّ:   ٤ شهيق + ٢ إمساك + ٤ زفير + ٢ راحة ← دورتُه **١٢** ثانية.

   فتفترقان بعد الدورة الأولى ولا تلتقيان: الدائرةُ تنكمش والشاشةُ تقول
   «شهيق». وهذه شاشةُ تنفّسٍ — الدائرةُ هي التعليمة، فإن كذبت لم يبقَ فيها
   شيء.

   والحركةُ كانت تبدأ عند التركيب لا عند الضغط، وتدور والجلسةُ متوقّفة.

   فالآن للدائرة `Animatable` واحدٌ يُقاد من داخل حلقة الأطوار نفسِها:
   طورٌ واحدٌ، أمرٌ واحدٌ للحركة، فلا يمكن أن يفترقا أصلاً.
═══════════════════════════════════════════════════════════════════ */

private const val INHALE_MS = 4000
private const val HOLD_MS   = 2000
private const val EXHALE_MS = 4000
private const val REST_MS   = 2000

private const val SMALL = 0.62f
private const val BIG   = 1.15f

@Composable
fun BreathingScreen(navController: NavHostController) {
    val rc      = LocalRafiqColors.current
    val still   = LocalReducedMotion.current
    var running by remember { mutableStateOf(false) }
    var phase   by remember { mutableIntStateOf(-1) }   // ‎-1 استعداد، ٠..٣ الأطوار
    var dhikr   by remember { mutableStateOf(BAQIYAT.first()) }

    val size = remember { Animatable(SMALL) }

    /*  حلقةٌ واحدةٌ تقود الطورَ والدائرةَ معاً.
     *
     *  `animateTo` معلَّقة، فمدّةُ الطور هي مدّةُ الحركة بالضبط ولا حاجة
     *  إلى `delay` موازٍ يمكن أن ينزلق عنها. وعند خفض الحركة `snapTo`:
     *  الدائرةُ تقفز إلى حجم الطور فتبقى التعليمةُ مقروءةً بلا تحرّك. */
    LaunchedEffect(running) {
        if (!running) {
            phase = -1
            size.snapTo(SMALL)
            return@LaunchedEffect
        }
        while (true) {
            phase = 0
            if (still) { size.snapTo(BIG); delay(INHALE_MS.toLong()) }
            else size.animateTo(BIG, tween(INHALE_MS, easing = EaseInOutCubic))

            phase = 1; delay(HOLD_MS.toLong())

            phase = 2
            if (still) { size.snapTo(SMALL); delay(EXHALE_MS.toLong()) }
            else size.animateTo(SMALL, tween(EXHALE_MS, easing = EaseInOutCubic))

            phase = 3; delay(REST_MS.toLong())
        }
    }

    val dhikrText = dhikr
    val phaseText = when (phase) {
        0    -> stringResource(R.string.breath_inhale, dhikrText)
        1    -> stringResource(R.string.breath_hold)
        2    -> stringResource(R.string.breath_exhale)
        3    -> stringResource(R.string.breath_rest)
        else -> stringResource(R.string.breath_ready)
    }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RafiqTopBar(
                title  = stringResource(R.string.breath_title),
                onBack = { navController.popBackStack() },
            )

            Column(
                modifier            = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    phaseText,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = rc.emerald,
                )

                Spacer(Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(size.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    rc.emerald.copy(alpha = 0.30f),
                                    rc.emeraldPastel.copy(alpha = 0.20f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    /*  كان `Color.White` على تدرّجٍ زمرّديٍّ فاتح: ١٫٣٥:١ في
                     *  السمة الفاتحة و٢٫١:١ في الداكنة — وأدنى ما يُقبل ٤٫٥:١.
                     *  و`rc.ink` حبرُ السمة نفسِها، يقلب مع الليل والنهار. */
                    Text(
                        dhikrText,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                        textAlign  = TextAlign.Center,
                        color      = rc.ink,
                    )
                }

                Spacer(Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RafiqShape.card)
                        .background(if (running) rc.card else rc.emerald)
                        .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.card)
                        .clickable { running = !running },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(if (running) R.string.breath_stop else R.string.breath_start),
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (running) rc.ink else rc.onEmerald,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(stringResource(R.string.breath_pick), fontSize = 13.sp, color = rc.inkMed)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BAQIYAT.forEach { text ->
                        val on = dhikr == text
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RafiqShape.item)
                                .background(if (on) rc.emeraldPastel else rc.card)
                                .border(1.dp, if (on) rc.emerald else rc.divider, RafiqShape.item)
                                .clickable { dhikr = text }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text,
                                color     = if (on) rc.emerald else rc.inkMed,
                                textAlign = TextAlign.Center,
                                style     = RafiqType.micro,
                            )
                        }
                    }
                }
            }
        }
    }
}
