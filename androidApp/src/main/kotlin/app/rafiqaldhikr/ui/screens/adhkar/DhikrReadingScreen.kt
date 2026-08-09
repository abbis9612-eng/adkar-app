package app.rafiqaldhikr.ui.screens.adhkar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.ErrorState
import app.rafiqaldhikr.ui.components.LoadingState
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqIconButton
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.*
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import org.koin.androidx.compose.koinViewModel

/* ═══════════════════════════════════════════════════════════════════
   قراءة الذكر — الشاشة التي تُقضى فيها الدقائق فعلاً

   كانت بطاقةً داخل صفحة: شريط تقدّم علوي، ثم بطاقة بيضاء فيها النصّ،
   ثم دائرة زمرّدية للعدّ، ثم زخرفة هندسية تدور خلف كل شيء.

   لكن هذه الشاشة تُفتح وأنت تمشي، أو تنتظر، أو مستلقٍ قبل النوم.
   فما تحتاجه: نصٌّ كبير مقروء، وهدفُ لمسٍ لا تُخطئه.

     • النصّ في وسط الورقة بلا بطاقة — الورقة نفسها هي السطح.
     • العدّ بضغطة في أيّ مكان من الشاشة، لا هدفاً صغيراً تصيبه.
     • النقاط أعلى الصفحة تقول كم ذكراً بقي — أدقّ من شريط مجرّد.
     • حلقة العدّ تُظهر الرقم لا تحبسه في دائرة ملوّنة.
═══════════════════════════════════════════════════════════════════ */

@Composable
fun DhikrReadingScreen(
    category:      String,
    navController: NavHostController,
    viewModel:     DhikrReadingViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(category) { viewModel.loadCategory(category) }

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        when {
            uiState.isLoading -> LoadingState()

            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.loadCategory(category) },
            )

            uiState.isAllCompleted -> LaunchedEffect(Unit) {
                navController.navigate(RafiqRoute.Celebration.route)
            }

            uiState.adhkar.isNotEmpty() -> {
                val dhikr = uiState.adhkar[uiState.currentIndex]
                val done  = uiState.currentCount >= dhikr.count

                Column(
                    Modifier
                        .fillMaxSize()
                        // كل الشاشة هدف اللمس — لا زرّ صغير
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.tap()
                        }
                        .statusBarsPadding()
                        .semantics {
                            contentDescription =
                                "اضغط في أيّ مكان للعدّ. ${uiState.currentCount} من ${dhikr.count}"
                        },
                ) {
                    TopRow(
                        title = getCategoryTitle(category),
                        onBack = { navController.popBackStack() },
                    )

                    Beads(
                        total   = uiState.adhkar.size,
                        current = uiState.currentIndex,
                    )

                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            dhikr.textAr,
                            style = RafiqType.dhikr,
                            color = rc.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(14.dp))

                        Text(
                            listOfNotNull(
                                dhikr.source.takeIf { it.isNotBlank() },
                                dhikr.sourceGrade.takeIf { it.isNotBlank() },
                            ).joinToString(" · "),
                            style = RafiqType.caption,
                            color = rc.inkMed,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (dhikr.virtue.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                dhikr.virtue,
                                style = RafiqType.bodyS,
                                color = rc.inkMed,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(Modifier.height(34.dp))

                        CountRing(uiState.currentCount, dhikr.count)

                        Spacer(Modifier.height(14.dp))

                        Text(
                            when {
                                !done -> "اضغط في أيّ مكان للعدّ"
                                uiState.currentIndex < uiState.adhkar.lastIndex -> "تمّ — اضغط للتالي"
                                else -> "تمّ القسم كاملاً"
                            },
                            style = RafiqType.caption,
                            color = if (done) rc.gold else rc.inkMed,
                        )

                        Spacer(Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

/* ── الرأس: عنوان وزرّ رجوع، لا غير ───────────────────────────── */

@Composable
private fun TopRow(title: String, onBack: () -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = RafiqType.titleM, color = rc.emerald)
        RafiqIconButton(onClick = onBack, label = "رجوع") {
            RafiqIcon(RIcon.ChevronRight, 18.dp, rc.emerald)
        }
    }
}

/* ── حبّات المسبحة: ذكرٌ لكل حبّة ─────────────────────────────── */

@Composable
private fun Beads(total: Int, current: Int) {
    val rc = LocalRafiqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .semantics { contentDescription = "الذكر ${current + 1} من $total" },
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // القوائم الطويلة لا تُرسم حبّةً حبّة — تُلخَّص
        if (total <= 24) {
            repeat(total) { i ->
                Bead(state = if (i < current) 2 else if (i == current) 1 else 0)
            }
        } else {
            Text(
                "${current + 1} / $total".localizedDigits(LocalArabicNumerals.current),
                style = NumbersStyle, fontSize = RafiqType.caption.fontSize, color = rc.inkMed,
            )
        }
    }
}

@Composable
private fun Bead(state: Int) {
    val rc = LocalRafiqColors.current
    Canvas(
        Modifier
            .height(8.dp)
            .width(if (state == 1) 18.dp else 6.dp)
    ) {
        val c = when (state) {
            2    -> rc.gold
            1    -> rc.emerald
            else -> rc.divider
        }
        drawRoundRect(
            color = c,
            topLeft = Offset(0f, size.height / 2f - 3.dp.toPx()),
            size = Size(size.width, 6.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        )
    }
}

/* ── حلقة العدّ: الرقم ظاهر، والحلقة تحيطه ─────────────────────── */

@Composable
private fun CountRing(count: Int, target: Int) {
    val rc = LocalRafiqColors.current
    val pct by animateFloatAsState(
        targetValue = (count.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = progressSpec(320),
        label = "countRing",
    )
    Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val r = (size.minDimension - stroke) / 2f
            val tl = Offset(size.width / 2f - r, size.height / 2f - r)
            val sz = Size(r * 2, r * 2)
            drawArc(rc.divider, 0f, 360f, false, tl, sz, style = Stroke(stroke))
            if (pct > 0.004f) {
                drawArc(
                    rc.gold, -90f, 360f * pct, false, tl, sz,
                    style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                count.toString().localizedDigits(LocalArabicNumerals.current),
                style = RafiqType.display, color = rc.ink,
            )
            Text(
                "من ${target.toString().localizedDigits(LocalArabicNumerals.current)}",
                style = RafiqType.caption, color = rc.inkMed,
            )
        }
    }
}

/* ── مساعد ────────────────────────────────────────────────────── */

private fun getCategoryTitle(category: String): String = when (category) {
    "morning"   -> "أذكار الصباح"
    "evening"   -> "أذكار المساء"
    "sleep"     -> "أذكار النوم"
    "wake"      -> "أذكار الاستيقاظ"
    "prayer"    -> "أذكار بعد الصلاة"
    "istighfar" -> "الاستغفار"
    "misc"      -> "أذكار متفرقة"
    else        -> "الأذكار"
}
