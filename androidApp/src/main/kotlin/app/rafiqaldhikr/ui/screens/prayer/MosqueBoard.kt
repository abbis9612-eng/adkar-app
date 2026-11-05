package app.rafiqaldhikr.ui.screens.prayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.rafiq.domain.model.PrayerEntry
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.progressSpec
import app.rafiqaldhikr.ui.theme.stillableFloat
import app.rafiqaldhikr.ui.theme.tapSpec
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.datetime.LocalDate

/* ═══════════════════════════════════════════════════════════════════
   لوحةُ المسجد — اللوحةُ المعلّقة في كلّ مسجد

   إطارٌ ولوحٌ داكن، وأرقامٌ على ألواحٍ تتقلّب حين تتبدّل. ولونُ اللوح
   تدرّجُ البطل نفسُه (`heroStart`…`heroEnd`) وحبرُه `onHero`: كلاهما ثابتٌ
   في الوضعين، فاللوحةُ داكنةٌ نهاراً وليلاً كما هي في المسجد، ولا لونَ
   مخترَع.

   والنافذةُ هي الفعل: ضغطةٌ «صلّيتُ»، وثانيةٌ «في المسجد» (`in_masjid` —
   عمودٌ قائمٌ منذ الترحيل الأوّل لم تكن له واجهة)، وثالثةٌ تمحو.

   وعند الأذان تتقلّب اللوحةُ كلُّها إلى «أذان المغرب»، وتنفتح تحتها
   إجابةُ المؤذّن جملةً جملة.
═══════════════════════════════════════════════════════════════════ */

private val FrameShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 28.dp)
private val BoardShape = RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp, bottomEnd = 11.dp, bottomStart = 20.dp)

/** نافذةٌ على اللوحة — صلاةٌ أو الشروق. */
data class BoardWindow(
    val key: String,
    val name: String,
    val timeMs: Long,
    /** الشروقُ يُعرض ولا يُسجَّل. */
    val loggable: Boolean,
)

/** حالُ الصلاة من سجلّها: لا شيء · صلّيتُ · في المسجد. */
enum class Mark { NONE, PRAYED, MASJID }

fun markOf(entry: PrayerEntry?): Mark = when {
    entry?.inMasjid == true -> Mark.MASJID
    entry?.prayed == true -> Mark.PRAYED
    else -> Mark.NONE
}

@Composable
fun MosqueBoard(
    windows: List<BoardWindow>,
    logs: List<PrayerEntry>,
    hijri: String,
    subtitle: String,
    nextKey: String?,
    nextName: String,
    leftSec: Long,
    adhanKey: String?,
    adhanName: String,
    onWindow: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    val ink = rc.onHero
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(FrameShape)
            .background(Brush.linearGradient(listOf(rc.lightDusk, rc.lightDusk.copy(alpha = 0.72f).compositeOver(rc.heroStart))))
            .padding(9.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(BoardShape)
                .background(Brush.verticalGradient(listOf(rc.heroEnd, rc.heroStart)))
                .padding(horizontal = 10.dp, vertical = 12.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.Bottom) {
                Text(hijri, style = RafiqType.metaL, fontWeight = FontWeight.Bold, color = ink, modifier = Modifier.weight(1f))
                Text(subtitle, style = RafiqType.metaS, color = ink.copy(alpha = 0.72f))
            }
            Box(Modifier.padding(top = 9.dp).fillMaxWidth().height(1.dp).background(ink.copy(alpha = 0.14f)))

            // الرأس: عدٌّ تنازليٌّ — أو «أذان المغرب» عند الأذان
            Column(
                Modifier.fillMaxWidth().heightIn(min = 112.dp).padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (adhanKey != null) {
                    Text(stringResource(R.string.board_adhan_now), style = RafiqType.metaS, color = ink)
                    Spacer(Modifier.height(6.dp))
                    WordFlaps(stringResource(R.string.board_adhan, adhanName), pulse = true)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.board_tap_hint), style = RafiqType.metaS, color = ink.copy(alpha = 0.8f))
                } else {
                    Text(stringResource(R.string.board_next_in, nextName), style = RafiqType.metaS, color = ink.copy(alpha = 0.75f))
                    Spacer(Modifier.height(6.dp))
                    DigitFlaps(countdown(leftSec))
                }
            }

            // ستُّ نوافذ: ثلاثٌ في صفّ
            windows.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    row.forEach { w ->
                        Window(
                            w = w,
                            mark = markOf(logs.find { it.prayerName == w.key }),
                            next = adhanKey == null && w.key == nextKey,
                            live = w.key == adhanKey,
                            onTap = { onWindow(w.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Window(w: BoardWindow, mark: Mark, next: Boolean, live: Boolean, onTap: () -> Unit, modifier: Modifier) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val ink = rc.onHero
    val haptic = LocalHapticFeedback.current
    val fill by animateColorAsState(
        when (mark) {
            Mark.MASJID -> ink.copy(alpha = 0.16f)
            Mark.PRAYED -> rc.emeraldMed.copy(alpha = 0.30f)
            Mark.NONE -> ink.copy(alpha = 0.03f)
        }, tapSpec(), label = "window",
    )
    val glow = if (live && mark == Mark.NONE) stillableFloat(0.35f, 1f, 1600, restValue = 1f).value else 1f
    val label = when {
        !w.loggable -> stringResource(R.string.win_sunrise)
        mark == Mark.MASJID -> stringResource(R.string.win_masjid)
        mark == Mark.PRAYED -> stringResource(R.string.win_prayed)
        live -> stringResource(R.string.leaf_now)
        next -> stringResource(R.string.win_next)
        else -> ""
    }
    val time = clock(w.timeMs, ar)
    Column(
        modifier
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(fill)
            .border(
                if (next || live || mark == Mark.MASJID) 1.5.dp else 1.dp,
                when {
                    live -> ink.copy(alpha = glow)
                    next || mark == Mark.MASJID -> ink
                    else -> ink.copy(alpha = 0.16f)
                },
                RoundedCornerShape(10.dp),
            )
            .then(
                if (w.loggable) Modifier.clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTap()
                } else Modifier
            )
            .semantics { contentDescription = "${w.name} $time"; if (label.isNotEmpty()) stateDescription = label }
            .graphicsLayer { alpha = if (w.loggable) 1f else 0.55f }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(w.name, style = RafiqType.meta, fontWeight = FontWeight.Bold,
            color = if (next || live) ink else ink.copy(alpha = 0.72f))
        Text(time, style = RafiqType.titleM, fontWeight = FontWeight.Black, color = ink, maxLines = 1)
        Text(label, style = RafiqType.metaS, fontWeight = if (mark != Mark.NONE) FontWeight.Bold else FontWeight.Normal,
            color = ink.copy(alpha = if (mark != Mark.NONE || live) 1f else 0.72f), textAlign = TextAlign.Center)
    }
}

/* ── الألواحُ المتقلّبة ─────────────────────────────────────────── */

/** أرقامٌ لوحاً لوحاً — من اليسار إلى اليمين في كلّ لغة، كالساعة. */
@Composable
private fun DigitFlaps(text: String) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            text.forEachIndexed { i, ch ->
                if (ch == ':') Text(":", style = RafiqType.titleL, color = LocalRafiqColors.current.onHero,
                    modifier = Modifier.padding(horizontal = 2.dp))
                else key(i) { Flap(ch.toString(), Modifier.widthIn(min = 30.dp).height(46.dp)) }
            }
        }
    }
}

/**
 * العربيُّ يُقلَّب كلمةً كلمة لا حرفاً حرفاً: الحروفُ تتّصل، وتفريقُها
 * في ألواحٍ يكسر الكلمة — «أ ذ ا ن» ليست «أذان».
 */
@Composable
private fun WordFlaps(text: String, pulse: Boolean) {
    val glow = if (pulse) stillableFloat(0.25f, 0.9f, 1600, restValue = 0.9f).value else 0f
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        text.split(' ').forEachIndexed { i, w ->
            key(i) {
                Flap(w, Modifier.height(42.dp).border(1.dp, LocalRafiqColors.current.onHero.copy(alpha = glow), RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp), big = false)
            }
        }
    }
}

/**
 * لوحٌ واحد: نصفاه بخطٍّ بينهما، وحين يتبدّل ما عليه ينقلب من أعلاه.
 * والانقلابُ من `progressSpec` — يسكن مع «تقليل الحركة».
 */
@Composable
private fun Flap(text: String, modifier: Modifier, big: Boolean = true) {
    val rc = LocalRafiqColors.current
    val spec = progressSpec<Float>(420)
    val turn = remember { Animatable(0f) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(text) {
        if (first) { first = false; return@LaunchedEffect }
        turn.snapTo(-88f)
        turn.animateTo(0f, spec)
    }
    Box(
        modifier
            .clip(RoundedCornerShape(5.dp))
            .background(Brush.verticalGradient(0f to rc.heroMid, 0.5f to rc.heroMid, 0.5f to rc.heroStart, 1f to rc.heroStart)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (big) RafiqType.titleXL else RafiqType.titleL,
            fontWeight = FontWeight.Black,
            color = rc.onHero,
            modifier = Modifier.padding(horizontal = 4.dp).graphicsLayer { rotationX = turn.value; cameraDistance = 12f * density },
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.Black.copy(alpha = 0.45f)))
    }
}

/* ── مصابيحُ الأسبوع: كلُّ مصباحٍ صلاةٌ في المسجد ───────────────── */

@Composable
fun MasjidWeek(week: List<Pair<LocalDate, Int>>, modifier: Modifier = Modifier) {
    if (week.isEmpty()) return
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val initials = LocalContext.current.resources.getStringArray(R.array.weekday_initials)
    val total = week.sumOf { it.second }
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 24.dp))
            .background(rc.card)
            .border(1.dp, rc.cardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.masjid_week), style = RafiqType.label, fontWeight = FontWeight.Bold, color = rc.ink)
            Text(stringResource(R.string.masjid_week_sub, total.localized(ar), (week.size * 5).localized(ar)),
                style = RafiqType.metaS, color = rc.inkLight)
        }
        week.forEachIndexed { i, (d, n) ->
            val today = i == week.lastIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                (4 downTo 0).forEach { j ->
                    Box(Modifier.size(width = 12.dp, height = 5.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (j < n) rc.gold else rc.divider))
                }
                Text(
                    if (today) stringResource(R.string.today) else initials[(d.dayOfWeek.ordinal + 1) % 7],
                    style = RafiqType.micro, color = if (today) rc.ink else rc.inkLight,
                    fontWeight = if (today) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/* ── إجابةُ المؤذّن ────────────────────────────────────────────── */

/**
 * الجُمَلُ السبع — الحاضرةُ وحدَها تُضغط، فلا يُقفز إلى آخرها. وحين
 * تتمّ ينفتح حديثُ الصلاة على النبيّ ﷺ وسؤالِ الوسيلة.
 */
@Composable
fun IjabaSteps(ijaba: Ijaba, step: Int, onStep: () -> Unit, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 24.dp)
    Column(modifier.fillMaxWidth().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(stringResource(R.string.ijaba_sub), style = RafiqType.bodyS, color = rc.inkMed,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
        ijaba.pairs.forEachIndexed { i, p ->
            val done = i < step
            val cur = i == step
            val bg by animateColorAsState(if (done) rc.emeraldPastel else rc.card, progressSpec(), label = "ijaba")
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clip(shape)
                    .background(bg)
                    .border(if (cur) 1.6.dp else 1.dp, if (cur) rc.emerald else if (done) bg else rc.cardBorder, shape)
                    .then(if (cur) Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStep()
                    } else Modifier)
                    .graphicsLayer { alpha = if (done || cur) 1f else 0.55f }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(if (done) "✓" else (i + 1).localized(ar), style = RafiqType.label,
                    color = if (done || cur) rc.emerald else rc.inkLight, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.ijaba_muezzin, p.muezzin), style = RafiqType.metaS, color = rc.inkLight)
                    // لفظُ الحديث — بخطّ النسخ كما يُعرض كلُّ مرويٍّ في التطبيق
                    Text(p.reply, style = RafiqType.dhikr, color = if (done) rc.emerald else rc.ink)
                }
            }
        }
        val finished = step >= ijaba.pairs.size
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(rc.card)
                .border(if (finished) 1.6.dp else 1.dp, if (finished) rc.emerald else rc.cardBorder, shape)
                .padding(14.dp),
        ) {
            Text(stringResource(if (finished) R.string.ijaba_then else R.string.ijaba_after),
                style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = rc.gold)
            Text(ijaba.after.text, style = RafiqType.dhikr.copy(fontSize = RafiqType.body.fontSize), color = rc.ink,
                modifier = Modifier.padding(top = 6.dp))
            Text(ijaba.after.source.localizedDigits(ar), style = RafiqType.metaS, color = rc.inkLight, modifier = Modifier.padding(top = 4.dp))
        }
        // الحديثُ الذي اقتُطعت منه الجُمَل — كاملاً بمصدره
        Text(ijaba.hadith.text, style = RafiqType.bodyS, color = rc.inkMed, modifier = Modifier.padding(top = 6.dp))
        Text(ijaba.hadith.source.localizedDigits(ar), style = RafiqType.metaS, color = rc.inkLight)
    }
}

/* ── أدواتٌ صغيرة ───────────────────────────────────────────────── */

/** «٢:٠٧:٥٣» — ساعاتٌ بلا صفرٍ بادئ، ودقائقُ وثوانٍ برقمين. */
internal fun countdownRaw(sec: Long): String {
    val s = sec.coerceAtLeast(0)
    return "%d:%02d:%02d".format(java.util.Locale.US, s / 3600, s % 3600 / 60, s % 60)
}

@Composable
private fun countdown(sec: Long): String = countdownRaw(sec).localizedDigits(LocalArabicNumerals.current)

/** «٥:٠٨» بلا ص/م — على اللوحة، كما في لوحات المساجد. */
internal fun clock(ms: Long, arabic: Boolean): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
    val h = (c.get(java.util.Calendar.HOUR_OF_DAY) % 12).let { if (it == 0) 12 else it }
    return "%d:%02d".format(java.util.Locale.US, h, c.get(java.util.Calendar.MINUTE)).localizedDigits(arabic)
}

private fun Color.compositeOver(bg: Color): Color = Color(
    red = red * alpha + bg.red * (1 - alpha),
    green = green * alpha + bg.green * (1 - alpha),
    blue = blue * alpha + bg.blue * (1 - alpha),
)
