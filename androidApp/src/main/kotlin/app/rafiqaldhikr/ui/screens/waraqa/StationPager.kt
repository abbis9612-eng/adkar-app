package app.rafiqaldhikr.ui.screens.waraqa

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationStatus
import app.rafiqaldhikr.ui.screens.daycompanion.DayCompanionViewModel.StationUi
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.LocalReducedMotion
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.tapSpec
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.formatClock
import app.rafiqaldhikr.ui.utils.localized
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ═══════════════════════════════════════════════════════════════════
   بطاقاتُ المحطّات — تُقلَّب يميناً ويساراً

   الورقةُ فوقها تقول «كيف يومي؟»، وهذه تقول «ماذا أفعل الآن؟». تُفتح
   على الحاضرة، وفي وسطها **ما يُفعل في المحطّة** بعلاماته — كان في
   النموذج فراغاً بين الشريط والحديث.

   و«فعلتُها» لها أثرٌ لأوّل مرّة: كانت `completeStation()` مكتوبةً في
   الكود ولا زرَّ يستدعيها. والآن تُعلّم المحطّةَ ومهامَّها، ثمّ تنتقل
   البطاقةُ وحدَها إلى التي تليها.

   وما فات لا يُلام عليه: يُفتح دائماً، ولا لونَ أحمر.
═══════════════════════════════════════════════════════════════════ */

private val CardShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomEnd = 22.dp, bottomStart = 32.dp)
private val GoShape   = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 22.dp)

@Composable
fun StationPager(
    stations: List<StationUi>,
    nowId: String?,
    timed: Boolean,
    leaf: LeafViewModel.UiState,
    onTask: (StationTask, Boolean) -> Unit,
    onDone: (StationUi, List<StationTask>) -> Unit,
    onUndo: (StationUi, List<StationTask>) -> Unit,
    onOpen: (String) -> Unit,
) {
    if (stations.isEmpty()) return
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val reduced = LocalReducedMotion.current
    val scope = rememberCoroutineScope()
    val start = stations.indexOfFirst { it.id == nowId }.coerceAtLeast(0)
    val pager = rememberPagerState(initialPage = start) { stations.size }

    // الحاضرةُ تتبدّل مع الوقت — فإن تبدّلت والبطاقةُ عليها انتقلت معها
    LaunchedEffect(start) { if (pager.currentPage != start) pager.scrollToPage(start) }

    val tasksOf = remember(stations) { stations.associate { it.id to StationTasks.of(it.id, it.route) } }
    fun done(st: StationUi) = st.status == StationStatus.DONE ||
        StationTasks.isStationDone(st.id, tasksOf[st.id].orEmpty(), leaf.prayed, leaf.completed, leaf.morning, leaf.evening)

    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(stringResource(R.string.stations_title), style = RafiqType.label, fontWeight = FontWeight.Bold,
            color = rc.ink, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.stations_hint), style = RafiqType.metaS, color = rc.inkLight)
    }

    HorizontalPager(
        state = pager,
        contentPadding = PaddingValues(horizontal = 28.dp),
        pageSpacing = 10.dp,
        verticalAlignment = Alignment.Top,
    ) { i ->
        val st = stations[i]
        val tasks = tasksOf[st.id].orEmpty()
        StationCard(
            st = st, index = i, total = stations.size, isNow = st.id == nowId, timed = timed,
            done = done(st), tasks = tasks, leaf = leaf,
            onTask = onTask, onOpen = onOpen,
            onDone = {
                onDone(st, tasks)
                if (i < stations.lastIndex) scope.launch {
                    delay(420)
                    if (reduced) pager.scrollToPage(i + 1) else pager.animateScrollToPage(i + 1)
                }
            },
            onUndo = { onUndo(st, tasks) },
        )
    }

    // شريطُ المحطّات — الأخضرُ تمّ، والمحاطُ بالذهبيّ فات، والذهبيُّ الآن
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 30.dp).padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        stations.forEachIndexed { i, st ->
            val missed = st.status == StationStatus.PASSED && !done(st)
            Box(
                Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 30.dp)
                    .fillMaxWidth()
                    .height(if (i == pager.currentPage) 9.dp else 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            done(st) -> rc.emeraldFill
                            st.id == nowId -> rc.gold
                            missed -> rc.bg
                            else -> rc.divider
                        },
                    )
                    .then(if (missed) Modifier.border(1.3.dp, rc.kaabaGold, RoundedCornerShape(3.dp)) else Modifier)
                    .clickable {
                        scope.launch { if (reduced) pager.scrollToPage(i) else pager.animateScrollToPage(i) }
                    },
            )
        }
    }
    // `getOrNull`: يومُ الجمعة يزيد محطّةً، فإن نقص العددُ والبطاقةُ على الأخيرة لم يُفهرَس خارجه
    val shown = stations.getOrNull(pager.currentPage) ?: stations.last()
    Text(
        stringResource(R.string.st_of, (stations.indexOf(shown) + 1).localized(ar), stations.size.localized(ar)) +
            " · " + stringResource(shown.title),
        style = RafiqType.metaS, color = rc.inkLight,
        modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}

@Composable
private fun StationCard(
    st: StationUi,
    index: Int,
    total: Int,
    isNow: Boolean,
    timed: Boolean,
    done: Boolean,
    tasks: List<StationTask>,
    leaf: LeafViewModel.UiState,
    onTask: (StationTask, Boolean) -> Unit,
    onOpen: (String) -> Unit,
    onDone: () -> Unit,
    onUndo: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val ctx = LocalContext.current
    val missed = st.status == StationStatus.PASSED && !done
    val border by animateColorAsState(
        when { done -> rc.emerald; isNow -> rc.emerald; else -> rc.cardBorder }, tapSpec(), label = "cardBorder",
    )

    Column(
        Modifier
            .fillMaxWidth()
            .rafiqCard(active = isNow, shape = CardShape)
            .border(if (isNow) 1.5.dp else 1.dp, border, CardShape),
    ) {
        // شريطُ الرأس: ذهبيٌّ للحاضرة، وأخضرُ لما تمّ
        Box(Modifier.fillMaxWidth().height(4.dp).background(
            when { done -> rc.emeraldFill; isNow -> rc.gold; else -> rc.divider }))

        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.st_of, (index + 1).localized(ar), total.localized(ar)),
                    style = RafiqType.metaS, color = rc.inkLight, modifier = Modifier.weight(1f))
                val (tag, bg, fg) = when {
                    done   -> Triple(R.string.st_tag_done, rc.emeraldPastel, rc.emerald)
                    isNow  -> Triple(R.string.st_tag_now, rc.emeraldFill, rc.onEmeraldFill)
                    missed -> Triple(R.string.prayer_time_passed, rc.tintGold, rc.gold)
                    else   -> Triple(R.string.st_tag_up, rc.chipBg, rc.inkMed)
                }
                Text(stringResource(tag), style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = fg,
                    modifier = Modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 2.dp))
            }

            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (timed && !st.spansDay) {
                    Text(formatClock(st.startMillis, ar), style = RafiqType.display, color = rc.emerald)
                }
                Text(stringResource(st.timeLabel), style = RafiqType.metaS, color = rc.inkMed,
                    modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(stringResource(st.title), style = RafiqType.titleL, color = rc.ink, modifier = Modifier.padding(top = 4.dp))

            if (isNow && timed && !st.spansDay) TimeBar(st)

            // المهامّ — الفراغُ الذي كان في وسط البطاقة
            Box(Modifier.fillMaxWidth().padding(top = 12.dp).height(1.dp).background(rc.divider))
            tasks.forEach { t ->
                val on = StationTasks.isDone(t, leaf.prayed, leaf.completed, leaf.morning, leaf.evening)
                TaskRow(t, on, onTask, onOpen, count = t.adhkar?.let { AdhkarCount.of(ctx, it) })
            }

            if (missed) {
                Text(stringResource(R.string.st_missed_note), style = RafiqType.bodyS, color = rc.inkMed,
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)).background(rc.tintGold)
                        .padding(horizontal = 10.dp, vertical = 7.dp))
            }

            if (st.virtue.isNotBlank()) {
                Text(st.virtue, style = RafiqType.dhikr.copy(fontSize = RafiqType.bodyS.fontSize * 1.05f),
                    color = rc.ink, modifier = Modifier.padding(top = 12.dp))
                if (st.source.isNotBlank()) Text(st.source, style = RafiqType.metaS, color = rc.gold)
            }

            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val go = st.route ?: tasks.firstNotNullOfOrNull { it.opens }
                if (go != null) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(GoShape).background(rc.ink)
                            .clickable(role = Role.Button) { onOpen(go) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.action_start), style = RafiqType.label,
                            fontWeight = FontWeight.Bold, color = rc.bg)
                    }
                }
                val haptic = LocalHapticFeedback.current
                Box(
                    Modifier
                        .then(if (go == null) Modifier.weight(1f) else Modifier)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (done) rc.emeraldPastel else rc.card)
                        .border(1.6.dp, if (done) rc.emeraldPastel else rc.emerald, RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button) {
                            if (done) onUndo() else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDone()
                            }
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(if (done) R.string.st_done else R.string.st_did), style = RafiqType.label,
                        fontWeight = FontWeight.Bold, color = rc.emerald)
                }
            }
        }
    }
}

/** شريطُ الوقت الباقي — يتحرّك كلَّ دقيقة. */
@Composable
private fun TimeBar(st: StationUi) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(st.id) { while (true) { now = System.currentTimeMillis(); delay(30_000) } }
    val span = (st.endMillis - st.startMillis).coerceAtLeast(1L)
    val pct = ((now - st.startMillis).toFloat() / span).coerceIn(0f, 1f)
    Box(Modifier.padding(top = 12.dp).fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(rc.divider)) {
        Box(Modifier.fillMaxWidth(pct).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(rc.gold))
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(formatClock(st.startMillis, ar), style = RafiqType.metaS, color = rc.inkLight)
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.leaf_left, durationLabel(st.endMillis - now, ar)),
            style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = rc.gold)
        Spacer(Modifier.weight(1f))
        Text(formatClock(st.endMillis, ar), style = RafiqType.metaS, color = rc.inkLight)
    }
}

@Composable
private fun TaskRow(
    t: StationTask,
    on: Boolean,
    onTask: (StationTask, Boolean) -> Unit,
    onOpen: (String) -> Unit,
    count: Int?,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val haptic = LocalHapticFeedback.current
    val fill by animateColorAsState(if (on) rc.emeraldFill else rc.card, tapSpec(), label = "tick")
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Checkbox) {
                if (!on) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onTask(t, !on)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(fill)
                .border(2.dp, if (on) rc.emeraldFill else rc.divider, CircleShape),
            contentAlignment = Alignment.Center,
        ) { if (on) RafiqIcon(RIcon.Check, 14.dp, rc.onEmeraldFill) }
        Text(stringResource(t.label), style = RafiqType.body,
            fontWeight = if (on) FontWeight.Normal else FontWeight.Bold,
            color = if (on) rc.inkMed else rc.ink, modifier = Modifier.weight(1f))
        val opens = t.opens
        when {
            opens != null -> Text(
                (count?.let { stringResource(R.string.st_count, it.localized(ar)) }
                    ?: stringResource(R.string.action_start)) + " ‹",
                style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = rc.gold,
                modifier = Modifier.clip(CircleShape).background(rc.tintGold)
                    .clickable(role = Role.Button) { onOpen(opens) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            t.kind == StationTask.Kind.PRAYER ->
                Text(stringResource(R.string.st_prayer_hint), style = RafiqType.metaS, color = rc.inkLight)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
}

/** «١س ٥٨د» أو «٢٠د» — مدّةٌ بلا ثوانٍ. */
@Composable
fun durationLabel(ms: Long, ar: Boolean): String {
    val m = (ms.coerceAtLeast(0L) / 60_000L).toInt()
    val h = m / 60
    return (if (h > 0) stringResource(R.string.dur_hm, h, m % 60) else stringResource(R.string.dur_m, m))
        .localizedDigits(ar)
}
