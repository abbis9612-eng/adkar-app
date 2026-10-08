package app.rafiqaldhikr.ui.screens.isnad

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.IsnadEntry
import app.rafiq.domain.model.isnadPercent
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel

/*
 * شاشةُ جَرْد الإسناد
 * ═════════════════
 *
 * ═══ ولماذا الرقمُ أوّلاً ═══
 *
 * الدعوى كلُّها في رقمٍ واحد: **مئةٌ بالمئة**. فيُعرَض كبيراً في أعلى
 * الشاشة، ثمّ يُفصَّل: كم نصّاً، وبأيّ الدرجات، ثمّ كلُّ نصٍّ بتخريجه.
 *
 * ولو بدأت بالقائمة لصارت شاشةَ «مصادر» تُتَصفَّح ولا تُقنع. والترتيبُ هنا
 * ترتيبُ حجّةٍ لا ترتيبُ محتوًى: الدعوى، ثمّ تفصيلُها، ثمّ دليلُها.
 *
 * ═══ والنصُّ يُطوى ولا يُقتطع ═══
 *
 * كلُّ سطرٍ يُظهر أوّلَ النصّ، ويُلمَس فيُفتح كاملاً بمصدره ودرجته. ولا
 * يُقتطع نصٌّ دينيٌّ بثلاث نقاطٍ في عرضٍ نهائيّ — فقاعدةُ المشروع أن لا
 * يُختصر. والطيُّ عرضٌ، والاقتطاعُ تغييرٌ.
 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IsnadScreen(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val vm: IsnadViewModel = koinViewModel()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val summary by vm.summary.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding(),
    ) {
        RafiqTopBar(
            title = stringResource(R.string.isnad_title),
            subtitle = stringResource(R.string.isnad_subtitle),
            onBack = { nav.popBackStack() },
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                val s = summary
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 30.dp))
                        .background(rc.emeraldPastel)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (s == null) "—"
                            else stringResource(
                                R.string.isnad_percent,
                                isnadPercent(s).localized(ar),
                            ),
                            style = RafiqType.display,
                            color = rc.emerald,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (s == null) stringResource(R.string.isnad_counting)
                            else stringResource(R.string.isnad_total, s.total.localized(ar)),
                            style = RafiqType.bodyS,
                            color = rc.emerald,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            summary?.let { s ->
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        for ((grade, n) in s.byGrade) {
                            Row(
                                Modifier
                                    .clip(RoundedCornerShape(9.dp, 9.dp, 9.dp, 16.dp))
                                    .background(rc.chipBg)
                                    .padding(horizontal = 11.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                //  الدرجةُ نصٌّ من البيانات لا من الموارد: هي
                                //  حكمُ أهل العلم بلفظه، ولا تُترجَم هنا.
                                Text(grade, style = RafiqType.caption, color = rc.ink)
                                Text(n.localized(ar), style = RafiqType.meta, color = rc.gold)
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.isnad_note),
                        style = RafiqType.caption,
                        color = rc.inkMed,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }

            items(entries, key = { it.group + it.text.take(24) }) { entry ->
                EntryRow(entry)
            }
        }
    }
}

/** سطرُ نصٍّ — يُطوى ويُفتح، ولا يُقتطع. */
@Composable
private fun EntryRow(entry: IsnadEntry) {
    val rc = LocalRafiqColors.current
    var open by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp, 13.dp, 13.dp, 22.dp))
            .background(rc.card)
            .clickable { open = !open }
            .padding(horizontal = 13.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.text,
                style = RafiqType.bodyS,
                color = rc.ink,
                maxLines = if (open) Int.MAX_VALUE else 2,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.height(0.dp))
            if (entry.grade.isNotBlank()) {
                Text(
                    entry.grade,
                    style = RafiqType.meta,
                    color = rc.emerald,
                    modifier = Modifier.padding(start = 9.dp),
                )
            }
        }
        AnimatedVisibility(open) {
            Column {
                Spacer(Modifier.height(9.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp, 9.dp, 9.dp, 16.dp))
                        .background(rc.chipBg)
                        .padding(11.dp),
                ) {
                    Text(entry.source, style = RafiqType.caption, color = rc.inkMed)
                }
            }
        }
    }
}
