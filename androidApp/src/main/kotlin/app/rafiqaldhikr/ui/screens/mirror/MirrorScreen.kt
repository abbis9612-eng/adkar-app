package app.rafiqaldhikr.ui.screens.mirror

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.Door
import app.rafiq.domain.model.MirrorDay
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.localizedDigits
import org.koin.androidx.compose.koinViewModel

/*
 *  التوقيع: سنةٌ كاملةٌ في شبكةٍ واحدة — يومٌ لكلّ مربّع، لا رقمٌ يُقال
 *
 * «سنتك» — المرآة
 * ═══════════════
 *
 * الإحصائيّاتُ تقول «٤٧ يوماً · ٣١٢ صفحة». وهي صادقةٌ لا تُحرّك أحداً:
 * من رآها عرف كم فعل، ولم يَرَ **كيف كان عامُه**.
 *
 * والمرآةُ تُري الشكلَ لا العدد: الانقطاعُ خطٌّ باهت، والمواظبةُ رقعةٌ
 * داكنة، ورمضانُ يُرى من بعيد. ثمّ ثلاثةُ أسطرٍ بالعربية لا أرقامٍ
 * جافّة.
 *
 * ═══ والشبكةُ تُقرأ عموداً لا سطراً ═══
 *
 * كلُّ عمودٍ أسبوعٌ من سبعة أيّام، والأعمدةُ تمشي بالزمن. وهو ترتيبٌ
 * يجعل الأسبوعَ وحدةً تُرى، فيُعرف «كنتُ أواظب في الأسابيع الأولى».
 */

/** ضلعُ المربّع، والفجوةُ بينه وبين جاره. */
private val CELL = 11.dp
private val GAP = 3.dp

@Composable
fun MirrorScreen(
    navController: NavHostController,
    vm: MirrorViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val ui by vm.ui.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(rc.bg).statusBarsPadding()) {
        RafiqTopBar(
            title = stringResource(R.string.mirror_title),
            onBack = { navController.popBackStack() },
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.mirror_sub).localizedDigits(ar),
                style = RafiqType.bodyS, color = rc.inkMed,
            )
            Spacer(Modifier.height(18.dp))

            if (!ui.loading && ui.summary?.activeDays == 0) {
                //  ولا تُعرض شبكةٌ فارغةٌ بلا كلمة: الفراغُ يُفسَّر لا يُترك.
                Text(
                    stringResource(R.string.mirror_empty),
                    style = RafiqType.body, color = rc.inkMed,
                )
                Spacer(Modifier.height(40.dp))
                return@Column
            }

            Grid(ui.days, rc)
            Spacer(Modifier.height(10.dp))
            Legend(rc)

            ui.summary?.let { s ->
                Spacer(Modifier.height(26.dp))
                Line(stringResource(R.string.mirror_active, s.activeDays.toString())
                    .localizedDigits(ar), rc)
                Line(stringResource(R.string.mirror_longest, s.longestStreak.toString())
                    .localizedDigits(ar), rc)
                if (s.quranPages > 0) {
                    Line(stringResource(R.string.mirror_quran, s.quranPages.toString())
                        .localizedDigits(ar), rc)
                }
                if (s.tasbeeh > 0) {
                    Line(stringResource(R.string.mirror_tasbeeh, s.tasbeeh.toString())
                        .localizedDigits(ar), rc)
                }
                s.mostKept?.let {
                    Line(stringResource(R.string.mirror_most, stringResource(it.label())), rc)
                }
            }
            Spacer(Modifier.height(44.dp))
        }
    }
}

/** شبكةُ العام — عمودٌ لكلّ أسبوع، سبعةُ مربّعاتٍ في العمود. */
@Composable
private fun Grid(days: List<MirrorDay>, rc: RafiqPalette) {
    //  الأقدمُ أوّلاً، والاتّجاهُ يتكفّل به النظام: في العربية يبدأ من
    //  اليمين فيمشي الزمنُ مع القراءة.
    val weeks = days.chunked(7)
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(GAP),
    ) {
        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                week.forEach { d ->
                    Box(
                        Modifier
                            .size(CELL)
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(shade(d.depth, rc)),
                    )
                }
            }
        }
    }
}

/**
 * لونُ العمق — خمسُ درجاتٍ من الزمرّد.
 *
 * والصفرُ ليس شفّافاً بل `divider`: المربّعُ الخالي **يُرى** فتُقرأ
 * الشبكةُ شبكةً. ولو اختفى لتقطّعت الصفوفُ وبدت أعمدةً عشوائيّة.
 */
private fun shade(depth: Int, rc: RafiqPalette): Color = when (depth.coerceIn(0, 5)) {
    0 -> rc.divider
    1 -> rc.emeraldFill.copy(alpha = 0.22f)
    2 -> rc.emeraldFill.copy(alpha = 0.42f)
    3 -> rc.emeraldFill.copy(alpha = 0.62f)
    4 -> rc.emeraldFill.copy(alpha = 0.82f)
    else -> rc.emeraldFill
}

@Composable
private fun Legend(rc: RafiqPalette) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.mirror_less), style = RafiqType.metaS, color = rc.inkLight)
        Spacer(Modifier.width(6.dp))
        (0..5).forEach {
            Box(
                Modifier.size(CELL).clip(RoundedCornerShape(2.5.dp)).background(shade(it, rc)),
            )
            Spacer(Modifier.width(GAP))
        }
        Spacer(Modifier.width(3.dp))
        Text(stringResource(R.string.mirror_more), style = RafiqType.metaS, color = rc.inkLight)
    }
}

@Composable
private fun Line(text: String, rc: RafiqPalette) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(4.dp).clip(RoundedCornerShape(2.dp)).background(rc.gold))
        Spacer(Modifier.width(10.dp))
        Text(text, style = RafiqType.body, color = rc.ink)
    }
}

private fun Door.label(): Int = when (this) {
    Door.MORNING -> R.string.door_morning
    Door.EVENING -> R.string.door_evening
    Door.QURAN -> R.string.door_quran
    Door.TASBEEH -> R.string.door_tasbeeh
    Door.PRAYERS -> R.string.door_prayers
}
