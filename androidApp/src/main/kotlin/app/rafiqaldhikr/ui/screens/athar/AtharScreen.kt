package app.rafiqaldhikr.ui.screens.athar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.AtharItem
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.EmptyState
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel

/*
 * شاشةُ الأثر
 * ══════════
 *
 * ═══ ولماذا المتنُ أوّلاً والشرحُ بلمسة ═══
 *
 * الحديثُ هو المقصود، والشرحُ معونةٌ عليه. فيُعرَض المتنُ بخطّ الرواية
 * كاملاً غيرَ مقتطع، ومعه درجتُه — ثمّ يُلمَس فيُفتح تخريجُه وشرحُه.
 *
 * ولا يُقتطع متنٌ بثلاث نقاطٍ ولا يُلخَّص شرح: شرطُ الناشر «عدم التعديل
 * أو الإضافة أو الحذف»، وهو قانونُ المشروع كذلك. **وما لا يُعرَض كاملاً
 * لا يُعرَض.**
 *
 * ═══ ونسبةُ الناشر ليست حاشيةً ═══
 *
 * تُعرَض في أعلى الشاشة لا في آخرها: من يقرأ حديثاً يحقُّ له أن يعرف من
 * أين جاء **قبل أن يقرأه**، لا بعد أن يصدّقه. وهي شرطٌ من شروط النشر
 * كذلك.
 */

/** مفتاحُ البابِ ← اسمُه في الموارد. */
private fun labelOf(key: String): Int = when (key) {
    "fadl_dhikr" -> R.string.athar_cat_fadl_dhikr
    "fadl_dua" -> R.string.athar_cat_fadl_dua
    "hady_dhikr" -> R.string.athar_cat_hady_dhikr
    "fawaid_dhikr" -> R.string.athar_cat_fawaid_dhikr
    "morning_evening" -> R.string.athar_cat_morning_evening
    "mutlaq" -> R.string.athar_cat_mutlaq
    "home" -> R.string.athar_cat_home
    "khala" -> R.string.athar_cat_khala
    "masjid" -> R.string.athar_cat_masjid
    "shidda" -> R.string.athar_cat_shidda
    "arida" -> R.string.athar_cat_arida
    "ahkam_dua" -> R.string.athar_cat_ahkam_dua
    "anwa_dua" -> R.string.athar_cat_anwa_dua
    "adab_dua" -> R.string.athar_cat_adab_dua
    "ijaba" -> R.string.athar_cat_ijaba
    "mathura" -> R.string.athar_cat_mathura
    "salah" -> R.string.athar_cat_salah
    else -> R.string.athar_title
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AtharScreen(nav: NavHostController) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val vm: AtharViewModel = koinViewModel()
    val book by vm.book.collectAsStateWithLifecycle()
    val picked by vm.category.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding(),
    ) {
        RafiqTopBar(
            title = stringResource(R.string.athar_title),
            subtitle = stringResource(R.string.athar_subtitle),
            onBack = { nav.popBackStack() },
        )

        val b = book
        if (b == null) {
            EmptyState(message = stringResource(R.string.athar_absent))
            return@Column
        }

        val shown = b.items.filter { picked == null || it.category == picked }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                //  نسبةُ الناشر أوّلاً — شرطٌ من شروطه، وحقٌّ للقارئ
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp, 13.dp, 13.dp, 22.dp))
                        .background(rc.chipBg)
                        .padding(13.dp),
                ) {
                    Text(
                        stringResource(R.string.athar_credit, b.publisher, b.fetchedOn),
                        style = RafiqType.caption,
                        color = rc.inkMed,
                    )
                }
            }

            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    for ((key, n) in vm.categories()) {
                        val on = picked == key
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(9.dp, 9.dp, 9.dp, 16.dp))
                                .background(if (on) rc.emeraldFill else rc.chipBg)
                                .clickable { vm.pick(key) }
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                stringResource(labelOf(key)),
                                style = RafiqType.caption,
                                color = if (on) rc.onEmeraldFill else rc.ink,
                            )
                            Text(
                                n.localized(ar),
                                style = RafiqType.meta,
                                color = if (on) rc.onEmeraldFill else rc.gold,
                            )
                        }
                    }
                }
            }

            items(shown, key = { it.sourceId }) { item -> AtharRow(item) }
        }
    }
}

/** حديثٌ: متنُه ودرجتُه، ويُفتح على تخريجه وشرحه. */
@Composable
private fun AtharRow(item: AtharItem) {
    val rc = LocalRafiqColors.current
    var open by remember(item.sourceId) { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp, 14.dp, 14.dp, 24.dp))
            .background(rc.card)
            .clickable { open = !open }
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            //  المتنُ بخطّ الرواية كاملاً — لا maxLines عليه
            Text(
                item.text,
                style = RafiqType.ayah,
                color = rc.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                item.grade,
                style = RafiqType.meta,
                color = rc.emerald,
                modifier = Modifier.padding(start = 9.dp),
            )
        }

        AnimatedVisibility(open) {
            Column {
                Spacer(Modifier.height(11.dp))
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
