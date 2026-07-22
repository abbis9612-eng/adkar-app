package app.rafiqaldhikr.ui.screens.tasbeeh

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.koin.androidx.compose.koinViewModel
import app.rafiqaldhikr.ui.components.IcoMisbaha
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.utils.localizedDigits
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.NumbersStyle
import kotlin.math.*

/* Colors are now provided by LocalRafiqColors from RafiqPalette.kt */

/* Colors are now provided by LocalRafiqColors from RafiqPalette.kt */

/* ══════════════════════════════════════════════════════════════
   DHIKR DATA
══════════════════════════════════════════════════════════════ */

private enum class DhikrType { SUBHAN_ALLAH, ALHAMDULILLAH, ALLAHU_AKBAR }

private data class DhikrOption(
    val text: String,
    val tashkeel: String,
    val type: DhikrType
)

private val DHIKR_OPTIONS = listOf(
    DhikrOption("سبحان الله",  "سُبْحَانَ اللَّهِ",       DhikrType.SUBHAN_ALLAH),
    DhikrOption("الحمد لله",   "الْحَمْدُ لِلَّهِ",       DhikrType.ALHAMDULILLAH),
    DhikrOption("الله أكبر",   "اللَّهُ أَكْبَرُ",        DhikrType.ALLAHU_AKBAR),
)

@Composable
private fun DhikrOption.resolveColors(): Pair<Color, Color> {
    val rc = LocalRafiqColors.current
    return when (this.type) {
        DhikrType.SUBHAN_ALLAH -> rc.emerald to rc.emeraldPastel
        DhikrType.ALHAMDULILLAH -> rc.brownAccent to rc.meccanBg
        DhikrType.ALLAHU_AKBAR -> rc.blueAccent to rc.blueAccent.copy(alpha = 0.1f)
    }
}


/* ══════════════════════════════════════════════════════════════
   ARC PROGRESS — Circular Canvas progress indicator
══════════════════════════════════════════════════════════════ */

@Composable
private fun ArcProgress(
    value: Int,
    max: Int,
    sizeDp: Dp = 200.dp,
    strokeColor: Color,
    bgColor: Color = LocalRafiqColors.current.divider,
    strokeW: Dp = 10.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val pct = (value.toFloat() / max.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val animPct by animateFloatAsState(
        pct, tween(700, easing = FastOutSlowInEasing), label = "arcPct"
    )

    Box(modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = strokeW.toPx()
            val r = (size.minDimension - sw * 2) / 2f
            val topLeft = Offset(size.width / 2f - r, size.height / 2f - r)
            val arcSize = Size(r * 2, r * 2)
            drawArc(bgColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(strokeColor, -90f, 360f * animPct, false, topLeft, arcSize, style = Stroke(sw, cap = StrokeCap.Round))
        }
        content()
    }
}

/* ══════════════════════════════════════════════════════════════
   PILL BUTTON
══════════════════════════════════════════════════════════════ */

@Composable
private fun PillBtn(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rc = LocalRafiqColors.current
    Box(
        modifier
            .size(40.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.13f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

/* ══════════════════════════════════════════════════════════════
   MILESTONE CARD
══════════════════════════════════════════════════════════════ */

@Composable
private fun MilestoneCard(count: Int, target: Int, accentColor: Color) {
    val rc = LocalRafiqColors.current
    val milestones = listOf(33, 66, 99)

    Column(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(rc.card)
            .border(1.dp, rc.gold.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.width(4.dp).height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(rc.gold)
            )
            Text("محطات الإنجاز", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark)
        }

        Spacer(Modifier.height(14.dp))

        milestones.forEach { m ->
            val progress = (count.toFloat() / m.toFloat()).coerceIn(0f, 1f)
            val done = count >= m

            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Milestone number
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (done) accentColor else rc.emeraldPastel),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        RafiqIcon(RIcon.Check, 12.dp, Color.White)
                    } else {
                        Text("$m".localizedDigits(LocalArabicNumerals.current), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
                    }
                }

                // Progress bar
                Column(Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentColor.copy(alpha = 0.12f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accentColor.copy(alpha = 0.6f), accentColor)
                                    )
                                )
                        )
                    }
                }

                // Status
                if (done) {
                    RafiqIcon(RIcon.Check, 14.dp, accentColor)
                } else {
                    Text(
                        "${count}/$m".localizedDigits(LocalArabicNumerals.current),
                        fontSize = 11.sp,
                        color = rc.inkMed,
                    )
                }
            }
        }
    }
}

/* ══════════════════════════════════════════════════════════════
   MAIN TASBEEH SCREEN
══════════════════════════════════════════════════════════════ */

@Composable
fun TasbeehScreen(
    navController: NavHostController,
    viewModel: TasbeehViewModel = koinViewModel()
) {
    val rc = LocalRafiqColors.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Find current dhikr option
    val currentDhikr = DHIKR_OPTIONS.find { it.text == state.dhikrText } ?: DHIKR_OPTIONS[0]
    val (primaryColor, pastelColor) = currentDhikr.resolveColors()
    var showDhikrPicker by remember { mutableStateOf(false) }

    // Tap animation
    var isPressed by remember { mutableStateOf(false) }
    val tapScale by animateFloatAsState(
        if (isPressed) 0.93f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "tapScale"
    )

    // Pulse animation for glow
    val tr = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by tr.animateFloat(
        0.15f, 0.4f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    val scrollState = rememberScrollState()

    Box(
        Modifier.fillMaxSize().background(rc.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // ═══ TOP BAR ═══
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left: Reset + Edit
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillBtn(onClick = {
                        viewModel.saveSession()
                        viewModel.reset()
                    }) { RafiqIcon(RIcon.Refresh, 18.dp, rc.emerald) }
                    PillBtn(onClick = { showDhikrPicker = true }) { RafiqIcon(RIcon.Edit, 18.dp, rc.emerald) }
                }

                // Title
                Text("المسبحة", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            }

            // ═══ DHIKR SELECTOR — Horizontal ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DHIKR_OPTIONS.forEach { opt ->
                    val selected = opt.text == state.dhikrText
                    val (optPrimary, optPastel) = opt.resolveColors()
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) optPastel else rc.card)
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) optPrimary.copy(alpha = 0.5f) else rc.gold.copy(alpha = 0.10f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                viewModel.setDhikr(opt.text)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            opt.text,
                            fontSize = 16.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) optPrimary else rc.inkDark,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ═══ ARC PROGRESS ═══
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // Glow behind
                Box(
                    Modifier
                        .size(220.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .background(
                            Brush.radialGradient(
                                listOf(primaryColor.copy(alpha = 0.18f), Color.Transparent),
                                radius = 350f
                            ),
                            CircleShape
                        )
                )

                ArcProgress(
                    value = state.count,
                    max = state.target.coerceAtLeast(1),
                    sizeDp = 200.dp,
                    strokeColor = primaryColor,
                    bgColor = primaryColor.copy(alpha = 0.12f),
                    strokeW = 10.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${state.count}".localizedDigits(LocalArabicNumerals.current),
                            style = NumbersStyle,
                            fontSize = 56.sp,
                            color = primaryColor,
                        )
                        Text(
                            "مرة",
                            fontSize = 14.sp,
                            color = LocalRafiqColors.current.inkMed,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ═══ DHIKR TEXT WITH TASHKEEL ═══
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    currentDhikr.tashkeel,
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 56.sp,
                        brush = Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.7f),
                                primaryColor,
                                primaryColor.copy(alpha = 0.7f),
                            )
                        ),
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ═══ TAP BUTTON ═══
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(160.dp)
                        .scale(tapScale)
                        .shadow(20.dp, CircleShape, ambientColor = primaryColor.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(primaryColor.copy(alpha = 0.85f), primaryColor),
                                radius = 300f
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            isPressed = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.increment()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    // Release press after short delay
                    LaunchedEffect(state.count) {
                        kotlinx.coroutines.delay(120)
                        isPressed = false
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IcoMisbaha(40.dp, Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "اضغط",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            // ═══ COMPLETION BADGE ═══
            if (state.isCompleted) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pastelColor)
                            .border(1.5.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RafiqIcon(RIcon.Check, 16.dp, primaryColor)
                        Text(
                            "أحسنت! اكتمل الذكر",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ TARGET SELECTOR ═══
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("العدد المستهدف", fontSize = 12.sp, color = LocalRafiqColors.current.inkMed)
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(33, 99, 100, 1000).forEach { t ->
                        val sel = state.target == t
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (sel) primaryColor else rc.card)
                                .border(
                                    1.dp,
                                    if (sel) primaryColor else rc.gold.copy(alpha = 0.15f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setTarget(t) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$t",
                                fontSize = 14.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = if (sel) Color.White else rc.inkDark,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ MILESTONES ═══
            MilestoneCard(
                count = state.count,
                target = state.target,
                accentColor = primaryColor,
            )

            Spacer(Modifier.height(28.dp))
        }
    }

    // ═══ DHIKR PICKER DIALOG ═══
    if (showDhikrPicker) {
        val allOptions = listOf(
            "سبحان الله",
            "الحمد لله",
            "الله أكبر",
            "لا إله إلا الله",
            "لا حول ولا قوة إلا بالله",
            "أستغفر الله",
        )
        AlertDialog(
            onDismissRequest = { showDhikrPicker = false },
            containerColor = LocalRafiqColors.current.card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("اختر الذكر", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allOptions.forEach { text ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (text == state.dhikrText) rc.emeraldPastel else rc.bg
                                )
                                .border(
                                    if (text == state.dhikrText) 1.5.dp else 0.dp,
                                    if (text == state.dhikrText) rc.emerald.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    viewModel.setDhikr(text)
                                    showDhikrPicker = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text,
                                fontSize = 17.sp,
                                fontWeight = if (text == state.dhikrText) FontWeight.Bold else FontWeight.Normal,
                                color = if (text == state.dhikrText) rc.emerald else rc.inkDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(rc.emeraldPastel)
                        .clickable { showDhikrPicker = false }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("إغلاق", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
                }
            },
        )
    }
}
