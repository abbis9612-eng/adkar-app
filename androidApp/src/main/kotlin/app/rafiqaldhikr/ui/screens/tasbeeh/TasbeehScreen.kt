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
import app.rafiqaldhikr.ui.components.FirstHint
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.graphicsLayer
import app.rafiqaldhikr.R
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
 *  التوقيع: الذكرُ نفسُه هو موضعُ اللمس، وحبّةٌ تطير من موضع الإبهام إلى شَرْطها
 *
 *  والشَّرْطُ لكلِّ تسبيحةٍ لا شريطٌ مطّاط — فيُعَدُّ الباقي بالعين.
 *
   DHIKR DATA
══════════════════════════════════════════════════════════════ */

private enum class DhikrType { SUBHAN_ALLAH, ALHAMDULILLAH, ALLAHU_AKBAR, TAHLIL, HAWQALA, ISTIGHFAR }

private data class DhikrOption(
    val text: String,
    val tashkeel: String,
    val type: DhikrType
)

/*  الستّةُ التي يعرضها منتقي الذكر — لا ثلاثةٌ منها.
 *
 *  كان المنتقي يعرض ستّاً وهذه القائمةُ ثلاثاً، و`find { it.text == … }`
 *  يسقط إلى `DHIKR_OPTIONS[0]` عند الثلاث الأخرى. فمن اختار «لا إله إلا
 *  الله» رأى النصَّ الكبيرَ يقول «سُبْحَانَ اللَّهِ» بينما يعدّ ذكراً
 *  آخر — واللونُ لونَ التسبيح كذلك.
 */
private val DHIKR_OPTIONS = listOf(
    DhikrOption("سبحان الله",                "سُبْحَانَ اللَّهِ",                       DhikrType.SUBHAN_ALLAH),
    DhikrOption("الحمد لله",                 "الْحَمْدُ لِلَّهِ",                        DhikrType.ALHAMDULILLAH),
    DhikrOption("الله أكبر",                  "اللَّهُ أَكْبَرُ",                         DhikrType.ALLAHU_AKBAR),
    DhikrOption("لا إله إلا الله",             "لَا إِلَهَ إِلَّا اللَّهُ",                   DhikrType.TAHLIL),
    DhikrOption("لا حول ولا قوة إلا بالله",   "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",      DhikrType.HAWQALA),
    DhikrOption("أستغفر الله",                "أَسْتَغْفِرُ اللَّهَ",                      DhikrType.ISTIGHFAR),
)

@Composable
private fun DhikrOption.resolveColors(): Pair<Color, Color> {
    val rc = LocalRafiqColors.current
    return when (this.type) {
        DhikrType.SUBHAN_ALLAH -> rc.emerald to rc.emeraldPastel
        DhikrType.ALHAMDULILLAH -> rc.gold to rc.meccanBg
        DhikrType.ALLAHU_AKBAR -> rc.lightNight to rc.lightNight.copy(alpha = 0.1f)
        DhikrType.TAHLIL       -> rc.emerald to rc.emeraldPastel
        DhikrType.HAWQALA      -> rc.gold to rc.meccanBg
        DhikrType.ISTIGHFAR    -> rc.lightDusk to rc.lightDusk.copy(alpha = 0.12f)
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
            Text(stringResource(R.string.tasbeeh_milestones), fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.inkDark, style = RafiqType.body)
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
    val ar = LocalArabicNumerals.current
    val ctx = LocalContext.current

    /*  دورةُ دُبُر الصلاة — نصوصُها وأعدادُها من `adhkar_prayer.json`،
     *  فلا تُكتب في الكود ولا تُعدَّل حرفاً. */
    val cycle = remember(ctx) { PrayerCycle.steps(ctx) }
    val hundredth = remember(ctx) { PrayerCycle.hundredth(ctx) }
    val cycleCounts = remember(cycle) { cycle.map { it.count } }
    var cycleOn by rememberSaveable { mutableStateOf(false) }
    var cycleStep by rememberSaveable { mutableStateOf(0) }
    var threadMode by rememberSaveable { mutableStateOf(false) }

    val step = cycle.getOrNull(cycleStep)
    val inCycle = cycleOn && step != null

    // Find current dhikr option
    val currentDhikr = DHIKR_OPTIONS.find { it.text == state.dhikrText } ?: DHIKR_OPTIONS[0]
    val (primaryColor, pastelColor) = currentDhikr.resolveColors()
    val accent = if (inCycle) rc.emerald else primaryColor
    val shownText = step?.text ?: currentDhikr.tashkeel
    var showDhikrPicker by remember { mutableStateOf(false) }

    val cycleDone = inCycle && cycleComplete(cycleStep, state.count, cycleCounts)

    /*  تمامُ الخطوة ينتقل بعد مَهْلةٍ قصيرةٍ — لتُرى الشَّرْطةُ الأخيرةُ
     *  تمتلئ قبل أن يتبدّل النصّ. والانتقالُ بـ`setDhikr` لأنّه يحفظ
     *  الشوطَ أوّلاً: كلُّ ثلاثٍ وثلاثين شوطٌ في السجلّ لا شوطٌ واحدٌ
     *  من مئة. */
    LaunchedEffect(inCycle, cycleStep, state.count, state.target) {
        if (inCycle && state.count >= state.target) {
            val next = nextCycleStep(cycleStep, cycle.size)
            if (next != null) {
                kotlinx.coroutines.delay(430)
                cycleStep = next
                viewModel.setDhikr(cycle[next].text)
                viewModel.setTarget(cycle[next].count)
            }
        }
    }

    fun countOne() {
        if (inCycle && state.count >= state.target) return
        val before = state.count
        viewModel.increment()
        //  اهتزازٌ ثلاثُ درجات: لمسةٌ · تمامُ ذكرٍ · تمامُ المائة
        val lastStep = inCycle && nextCycleStep(cycleStep, cycle.size) == null
        haptic.performHapticFeedback(
            if (before + 1 >= state.target && lastStep) HapticFeedbackType.LongPress
            else if (before + 1 >= state.target) HapticFeedbackType.LongPress
            else HapticFeedbackType.TextHandleMove,
        )
    }

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
            RafiqTopBar(title = stringResource(R.string.tasbeeh_title)) {
                RafiqIconButton(
                    onClick = {
                        viewModel.saveSession()
                        viewModel.reset()
                    },
                    label = stringResource(R.string.tasbeeh_reset),
                ) { RafiqIcon(RIcon.Refresh, 18.dp, rc.emerald) }
                RafiqIconButton(onClick = { showDhikrPicker = true }, label = stringResource(R.string.tasbeeh_pick)) { RafiqIcon(RIcon.Edit, 18.dp, rc.emerald) }
            }

            //  تلميحٌ في موضعه — ويذكر التراجعَ لأنّه أخفى ما في الشاشة.
            FirstHint(
                "tasbeeh", R.string.hint_tasbeeh,
                Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            // ═══ DHIKR SELECTOR — Horizontal ═══
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                //  الدورةُ أوّلَ الصفّ — أشهرُ ما يُعَدّ، ودُبُرَ كلِّ صلاة
                if (cycle.isNotEmpty()) {
                    val sel = inCycle
                    Box(
                        Modifier
                            .clip(RafiqShape.card)
                            .background(if (sel) rc.emeraldPastel else rc.card)
                            .border(
                                if (sel) 2.dp else 1.dp,
                                if (sel) rc.emerald.copy(alpha = 0.5f) else rc.gold.copy(alpha = BorderIdle),
                                RafiqShape.card,
                            )
                            .clickable {
                                cycleOn = true
                                cycleStep = 0
                                viewModel.setDhikr(cycle[0].text)
                                viewModel.setTarget(cycle[0].count)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text(
                            stringResource(R.string.tasbeeh_cycle),
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            color = if (sel) rc.emerald else rc.inkDark, style = RafiqType.body,
                        )
                    }
                }
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
                                cycleOn = false
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

            // ═══ مُبدِّلُ النمط — شيءٌ جانبيٌّ فوق الذكر ═══
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ModeSwitch(threadMode) { threadMode = it }
            }

            Spacer(Modifier.height(10.dp))

            // ═══ الفعلُ الأوّل — اللمسةُ التي تعدّ ═══
            if (cycleDone && hundredth != null) {
                /*  تمامُ المائة — حدثٌ لا سطرٌ عابر. ونصُّه وفضلُه
                 *  ومصدرُه من الأصل كما هي. */
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .rafiqCard()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.tasbeeh_hundred),
                        style = RafiqType.metaS, color = rc.gold,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        hundredth.text,
                        style = RafiqType.dhikr, color = rc.ink,
                        textAlign = TextAlign.Center,
                    )
                    if (hundredth.virtue != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            hundredth.virtue,
                            style = RafiqType.bodyS, color = rc.inkMed,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${hundredth.source} · ${hundredth.grade}",
                        style = RafiqType.metaS, color = rc.inkLight,
                    )
                }
            } else if (threadMode) {
                ThreadCounter(
                    done = state.count,
                    target = state.target.coerceAtLeast(1),
                    rc = rc,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp),
                    onTap = { countOne() },
                )
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(
                            R.string.tasbeeh_left_of,
                            (state.target - state.count).coerceAtLeast(0).localized(ar),
                            state.target.localized(ar),
                        ),
                        style = RafiqType.metaS, color = rc.gold,
                    )
                }
            } else {
                WordCounter(
                    tashkeel = shownText,
                    done = state.count,
                    target = state.target.coerceAtLeast(1),
                    accent = accent,
                    firstTap = state.count == 0,
                    modifier = Modifier.fillMaxWidth(),
                    onTap = { countOne() },
                )
            }

            Spacer(Modifier.height(14.dp))

            // ═══ تراجع — فلا معنى لسؤال «هل حُسِبت؟» بلا مخرجٍ للغلط ═══
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val can = state.count > 0
                    Box(
                        Modifier
                            .clip(RafiqShape.chip)
                            .border(1.dp, rc.cardBorder, RafiqShape.chip)
                            .graphicsLayer { alpha = if (can) 1f else 0.38f }
                            .clickable(enabled = can) {
                                viewModel.decrement()
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            .padding(horizontal = 16.dp, vertical = 9.dp),
                    ) {
                        Text(stringResource(R.string.tasbeeh_undo),
                            style = RafiqType.metaS, color = rc.inkMed)
                    }
                    if (inCycle && step != null) {
                        Text(
                            stringResource(
                                R.string.tasbeeh_of_hundred,
                                cycleTotal(cycleStep, state.count, cycleCounts).localized(ar),
                                cycleCounts.sum().localized(ar),
                            ),
                            style = RafiqType.metaS, color = rc.inkLight,
                        )
                    }
                }
            }

            //  مصدرُ النصّ ودرجتُه — لا يُفصل عن النصّ أبداً
            if (inCycle && step != null) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "${step.source} · ${step.grade}",
                        style = RafiqType.metaS, color = rc.inkLight,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // ═══ TARGET SELECTOR — لا في الدورة: أعدادُها من الحديث ═══
            if (!inCycle) Column(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.tasbeeh_target), color = LocalRafiqColors.current.inkMed, style = RafiqType.caption)
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
        // مصدرٌ واحد: كان المنتقي يسرد ستّاً بيده والقائمةُ ثلاثاً.
        val allOptions = DHIKR_OPTIONS.map { it.text }
        AlertDialog(
            onDismissRequest = { showDhikrPicker = false },
            containerColor = LocalRafiqColors.current.card,
            shape = RafiqShape.card,
            title = {
                Text(stringResource(R.string.tasbeeh_choose), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald)
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
                    Text(stringResource(R.string.action_close), fontWeight = FontWeight.Bold, color = LocalRafiqColors.current.emerald, style = RafiqType.bodyS)
                }
            },
        )
    }
}
