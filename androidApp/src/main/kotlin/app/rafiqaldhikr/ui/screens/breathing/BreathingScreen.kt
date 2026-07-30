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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import kotlinx.coroutines.delay
import app.rafiqaldhikr.ui.components.RafiqBackButton
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.stillableFloat

@Composable
fun BreathingScreen(navController: NavHostController) {
    val rc = LocalRafiqColors.current
    var isRunning by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf("استعد") }
    var dhikrText by remember { mutableStateOf("سبحان الله") }

    val scale by stillableFloat(0.6f, 1.2f, 4000, EaseInOutCubic, RepeatMode.Reverse, "scale")

    val alpha by stillableFloat(0.3f, 0.8f, 4000, EaseInOutCubic, RepeatMode.Reverse, "alpha")

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
            RafiqTopBar(
                title  = "التنفس والذكر",
                onBack = {navController.popBackStack()},
            )

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
                        .clip(RafiqShape.card)
                        .background(if (isRunning) rc.card else rc.emerald)
                        .border(1.dp, rc.gold.copy(alpha = BorderIdle), RafiqShape.card)
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
                                .clip(RafiqShape.item)
                                .background(if (dhikrText == text) rc.emeraldPastel else rc.card)
                                .border(
                                    1.dp,
                                    if (dhikrText == text) rc.emerald else rc.divider,
                                    RafiqShape.item
                                )
                                .clickable { dhikrText = text }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text,
                                color    = if (dhikrText == text) rc.emerald else rc.inkMed,
                                textAlign = TextAlign.Center, style = RafiqType.micro)
                        }
                    }
                }
            }
        }
    }
}
