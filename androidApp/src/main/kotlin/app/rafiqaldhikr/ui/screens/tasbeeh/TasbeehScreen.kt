package app.rafiqaldhikr.ui.screens.tasbeeh

import androidx.compose.animation.core.RepeatMode
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
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.BorderIdle
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.theme.stillableFloat
import app.rafiqaldhikr.ui.components.RafiqIconButton
import app.rafiqaldhikr.ui.utils.localized
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI

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
        DhikrType.ALHAMDULILLAH -> rc.gold to rc.meccanBg
        DhikrType.ALLAHU_AKBAR -> rc.lightNight to rc.lightNight.copy(alpha = 0.1f)
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
            .rafiqCard()
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.width(4.dp).height(18.dp)
                    .clip(RafiqShape.chip)
                    .background(rc.gold)
            )
            Text("محطات الإنجاز", fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark, style = RafiqType.body)
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
                        Text("$m".localizedDigits(LocalArabicNumerals.current), fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald, style = RafiqType.micro)
                    }
                }

                // Progress bar
                Column(Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RafiqShape.chip)
                            .background(accentColor.copy(alpha = 0.12f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RafiqShape.chip)
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
                    Text("${count}/$m".localizedDigits(LocalArabicNumerals.current),
                        color = rc.inkMed, style = RafiqType.micro)
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
    val pulseAlpha by stillableFloat(0.15f, 0.4f, 2000, FastOutSlowInEasing, RepeatMode.Reverse, label = "pulseAlpha")

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
            RafiqTopBar(title = "المسبحة") {
                RafiqIconButton(
                    onClick = {
                        viewModel.saveSession()
                        viewModel.reset()
                    },
                    label = "تصفير العدّاد",
                ) { RafiqIcon(RIcon.Refresh, 18.dp, rc.emerald) }
                RafiqIconButton(onClick = { showDhikrPicker = true }, label = "اختيار الذكر") { RafiqIcon(RIcon.Edit, 18.dp, rc.emerald) }
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
                            .clip(RafiqShape.card)
                            .background(if (selected) optPastel else rc.card)
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) optPrimary.copy(alpha = 0.5f) else rc.gold.copy(alpha = BorderIdle),
                                RafiqShape.card
                            )
                            .clickable {
                                viewModel.setDhikr(opt.text)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(opt.text,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) optPrimary else rc.inkDark, style = RafiqType.body)
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

                /*  ثلاثٌ وثلاثون حبّة — لا قوسُ تقدّمٍ مجرَّد.
                 *
                 *  المسبحةُ ثلاثٌ وثلاثون حبّةً تُدار، فحلقتُها هي الشيءُ
                 *  نفسُه لا رسمٌ يمثّله. والمضيئةُ الأخيرةُ أكبرُ وذهبيّة،
                 *  فتُرى الحركةُ قبل أن يُقرأ الرقم.
                 *
                 *  وكانت الشاشةُ دائرتين: حلقةٌ باهتةٌ فيها العدّاد صغيراً،
                 *  وتحتها دائرةٌ خضراءُ ضخمةٌ مكتوبٌ عليها «اضغط» — فأكبرُ
                 *  عنصرٍ أمرٌ وأصغرُها الجواب. صارت واحدة: الرقمُ هو البطل،
                 *  والحلقةُ نفسُها مساحةُ اللمس. */
                MisbahaRing(
                    count = state.count,
                    target = state.target.coerceAtLeast(1),
                    scale = tapScale,
                    rc = LocalRafiqColors.current,
                    onTap = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.increment()
                    },
                )
                LaunchedEffect(state.count) {
                    kotlinx.coroutines.delay(120)
                    isPressed = false
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

            // ═══ COMPLETION BADGE ═══
            if (state.isCompleted) {
                Spacer(Modifier.height(16.dp))
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        Modifier
                            .clip(RafiqShape.card)
                            .background(pastelColor)
                            .border(1.5.dp, primaryColor.copy(alpha = 0.3f), RafiqShape.card)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RafiqIcon(RIcon.Check, 16.dp, primaryColor)
                        Text("أحسنت! اكتمل الذكر",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor, style = RafiqType.bodyS)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ═══ TARGET SELECTOR ═══
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("العدد المستهدف", color = LocalRafiqColors.current.inkMed, style = RafiqType.caption)
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(33, 99, 100, 1000).forEach { t ->
                        val sel = state.target == t
                        Box(
                            Modifier
                                .clip(RafiqShape.item)
                                .background(if (sel) primaryColor else rc.card)
                                .border(
                                    1.dp,
                                    if (sel) primaryColor else rc.gold.copy(alpha = BorderIdle),
                                    RafiqShape.item
                                )
                                .clickable { viewModel.setTarget(t) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("$t",
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = if (sel) Color.White else rc.inkDark, style = RafiqType.bodyS)
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
            shape = RafiqShape.card,
            title = {
                Text("اختر الذكر", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allOptions.forEach { text ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RafiqShape.item)
                                .background(
                                    if (text == state.dhikrText) rc.emeraldPastel else rc.bg
                                )
                                .border(
                                    if (text == state.dhikrText) 1.5.dp else 0.dp,
                                    if (text == state.dhikrText) rc.emerald.copy(alpha = 0.3f) else Color.Transparent,
                                    RafiqShape.item
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
                        .clip(RafiqShape.item)
                        .background(rc.emeraldPastel)
                        .clickable { showDhikrPicker = false }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald, style = RafiqType.bodyS)
                }
            },
        )
    }
}

/* ── حلقةُ المسبحة ────────────────────────────────────────────── */

@Composable
private fun MisbahaRing(
    count: Int,
    target: Int,
    scale: Float,
    rc: app.rafiqaldhikr.ui.theme.RafiqPalette,
    onTap: () -> Unit,
) {
    val ar = LocalArabicNumerals.current
    val laps = count / target
    val cycle = if (count == 0) 0 else (count % target).let { if (it == 0) target else it }
    val lit = (cycle.toFloat() / target * BEADS).toInt()

    Box(
        Modifier
            .size(252.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f - 12.dp.toPx()
            for (i in 0 until BEADS) {
                val a = (i.toFloat() / BEADS) * 2f * PI.toFloat() - PI.toFloat() / 2f
                val p = Offset(c.x + r * cos(a), c.y + r * sin(a))
                val on = i < lit
                val cur = i == lit - 1
                if (cur) {
                    drawCircle(rc.goldLight.copy(alpha = 0.22f), 11.dp.toPx(), p)
                    drawCircle(rc.goldLight, 8.dp.toPx(), p)
                } else {
                    drawCircle(if (on) rc.emerald else rc.divider, 5.dp.toPx(), p)
                }
            }
        }
        Column(
            Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(rc.card)
                .border(1.dp, rc.cardBorder, CircleShape),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("عددُك", style = RafiqType.caption, color = rc.gold)
            // الصفرُ العربيُّ «٠» نقطةٌ صغيرة، فيبدو وحده عطباً لا رقماً
            Text(
                if (count == 0) "ابدأ" else count.localized(ar),
                style = if (count == 0) RafiqType.titleXL else NumbersStyle,
                fontSize = if (count == 0) 30.sp else 64.sp,
                color = rc.ink,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                if (laps > 0) "أتممتَ ${laps.localized(ar)} " +
                    (if (laps == 1) "دورة" else "دورات")
                else "علامةُ الوِرد عند ${target.localized(ar)}",
                style = RafiqType.caption,
                color = rc.inkMed,
            )
        }
    }
}

/** حبّاتُ المسبحة — ثلاثٌ وثلاثون، وهي عددُها المعروف. */
private const val BEADS = 33
