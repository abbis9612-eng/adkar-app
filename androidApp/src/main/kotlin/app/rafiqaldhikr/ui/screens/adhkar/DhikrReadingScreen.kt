package app.rafiqaldhikr.ui.screens.adhkar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.ErrorState
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import org.koin.androidx.compose.koinViewModel
import kotlin.math.*
import app.rafiqaldhikr.ui.components.RafiqBackButton

/* Colors provided by LocalRafiqColors */

/* ══════════════════════════════════════════════════════════════
   CANVAS ICONS
══════════════════════════════════════════════════════════════ */

/* ══════════════════════════════════════════════════════════════
   GEOMETRIC DECORATION
══════════════════════════════════════════════════════════════ */

@Composable
private fun GeomDecoration(
    sizeDp: Dp = 160.dp,
    color: Color = LocalRafiqColors.current.gold.copy(alpha = 0.10f),
    spinDuration: Int = 90_000,
    modifier: Modifier = Modifier,
) {
    val tr = rememberInfiniteTransition(label = "geom")
    val rotation by tr.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(spinDuration, easing = LinearEasing)),
        label = "geomRot"
    )
    Canvas(modifier = modifier.size(sizeDp)) {
        val sz = this.size.width; val cx = sz / 2f; val cy = sz / 2f
        rotate(rotation, pivot = Offset(cx, cy)) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = (i * 60 - 90) * PI.toFloat() / 180f; val r = sz * 0.43f
                    if (i == 0) moveTo(cx + r * cos(a), cy + r * sin(a))
                    else lineTo(cx + r * cos(a), cy + r * sin(a))
                }; close()
            }
            drawPath(hex, color, style = Stroke(1.2f))
            drawCircle(color, sz * 0.44f, Offset(cx, cy), style = Stroke(0.7f))
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun DhikrReadingScreen(
    category:      String,
    navController: NavHostController,
    viewModel:     DhikrReadingViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val inf = rememberInfiniteTransition(label = "dhikrPulse")
    val pulseScale by inf.animateFloat(
        1f, 1.04f,
        infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )

    LaunchedEffect(category) { viewModel.loadCategory(category) }

    Box(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
    ) {
        // Background decoration
        GeomDecoration(
            sizeDp = 200.dp,
            color = LocalRafiqColors.current.gold.copy(alpha = 0.05f),
            spinDuration = 120_000,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
        )

        when {
            uiState.isLoading -> LoadingState()
            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadCategory(category) }
            )
            uiState.isAllCompleted -> {
                LaunchedEffect(Unit) {
                    navController.navigate(RafiqRoute.Celebration.route)
                }
            }
            uiState.adhkar.isNotEmpty() -> {
                val dhikr = uiState.adhkar[uiState.currentIndex]

                Column(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.tap()
                            }
                        )
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
                        // Title
                        Column {
                            Text(
                                getCategoryTitle(category),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalRafiqColors.current.emerald,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "${uiState.currentIndex + 1} / ${uiState.adhkar.size}"
                                    .localizedDigits(LocalArabicNumerals.current),
                                style = NumbersStyle,
                                fontSize = 13.sp,
                                color = LocalRafiqColors.current.inkMed,
                            )
                        }

                        RafiqBackButton(onClick = { navController.popBackStack() })
                    }

                    // ═══ PROGRESS BAR ═══
                    val progress = uiState.currentIndex.toFloat() / uiState.adhkar.size.coerceAtLeast(1)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(rc.emeraldPastel)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(rc.emerald, rc.emeraldLight))
                                )
                        )
                    }

                    // ═══ MAIN CONTENT ═══
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(24.dp))

                        // Dhikr text card
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(rc.card)
                                .border(1.dp, rc.gold.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(28.dp)
                        ) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // Gold ornament top
                                Canvas(Modifier.size(width = 60.dp, height = 3.dp)) {
                                    drawRoundRect(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, rc.gold.copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
                                    )
                                }

                                Spacer(Modifier.height(20.dp))

                                Text(
                                    dhikr.textAr,
                                    fontSize = 23.sp,
                                    fontFamily = app.rafiqaldhikr.ui.theme.NaskhFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = LocalRafiqColors.current.ink,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 46.sp,
                                )

                                if (dhikr.virtue.isNotBlank()) {
                                    Spacer(Modifier.height(18.dp))

                                    // Divider
                                    Canvas(Modifier.size(width = 80.dp, height = 1.dp)) {
                                        drawLine(
                                            rc.gold.copy(alpha = 0.2f),
                                            Offset(0f, 0f),
                                            Offset(size.width, 0f),
                                            1f,
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Text(
                                        dhikr.virtue,
                                        fontSize = 14.sp,
                                        color = LocalRafiqColors.current.emerald,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp,
                                    )
                                }

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    "المصدر: ${dhikr.source}",
                                    fontSize = 12.sp,
                                    color = LocalRafiqColors.current.inkLight,
                                    textAlign = TextAlign.Center,
                                )

                                Spacer(Modifier.height(16.dp))

                                // Gold ornament bottom
                                Canvas(Modifier.size(width = 60.dp, height = 3.dp)) {
                                    drawRoundRect(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, rc.gold.copy(alpha = 0.4f), Color.Transparent)
                                        ),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(36.dp))

                        // ═══ TAP COUNTER CIRCLE ═══
                        Box(
                            Modifier
                                .size(160.dp)
                                .scale(pulseScale)
                                .shadow(20.dp, CircleShape,
                                    ambientColor = LocalRafiqColors.current.emerald.copy(alpha = 0.20f))
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(rc.emeraldMed, rc.emerald),
                                        radius = 300f,
                                    )
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.tap()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            // Gold ring
                            Canvas(Modifier.size(150.dp)) {
                                drawCircle(
                                    rc.goldLight.copy(alpha = 0.20f),
                                    size.width / 2f - 4f,
                                    Offset(size.width / 2f, size.height / 2f),
                                    style = Stroke(2f),
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    uiState.currentCount.toString()
                                        .localizedDigits(LocalArabicNumerals.current),
                                    style = NumbersStyle,
                                    fontSize = 44.sp,
                                    color = Color.White,
                                )
                                Text(
                                    "/ ${dhikr.count}".localizedDigits(LocalArabicNumerals.current),
                                    style = NumbersStyle,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "اضغط للتسبيح",
                            fontSize = 13.sp,
                            color = LocalRafiqColors.current.inkLight,
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   HELPERS
══════════════════════════════════════════════════════════════ */

private fun getCategoryTitle(category: String): String = when (category) {
    "morning" -> "أذكار الصباح"
    "evening" -> "أذكار المساء"
    "sleep"   -> "أذكار النوم"
    "prayer"  -> "أذكار الصلاة"
    else      -> "أذكار"
}
