package app.rafiqaldhikr.ui.screens.tasbeeh

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.progressSpec
import app.rafiqaldhikr.ui.theme.tapSpec
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import androidx.compose.material3.Text
import kotlin.math.max
import kotlin.math.min

/* ══════════════════════════════════════════════════════════════════════
 *  عدّادا المسبحة — ونمطُهما مسألةُ **جوابِ اللمسة** لا شكلِ الزينة
 *
 *  كان الفعلُ الأوّل قرصاً في وسطه رقمٌ وحولَه قوسُ تقدّم. وذلك شكلٌ
 *  يصلح لعدِّ أكوابِ الماء بتبديل الكلمات — فيرسب في اختبار النسخ
 *  المكتوب في `AGENTS.md`. والأسوأُ أنّه **لا يُجيب**: من ضغط بإبهامه
 *  وعينُه نصفُ مغمضةٍ لا يعرف أحُسِبت ضغطتُه أم لا، ولا له مخرجٌ إن
 *  حُسِبت مرّتين.
 *
 *  فالجوابُ هنا ثلاثُ قنواتٍ معاً: **أثرٌ من موضع الإبهام بعينه**
 *  (حبّةٌ تخرج من إحداثيّات لمستك وتطير إلى الشَّرْط فتُشعله)، و**عددٌ
 *  مرئيٌّ لا شريطٌ مطّاط** (ثلاثٌ وثلاثون شَرْطةً تُعَدّ بالعين)،
 *  و**اهتزازٌ ثلاثُ درجات** يُدبِّره النادي.
 * ══════════════════════════════════════════════════════════════════════ */

/** رحلةُ حبّةٍ واحدةٍ من موضع الإبهام إلى الشَّرْط التالي. */
private data class Flight(val id: Long, val from: Offset, val to: Offset)

/* ── نمطُ «الكلمة»: الذكرُ نفسُه هو موضعُ اللمس ───────────────────── */

@Composable
fun WordCounter(
    /** النصُّ المشكَّل — من الأصل لا من الكود. */
    tashkeel: String,
    done: Int,
    target: Int,
    accent: Color,
    firstTap: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val density = LocalDensity.current

    var root by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var wordAt by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var tally by remember { mutableStateOf<Rect?>(null) }
    var flight by remember { mutableStateOf<Flight?>(null) }
    var pressed by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(0L) }

    val prog = remember { Animatable(1f) }
    /*  المواصفةُ تُقرأ هنا لا داخل [LaunchedEffect]: كتلتُه معلَّقةٌ لا
     *  تركيبيّة، و`progressSpec` تركيبيّةٌ لأنّها تقرأ «تقليل الحركة».
     *  فقراءتُها هنا هي التي تجعل الإعدادَ مسموعاً. */
    val flySpec = progressSpec<Float>(290)
    LaunchedEffect(flight?.id) {
        if (flight != null) {
            prog.snapTo(0f)
            prog.animateTo(1f, flySpec)
        }
    }
    val press by androidx.compose.animation.core.animateFloatAsState(
        if (pressed) 0.955f else 1f, tapSpec(), label = "wordPress",
    )
    LaunchedEffect(done) {
        if (pressed) { kotlinx.coroutines.delay(130); pressed = false }
    }

    Box(modifier.onGloballyPositioned { root = it }) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            /*  الذكرُ هو الزرّ — أعرضُ هدفٍ تحتمله الشاشة، ولا زرَّ بجانبه.
             *  ويُلتقَط موضعُ اللمس بإحداثيّاته لتخرج الحبّةُ من تحت
             *  الإبهام نفسِه لا من وسط الشاشة. */
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 148.dp)
                    .onGloballyPositioned { wordAt = it }
                    .pointerInput(target, done) {
                        detectTapGestures { local ->
                            pressed = true
                            val r = root
                            val w = wordAt
                            val t = tally
                            if (r != null && w != null && t != null && done < target) {
                                val from = r.localPositionOf(w, local)
                                nextId += 1
                                flight = Flight(nextId, from, notchCenter(t, done, target))
                            }
                            onTap()
                        }
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tashkeel,
                    style = RafiqType.dhikrL,
                    color = rc.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.scale(press),
                )
            }

            Box(
                Modifier
                    .width(96.dp)
                    .height(1.dp)
                    .background(rc.gold.copy(alpha = 0.32f)),
            )
            Spacer(Modifier.height(12.dp))

            Text(
                (target - done).coerceAtLeast(0).localized(ar),
                style = RafiqType.display, color = rc.ink,
            )
            Text(
                stringResource(
                    R.string.tasbeeh_left_of,
                    (target - done).coerceAtLeast(0).localized(ar), target.localized(ar),
                ),
                style = RafiqType.metaS, color = rc.gold,
            )

            Spacer(Modifier.height(14.dp))
            TallyRow(
                done = done, target = target, accent = accent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .onGloballyPositioned { c ->
                        root?.let { tally = it.localBoundingBoxOf(c) }
                    },
            )

            if (firstTap) {
                Spacer(Modifier.height(12.dp))
                /*  دعوةُ أوّلِ لمسة — تختفي بعدها ولا تعود. فالعطبُ أنّ
                 *  الشاشةَ لا تقول ماذا يُفعَل، لا أنّها ناقصةُ زينة. */
                Box(
                    Modifier
                        .clip(RafiqShape.chip)
                        .border(1.dp, rc.gold.copy(alpha = 0.45f), RafiqShape.chip)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        stringResource(R.string.tasbeeh_tap_invite),
                        style = RafiqType.metaS, color = rc.inkLight,
                    )
                }
            }
        }

        /*  الحبّةُ الطائرة — من إبهامك إلى العدد. وهي الجوابُ البصريّ:
         *  ترى موضعَ ضغطك، وترى اللمسةَ تصل. */
        val f = flight
        val p = prog.value
        if (f != null && p < 1f) {
            val x = f.from.x + (f.to.x - f.from.x) * p
            val y = f.from.y + (f.to.y - f.from.y) * easeOutQuad(p)
            Box(
                Modifier
                    .offset {
                        IntOffset((x - with(density) { 7.dp.toPx() }).toInt(),
                                  (y - with(density) { 7.dp.toPx() }).toInt())
                    }
                    .size(14.dp)
                    .graphicsLayer { val s = 1f - 0.55f * p; scaleX = s; scaleY = s; alpha = 1f - p * p }
                    .clip(CircleShape)
                    .background(rc.goldLight)
                    .border(1.dp, rc.gold, CircleShape),
            )
        }
    }
}

/** مركزُ الشَّرْط رقم [index] — ومن اليمين، فالصفُّ عربيّ. */
private fun notchCenter(rect: Rect, index: Int, target: Int): Offset {
    val n = target.coerceAtLeast(1)
    val w = rect.width / n
    return Offset(rect.right - (index + 0.5f) * w, rect.top + rect.height * 0.55f)
}

private fun easeOutQuad(t: Float) = 1f - (1f - t) * (1f - t)

/* ── الشَّرْطات: عددٌ يُعَدّ بالعين لا شريطٌ مطّاط ──────────────────── */

@Composable
fun TallyRow(done: Int, target: Int, accent: Color, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    /*  شريطُ التقدّم المطّاطُ يقول «نحو الثلثين» ولا يقول «بقيت إحدى
     *  عشرة». والشَّرْطُ لكلِّ تسبيحةٍ يُعَدّ بالعين، وهو ما يُثبت أنّ
     *  اللمسةَ حُسِبت. ولا يُرسم أكثرُ من مئةٍ — فوقها تُقرأ خطّاً. */
    val n = target.coerceIn(1, 100)
    Row(
        modifier.height(26.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        for (i in 0 until n) {
            val on = i < done
            val nx = i == done
            Box(
                Modifier
                    .weight(1f)
                    .height(if (on) 24.dp else if (nx) 17.dp else 11.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            on -> accent
                            nx -> rc.goldLight
                            else -> rc.divider
                        },
                    ),
            )
        }
    }
}

/* ── نمطُ «الخيط»: حَبَبٌ تنزل تحت الإبهام ────────────────────────── */

@Composable
fun ThreadCounter(
    done: Int,
    target: Int,
    rc: RafiqPalette,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    /*  المسبحةُ لا تُنقَر — تُدار: تُدفَع حبّةٌ تحت الإبهام فتنزل. فموضعُ
     *  الحبّة هو العدد، تعرف أين أنت بلا أن تقرأ رقماً. والأخيرةُ
     *  مستطيلةٌ: هي **الإمام**، الحبّةُ الفاصلةُ التي تُحَسُّ باليد
     *  فتُعلِم أنّ الشوطَ تمّ. */
    val slide by androidx.compose.animation.core.animateFloatAsState(
        done.toFloat(), progressSpec(170), label = "beadSlide",
    )
    Box(
        modifier
            .clip(RafiqShape.card)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val palmY = size.height * 0.70f
            val gap = 30.dp.toPx()
            val cx = size.width / 2f
            val shift = palmY - slide * gap
            val n = target.coerceAtLeast(1)

            drawLine(
                rc.gold.copy(alpha = 0.45f),
                Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 1.6.dp.toPx(),
            )
            //  لا تُرسم مئةُ حبّةٍ وتسعُها خارجَ الشاشة — ما يُرى فقط
            val lo = max(0, (slide - 10).toInt())
            val hi = min(n - 1, (slide + 10).toInt())
            for (i in lo..hi) {
                val y = shift + i * gap
                val isImam = i == n - 1
                val cur = i == done
                when {
                    isImam -> drawRoundRect(
                        color = rc.emeraldFill,
                        topLeft = Offset(cx - 8.5.dp.toPx(), y - 11.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(17.dp.toPx(), 22.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()),
                        alpha = if (i < done) 0.3f else 1f,
                    )
                    cur -> {
                        drawCircle(rc.goldLight.copy(alpha = 0.22f), 15.dp.toPx(), Offset(cx, y))
                        drawCircle(rc.goldLight, 9.dp.toPx(), Offset(cx, y))
                    }
                    else -> drawCircle(
                        if (i < done) rc.emerald.copy(alpha = 0.3f) else rc.divider,
                        6.5.dp.toPx(), Offset(cx, y),
                    )
                }
            }
            //  خطُّ الإبهام — حيث تُدفَع الحبّة
            drawLine(
                rc.gold.copy(alpha = 0.30f),
                Offset(size.width * 0.12f, palmY + 20.dp.toPx()),
                Offset(size.width * 0.88f, palmY + 20.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

/* ── مُبدِّلُ النمط — شيءٌ جانبيٌّ لا يزاحم الكلمة ──────────────────── */

@Composable
fun ModeSwitch(threadMode: Boolean, modifier: Modifier = Modifier, onPick: (Boolean) -> Unit) {
    val rc = LocalRafiqColors.current
    Row(
        modifier
            .clip(RafiqShape.chip)
            .border(1.dp, rc.cardBorder, RafiqShape.chip),
    ) {
        Seg(stringResource(R.string.tasbeeh_mode_word), !threadMode) { onPick(false) }
        Seg(stringResource(R.string.tasbeeh_mode_thread), threadMode) { onPick(true) }
    }
}

@Composable
private fun Seg(label: String, on: Boolean, onClick: () -> Unit) {
    val rc = LocalRafiqColors.current
    Box(
        Modifier
            .background(if (on) rc.emeraldFill else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = RafiqType.metaS, color = if (on) rc.onEmeraldFill else rc.inkLight)
    }
}
