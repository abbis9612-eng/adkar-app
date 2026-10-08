package app.rafiqaldhikr.ui.screens.arbaeen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.AtharItem
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.EmptyState
import app.rafiqaldhikr.ui.components.FirstHint
import app.rafiqaldhikr.ui.components.RIcon
import app.rafiqaldhikr.ui.components.RafiqIcon
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel

/*
 * شاشةُ الأربعين النوويّة
 * ═════════════════════
 *
 * ═══ ولماذا الرقمُ قبل المتن ═══
 *
 * هذا **كتابٌ مرتَّب** لا قائمةَ أحاديث: من حفظ «الحديث الأربعين» عرفه
 * برقمه. فالرقمُ في دائرةٍ قبل المتن، وتُظلَّم دائرتُه إذا قُرئ — فيُرى
 * التقدّمُ بنظرةٍ واحدةٍ على الصفحة لا بشريطٍ في أعلاها وحدَه.
 *
 * ═══ والعلامةُ يضعها القارئُ بيده ═══
 *
 * ولا تُوضَع تلقائيّاً بفتح الحديث: من فتحه ليقرأ موضعاً فيه لم يقرأه،
 * وعلامةٌ تُوضَع عنه تكذب عليه. فزرٌّ صريح.
 */

@Composable
fun ArbaeenScreen(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val vm: ArbaeenViewModel = koinViewModel()
    val book by vm.book.collectAsStateWithLifecycle()
    val read by vm.read.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding(),
    ) {
        RafiqTopBar(
            title = stringResource(R.string.arbaeen_title),
            subtitle = stringResource(R.string.arbaeen_subtitle),
            onBack = { nav.popBackStack() },
        )

        val b = book
        if (b == null) {
            EmptyState(message = stringResource(R.string.arbaeen_absent))
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 24.dp))
                        .background(rc.emeraldPastel)
                        .padding(14.dp),
                ) {
                    Column {
                        Text(
                            stringResource(
                                R.string.arbaeen_progress,
                                read.size.localized(ar),
                                b.items.size.localized(ar),
                            ),
                            style = RafiqType.titleM,
                            color = rc.emerald,
                        )
                        Spacer(Modifier.height(7.dp))
                        Text(
                            stringResource(
                                R.string.arbaeen_credit,
                                b.publisher,
                                b.fetchedOn,
                            ),
                            style = RafiqType.caption,
                            color = rc.emerald,
                        )
                    }
                }
            }

            item { FirstHint("arbaeen", R.string.arbaeen_hint) }

            itemsIndexed(b.items, key = { _, it -> it.sourceId }) { i, item ->
                HadithRow(
                    n = i + 1,
                    item = item,
                    done = (i + 1) in read,
                    onToggle = { vm.toggle(i + 1) },
                )
            }
        }
    }
}

/** حديثٌ برقمه: متنُه كاملاً، ويُفتح على تخريجه وشرحه. */
@Composable
private fun HadithRow(n: Int, item: AtharItem, done: Boolean, onToggle: () -> Unit) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    var open by remember(item.sourceId) { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 24.dp))
            .background(rc.card)
            .clickable { open = if (open == 0) 1 else 0 }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (done) rc.emeraldFill else rc.chipBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    n.localized(ar),
                    style = RafiqType.meta,
                    color = if (done) rc.onEmeraldFill else rc.inkMed,
                )
            }
            Spacer(Modifier.size(11.dp))
            //  المتنُ كاملاً — لا اقتطاعَ لنصٍّ دينيّ
            Text(
                item.text,
                style = RafiqType.ayah,
                color = rc.ink,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.grade, style = RafiqType.meta, color = rc.emerald)
            Spacer(Modifier.weight(1f))
            Row(
                Modifier
                    .clip(RoundedCornerShape(9.dp, 9.dp, 9.dp, 16.dp))
                    .background(if (done) rc.emeraldPastel else rc.chipBg)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RafiqIcon(RIcon.Check, 14.dp, if (done) rc.emerald else rc.inkLight)
                Text(
                    stringResource(if (done) R.string.arbaeen_read else R.string.arbaeen_mark),
                    style = RafiqType.caption,
                    color = if (done) rc.emerald else rc.inkMed,
                )
            }
        }

        AnimatedVisibility(open == 1) {
            Column {
                Spacer(Modifier.height(10.dp))
                Panel(stringResource(R.string.athar_takhrij), item.source)
                if (item.explanation.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Panel(stringResource(R.string.athar_sharh), item.explanation)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.athar_ref_id, item.sourceId),
                    style = RafiqType.metaS,
                    color = rc.inkLight,
                )
            }
        }
    }
}

@Composable
private fun Panel(label: String, body: String) {
    val rc = LocalRafiqColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp, 9.dp, 9.dp, 16.dp))
            .background(rc.chipBg)
            .padding(11.dp),
    ) {
        Text(label, style = RafiqType.meta, color = rc.gold)
        Spacer(Modifier.height(5.dp))
        Text(body, style = RafiqType.bodyS, color = rc.inkMed)
    }
}
