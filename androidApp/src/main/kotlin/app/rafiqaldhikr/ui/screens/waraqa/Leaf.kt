package app.rafiqaldhikr.ui.screens.waraqa

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.progressSpec
import app.rafiqaldhikr.ui.theme.tapSpec
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/* ═══════════════════════════════════════════════════════════════════
   الورقة — ورقةُ تقويم الحائط

   ورقةٌ من رزنامةٍ معلّقة: ثقبا التعليق وخطُّ القطع، وتحتها الأوراقُ
   الباقية، وعليها التاريخُ الهجريُّ كبيراً ومواقيتُ مدينتك. وهو شيءٌ
   في بيوتنا، لا قالبُ تطبيق.

   والفعلُ الأوّلُ عليها: تلمس الصلاةَ فتُحاط بدائرةِ قلمٍ بخطّ اليد.
   لا مربّعَ اختيار ولا علامةَ صحّ — بل ما يفعله الواحدُ بقلمه على
   ورقة التقويم. والدائرةُ تُرسم حول الرقم لا فوقه: كانت في النموذج
   الأوّل تشطب «١١:٤٦» فلا يُقرأ.

   وبعد العشاء تنقلب الورقة: على ظهرها محاسبةُ اليوم وأسبوعُك.
═══════════════════════════════════════════════════════════════════ */

/** حالُ الصلاة على الورقة — ما يُكتب تحت وقتها. */
enum class PrayerSlot { NONE, NOW, NEXT }

data class LeafPrayer(
    val key: String,
    @StringRes val name: Int,
    /** الوقتُ مكتوباً، أو `null` بلا مدينة — والصلاةُ تُحاط بالقلم مع ذلك. */
    val time: String?,
    val prayed: Boolean,
    val slot: PrayerSlot,
    /** «بعد ١س ٥٨د» للقادمة، و«يبقى ٢٠د» للحاضرة إن قرب خروجُها. */
    val hint: String? = null,
)

data class AccountRow(val label: String, val value: String, val done: Boolean)

/** شكلُ الورقة: زاويتا التعليق ضيّقتان، والزاويةُ الواحدةُ الأوسعُ أسفلَها. */
private val LeafShape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomEnd = 20.dp, bottomStart = 28.dp)

@Composable
fun DayLeaf(
    hijriDay: Int,
    hijriMonthYear: String,
    dateLine: String,
    prayers: List<LeafPrayer>,
    onPrayer: (key: String, prayed: Boolean) -> Unit,
    flipped: Boolean,
    onFlip: () -> Unit,
    front: @Composable ColumnScope.() -> Unit,
    accountRows: List<AccountRow>,
    week: List<LeafViewModel.WeekDay>,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    val complete = prayers.size == 5 && prayers.all { it.prayed }
    val angle by animateFloatAsState(if (flipped) 180f else 0f, progressSpec(700), label = "leafFlip")

    Box(modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 12.dp)) {
        // الأوراقُ الباقيةُ تحتها — حافّتان تُطلّان من أسفل
        LeafEdge(Modifier.matchParentSize().padding(horizontal = 14.dp).offset(y = 12.dp), alpha = 0.7f)
        LeafEdge(Modifier.matchParentSize().padding(horizontal = 7.dp).offset(y = 6.dp), alpha = 1f)

        Box(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = angle
                    cameraDistance = 14f * density
                }
                .clip(LeafShape)
                .background(rc.card)
                .border(1.dp, rc.cardBorder, LeafShape),
        ) {
            if (angle <= 90f) {
                Column(Modifier.fillMaxWidth()) {
                    Binding()
                    DateRow(hijriDay, hijriMonthYear, dateLine, complete)
                    PrayerRow(prayers, onPrayer)
                    front()
                }
            } else {
                // الوجهُ الخلفيُّ مقلوبٌ مرّةً ثانيةً ليُقرأ لا ليُرى في مرآة
                Column(Modifier.fillMaxWidth().graphicsLayer { rotationY = 180f }) {
                    Binding()
                    AccountFace(accountRows, week, complete, onFlip)
                }
            }
        }
    }
}

@Composable
private fun LeafEdge(modifier: Modifier, alpha: Float) {
    val rc = LocalRafiqColors.current
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(LeafShape)
            .background(rc.chipBg)
            .border(1.dp, rc.cardBorder, LeafShape),
    )
}

/** ثقبا التعليق وخطُّ القطع. */
@Composable
private fun Binding() {
    val rc = LocalRafiqColors.current
    Box(Modifier.fillMaxWidth().height(22.dp).background(rc.chipBg)) {
        Row(
            Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(128.dp),
        ) {
            repeat(2) { Box(Modifier.size(11.dp).clip(CircleShape).background(rc.bg)) }
        }
        Canvas(Modifier.fillMaxWidth().height(1.5.dp).align(Alignment.BottomCenter)) {
            drawLine(
                rc.divider, Offset(0f, size.height / 2), Offset(size.width, size.height / 2),
                strokeWidth = size.height,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
            )
        }
    }
}

@Composable
private fun DateRow(day: Int, monthYear: String, dateLine: String, complete: Boolean) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (day > 0) {
                Text(
                    day.localized(ar),
                    style = RafiqType.display.copy(fontSize = RafiqType.display.fontSize * 2, lineHeight = RafiqType.display.fontSize * 2),
                    fontWeight = FontWeight.Black,
                    color = rc.emerald,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(monthYear.localizedDigits(ar), style = RafiqType.titleM, color = rc.ink)
                Text(dateLine.localizedDigits(ar), style = RafiqType.meta, color = rc.inkMed)
            }
        }
        Stamp(complete, Modifier.align(Alignment.CenterEnd).padding(end = 14.dp))
    }
}

/** «اكتملت ورقتك» — ختمٌ مائلٌ حين تُحاط الصلواتُ الخمس. */
@Composable
private fun Stamp(shown: Boolean, modifier: Modifier) {
    val rc = LocalRafiqColors.current
    val s by animateFloatAsState(if (shown) 1f else 0f, tapSpec(), label = "stamp")
    if (s <= 0.01f) return
    Column(
        modifier
            .scale(s)
            .rotate(-12f)
            .clip(RoundedCornerShape(12.dp))
            .background(rc.card.copy(alpha = 0.85f))
            .border(2.dp, rc.emerald, RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.leaf_complete), style = RafiqType.metaL, fontWeight = FontWeight.Bold, color = rc.emerald)
        Text(stringResource(R.string.leaf_complete_sub), style = RafiqType.metaS, color = rc.emerald)
    }
}

@Composable
private fun PrayerRow(prayers: List<LeafPrayer>, onPrayer: (String, Boolean) -> Unit) {
    val rc = LocalRafiqColors.current
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            prayers.forEachIndexed { i, p ->
                if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(rc.divider))
                PrayerCell(p, seed = i, onPrayer = onPrayer, modifier = Modifier.weight(1f))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
    }
}

@Composable
private fun PrayerCell(p: LeafPrayer, seed: Int, onPrayer: (String, Boolean) -> Unit, modifier: Modifier) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val haptic = LocalHapticFeedback.current
    val ink by animateFloatAsState(if (p.prayed) 1f else 0f, progressSpec(520), label = "penInk")
    val name = stringResource(p.name)
    val state = stringResource(if (p.prayed) R.string.leaf_prayed else R.string.leaf_not_prayed)

    Column(
        modifier
            .heightIn(min = 82.dp)
            .clickable(role = Role.Checkbox) {
                if (!p.prayed) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onPrayer(p.key, !p.prayed)
            }
            .semantics { contentDescription = name; stateDescription = state }
            .padding(top = 10.dp, bottom = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(name, style = RafiqType.meta, color = if (p.slot == PrayerSlot.NOW && !p.prayed) rc.gold else rc.inkMed,
            fontWeight = if (p.slot == PrayerSlot.NOW) FontWeight.Bold else FontWeight.Normal)
        Spacer(Modifier.height(6.dp))
        Box(contentAlignment = Alignment.Center) {
            Text(
                (p.time ?: "—").localizedDigits(ar),
                style = RafiqType.titleM,
                fontWeight = FontWeight.ExtraBold,
                color = rc.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
            )
            if (ink > 0f) PenLoop(ink, seed, Modifier.matchParentSize())
        }
        Spacer(Modifier.height(7.dp))
        val sub = when {
            p.prayed -> stringResource(R.string.leaf_prayed)
            p.hint != null -> p.hint
            p.slot == PrayerSlot.NOW -> stringResource(R.string.leaf_now)
            else -> ""
        }
        Text(
            sub.localizedDigits(ar),
            style = RafiqType.metaS,
            fontWeight = if (p.prayed || p.slot == PrayerSlot.NOW) FontWeight.Bold else FontWeight.Normal,
            color = when {
                p.prayed -> rc.emerald
                p.slot == PrayerSlot.NOW -> rc.gold
                else -> rc.inkLight
            },
        )
        if (p.slot == PrayerSlot.NOW && !p.prayed) {
            Spacer(Modifier.weight(1f))
            Box(Modifier.fillMaxWidth(0.6f).height(3.dp).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(rc.gold))
        }
    }
}

/**
 * دائرةُ قلمٍ بخطّ اليد حول الوقت.
 *
 * تُولَّد من بذرةٍ ثابتةٍ لكلّ صلاة: دائرةٌ تتجاوز نقطةَ بدئها قليلاً،
 * ونصفُ قطرٍ يرتجف بقدرٍ صغير — كما يدور قلمٌ على ورقة. والبذرةُ ثابتةٌ
 * فلا تتبدّل الدائرةُ بين فتحةٍ وفتحة. وتُرسم بمقدار [progress] على
 * طول المسار، فتظهر كأنّها تُخطّ الآن.
 */
@Composable
private fun PenLoop(progress: Float, seed: Int, modifier: Modifier) {
    val rc = LocalRafiqColors.current
    val unit = remember(seed) { penLoopPoints(seed) }
    /*  أوسعُ من الرقم: الدائرةُ تحيط ولا تشطب. */
    Canvas(modifier.graphicsLayer { scaleX = 1.22f; scaleY = 1.5f }) {
        val path = Path().apply {
            unit.forEachIndexed { i, (x, y) ->
                val px = x * size.width
                val py = y * size.height
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        val measure = PathMeasure().apply { setPath(path, false) }
        val part = Path()
        measure.getSegment(0f, measure.length * progress, part, true)
        drawPath(part, rc.emerald.copy(alpha = 0.92f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** نقاطُ الدائرة على مربّعٍ من ٠ إلى ١ — دالّةٌ محضةٌ تُختبر. */
internal fun penLoopPoints(seed: Int, n: Int = 46): List<Pair<Float, Float>> {
    var s = seed * 9301 + 49297
    fun rnd(): Float { s = (s * 9301 + 49297) % 233280; return s / 233280f }
    val a0 = -2.6f + rnd() * 0.4f
    val turn = (2 * PI).toFloat() * (1.13f + rnd() * 0.06f)
    return (0..n).map { i ->
        val t = a0 + turn * i / n
        val j = 1f + (rnd() - 0.5f) * 0.06f
        val rx = 0.47f * j
        val ry = 0.40f * j + (i.toFloat() / n) * 0.03f
        Pair(0.5f + rx * cos(t), 0.5f + ry * sin(t))
    }
}

/* ── ظهرُ الورقة: محاسبةُ اليوم ──────────────────────────────── */

@Composable
private fun AccountFace(
    rows: List<AccountRow>,
    week: List<LeafViewModel.WeekDay>,
    complete: Boolean,
    onFlip: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val names = androidx.compose.ui.platform.LocalContext.current.resources.getStringArray(R.array.weekday_names)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 62.dp)) {
            Text(stringResource(R.string.leaf_account), style = RafiqType.titleL, color = rc.ink,
                modifier = Modifier.align(Alignment.CenterStart))
            Stamp(complete, Modifier.align(Alignment.CenterEnd))
        }
        rows.forEach { r ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    Modifier.size(18.dp).clip(CircleShape)
                        .background(if (r.done) rc.emeraldFill else rc.card)
                        .border(2.dp, if (r.done) rc.emeraldFill else rc.divider, CircleShape),
                )
                Text(r.label, style = RafiqType.bodyS, color = rc.ink, modifier = Modifier.weight(1f))
                Text(r.value.localizedDigits(ar), style = RafiqType.metaS, color = if (r.done) rc.emerald else rc.inkLight)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            week.forEachIndexed { i, d ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    val today = i == week.lastIndex
                    Box(
                        Modifier.fillMaxWidth().height(46.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomEnd = 7.dp, bottomStart = 10.dp))
                            .background(rc.chipBg)
                            .border(if (today) 2.dp else 1.dp, if (today) rc.gold else rc.cardBorder,
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomEnd = 7.dp, bottomStart = 10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        // خمسُ نقاطٍ لخمسِ صلوات — المملوءةُ ما صُلّي
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) { k -> Dot(k < d.prayed) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(2) { k -> Dot(k + 3 < d.prayed) }
                            }
                        }
                    }
                    Text(
                        names[d.date.dayOfWeek.ordinal.let { (it + 1) % 7 }],
                        style = RafiqType.metaS, color = rc.inkLight, maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        Text(stringResource(R.string.leaf_account_end), style = RafiqType.bodyS, color = rc.inkMed,
            modifier = Modifier.padding(top = 12.dp))
        Text(
            stringResource(R.string.leaf_flip_front),
            style = RafiqType.label, fontWeight = FontWeight.Bold, color = rc.gold,
            modifier = Modifier.padding(top = 6.dp).heightIn(min = 48.dp).clickable(onClick = onFlip)
                .wrapContentHeight(Alignment.CenterVertically),
        )
    }
}

@Composable
private fun Dot(on: Boolean) {
    val rc = LocalRafiqColors.current
    Box(Modifier.size(5.dp).clip(CircleShape).background(if (on) rc.emeraldFill else rc.divider))
}
