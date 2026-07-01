package app.rafiqaldhikr.ui.screens.breathing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlinx.coroutines.delay

@Composable
fun BreathingScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current
    var isRunning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("استعد") }
    var dhikrText by remember { mutableStateOf("سبحان الله") }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue  = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue  = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Phase cycling
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (isRunning) {
            phase = "شهيق — $dhikrText"; delay(4000)
            phase = "إمساك"; delay(2000)
            phase = "زفير — الحمد لله"; delay(4000)
            phase = "راحة"; delay(2000)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ═══ HEADER ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "التنفس والذكر",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = rc.emerald,
                )

                // Back Button
                Box(
                    Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(rc.card)
                        .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(18.dp)) {
                        val w = size.width
                        val h = size.height
                        drawPath(androidx.compose.ui.graphics.Path().apply {
                            moveTo(w * 0.35f, h * 0.15f)
                            lineTo(w * 0.70f, h * 0.50f)
                            lineTo(w * 0.35f, h * 0.85f)
                        }, rc.emerald, style = androidx.compose.ui.graphics.drawscope.Stroke(w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                    }
                }
            }

            // Content
            Column(
                modifier            = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Phase label
                Text(
                    phase,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = rc.emerald
                )

                Spacer(Modifier.height(48.dp))

                // Breathing circle
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(if (isRunning) scale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    rc.emerald.copy(alpha = if (isRunning) alpha else 0.4f),
                                    rc.emeraldPastel.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dhikrText,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                        textAlign  = TextAlign.Center,
                        color      = androidx.compose.ui.graphics.Color.White
                    )
                }

                Spacer(Modifier.height(48.dp))

                // Start/Stop button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isRunning) rc.card else rc.emerald)
                        .border(1.dp, rc.gold.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .clickable { isRunning = !isRunning },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isRunning) "إيقاف" else "ابدأ التنفس",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isRunning) rc.ink else androidx.compose.ui.graphics.Color.White
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Dhikr picker
                Text("اختر الذكر", fontSize = 13.sp, color = rc.inkMed)
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("سبحان الله", "الحمد لله", "الله أكبر", "لا إله إلا الله").forEach { text ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (dhikrText == text) rc.emeraldPastel else rc.card)
                                .border(
                                    1.dp,
                                    if (dhikrText == text) rc.emerald else rc.divider,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { dhikrText = text }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text,
                                fontSize = 11.sp,
                                color    = if (dhikrText == text) rc.emerald else rc.inkMed,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
