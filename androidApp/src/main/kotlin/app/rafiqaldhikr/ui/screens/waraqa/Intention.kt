package app.rafiqaldhikr.ui.screens.waraqa

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.theme.tapSpec

/* ═══════════════════════════════════════════════════════════════════
   عزمُ اليوم — سؤالٌ واحدٌ بعد الفجر

   «ما تنوي اليومَ مع فرائضك؟» — الضحى والوتر والوِرد، والكهفُ يومَ
   الجمعة. ثمّ يصير سطراً على الورقة: «نويتُ اليوم: الضحى · الوتر»،
   وكلُّ نيّةٍ فيه تُعلَّم حين تُفعل مهمّتُها في المحطّات — فالعزمُ
   والفعلُ في سجلٍّ واحد.

   ويُعدَّل بالضغط على السطر: من عزم ثمّ بدا له فلا يُحبَس على اختياره.
═══════════════════════════════════════════════════════════════════ */

private val IntentNames = mapOf(
    "duha" to R.string.intent_duha,
    "witr" to R.string.intent_witr,
    "kahf" to R.string.intent_kahf,
    "wird" to R.string.intent_wird,
)

/**
 * @param intentions عزمُ اليوم، أو `null` ما لم يُعزَم.
 * @param ask        أيُعرض السؤالُ إن لم يُعزَم — بعد دخول الفجر.
 */
@Composable
fun IntentionBlock(
    intentions: Set<String>?,
    ask: Boolean,
    friday: Boolean,
    completed: Set<String>,
    onCommit: (Set<String>) -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    val chosen = intentions.orEmpty() - LeafViewModel.NONE
    when {
        editing || (intentions == null && ask) -> Ask(
            initial = chosen,
            friday = friday,
            onCommit = { onCommit(it); editing = false },
        )
        chosen.isNotEmpty() -> Line(chosen, completed, onEdit = { editing = true })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Ask(initial: Set<String>, friday: Boolean, onCommit: (Set<String>) -> Unit) {
    val rc = LocalRafiqColors.current
    val haptic = LocalHapticFeedback.current
    var picked by rememberSaveable { mutableStateOf(initial.toList()) }
    val items = LeafViewModel.INTENT_TASK.keys.filter { it != "kahf" || friday }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 12.dp)
            .clip(RoundedCornerShape(14.dp)).background(rc.tintGold)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(stringResource(R.string.intent_q), style = RafiqType.label, fontWeight = FontWeight.Bold, color = rc.ink)
        FlowRow(
            Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { key ->
                val on = key in picked
                val bg by animateColorAsState(if (on) rc.emeraldFill else rc.card, tapSpec(), label = "intent")
                Text(
                    stringResource(IntentNames.getValue(key)),
                    style = RafiqType.label,
                    fontWeight = FontWeight.Bold,
                    color = if (on) rc.onEmeraldFill else rc.inkMed,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(bg)
                        .border(1.5.dp, if (on) rc.emeraldFill else rc.divider, CircleShape)
                        .clickable(role = Role.Checkbox) {
                            picked = if (on) picked - key else picked + key
                        }
                        .padding(horizontal = 14.dp)
                        .wrapContentHeight(Alignment.CenterVertically),
                )
            }
        }
        Box(
            Modifier.padding(top = 12.dp).fillMaxWidth().height(48.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 20.dp))
                .background(rc.ink)
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCommit(picked.toSet())
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(if (picked.isEmpty()) R.string.intent_none else R.string.intent_go),
                style = RafiqType.label, fontWeight = FontWeight.Bold, color = rc.bg,
            )
        }
    }
}

/** «نويتُ اليوم: الضحى ✓ · الوتر» — وما فُعل منها معلَّم. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Line(chosen: Set<String>, completed: Set<String>, onEdit: () -> Unit) {
    val rc = LocalRafiqColors.current
    val order = LeafViewModel.INTENT_TASK.keys.filter { it in chosen }
    FlowRow(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(stringResource(R.string.intent_line), style = RafiqType.metaS, fontWeight = FontWeight.Bold, color = rc.gold)
        order.forEachIndexed { i, key ->
            val done = LeafViewModel.INTENT_TASK[key] in completed
            if (i > 0) Text("·", style = RafiqType.metaS, color = rc.inkLight)
            Text(
                (if (done) "✓ " else "") + stringResource(IntentNames.getValue(key)),
                style = RafiqType.bodyS,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                color = if (done) rc.emerald else rc.ink,
            )
        }
    }
}
