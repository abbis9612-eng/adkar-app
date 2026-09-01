package app.rafiqaldhikr.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.screens.achievements.AwraqViewModel.Cell
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqPalette
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localized
import org.koin.androidx.compose.koinViewModel

/* ══════════════════════════════════════════════════════════════
   «أوراقي» — ما أتممتَه، لا نسبةً من هدفٍ لم تضعه.

   ثلاثةُ أسئلةٍ تجيبها الشاشة:
     ١) أيُّ وردٍ يثبت وأيُّه يفلت؟  — شبكة المحطّات × الأيّام.
        شريطٌ يقول «٨٨٪» لا يقول أيَّ ورد، فلا يُعرف ما يُصلَح.
     ٢) وكيف كان شهري؟             — كثافةُ ثلاثين يوماً في نظرة.
     ٣) وكم أتممت؟                 — عددُ ما فعلتَ لا نسبةٌ من هدف.

   وما حُذف عمداً: السلسلة واللهبُ والنقاطُ والأوسمة والنسبةُ المئوية.
   كلُّها تحوّل العبادة إلى رصيدٍ يُخاف عليه، وتُختَم الصفحة بما ينقضها:
   «أَحَبُّ الأعمالِ إلى اللهِ أَدْوَمُها وإنْ قَلَّ» — متفق عليه.
══════════════════════════════════════════════════════════════ */

/* ── السُّلَّم اللوني ────────────────────────────────────────────

   ثلاثُ درجاتٍ لا أربع. قيست أربعُ درجاتٍ على تسع لوحاتٍ (منها ما
   يختاره المستخدم من شاشة الألوان) فسقطت أدنى خطوةٍ إلى 1.17 — دون
   عتبة التمييز 1.20 — لأنّ اللون الفاتح لا يملك مدىً كافياً على ورقٍ
   شبه أبيض. وبثلاثٍ: أدنى خطوةٍ 1.26 في اللوحات كلّها.

   والحالةُ الرابعة («اليوم») ليست درجةً بل شكلاً — حلقةٌ حول الخانة —
   فلا تنافس النبرة على مدىً ضيّق أصلاً.
──────────────────────────────────────────────────────────────── */

private fun emptyTone(rc: RafiqPalette)   = lerp(rc.bg, rc.ink, 0.12f)
private fun partialTone(rc: RafiqPalette) = lerp(rc.bg, rc.emeraldFill, 0.85f)
private fun fullTone(rc: RafiqPalette)    = lerp(rc.emeraldFill, rc.ink, 0.10f)

/** «٢٦ ورداً» — والعربية تعدّ المفرد والمثنّى والقلّة بصيغٍ مختلفة. */
private fun wirdCount(n: Int, ar: Boolean): String = when {
    n == 0 -> "لا وِردَ بعد"
    n == 1 -> "وِردٌ واحد"
    n == 2 -> "وِردان"
    n <= 10 -> "${n.localized(ar)} أوراد"
    else -> "${n.localized(ar)} وِرداً"
}

private fun dayCount(n: Int, ar: Boolean): String = when {
    n == 0 -> "لم تفتحها بعد"
    n == 1 -> "يومٌ واحد"
    n == 2 -> "يومان"
    n <= 10 -> "${n.localized(ar)} أيّام"
    else -> "${n.localized(ar)} يوماً"
}

@Composable
fun AchievementsScreen(
    navController: NavHostController,
    viewModel: AwraqViewModel = koinViewModel(),
) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    Column(
        Modifier
            .fillMaxSize()
            .background(rc.bg)
            .statusBarsPadding(),
    ) {
        RafiqTopBar(
            title    = "أوراقي",
            subtitle = "ما أتممتَه — لتراه، لا ليحاسبك",
            onBack   = { navController.popBackStack() },
        )

        if (s.loading) return@Column

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            /* ═══ ١ · أيُّ وردٍ يثبت؟ ═══ */
            SectionHead("آخر سبعة أيّام", wirdCount(s.weekTotal, ar))
            WeekGrid(s.weekDays, s.rows, rc)
            Spacer(Modifier.height(12.dp))
            Legend(
                listOf(
                    fullTone(rc) to "أتممتَها",
                    emptyTone(rc) to "لم تفتحها",
                ),
                ring = "اليوم",
            )

            /* ═══ ما تقوله الشبكة بالكلمات ═══ */
            s.steadiest?.let {
                Spacer(Modifier.height(16.dp))
                Insight(
                    "أثبتُ محطّاتك: $it",
                    s.faintest?.let { f -> "وأخفُّها حضوراً: $f — فلعلّه موضعُ ما تريد أن تزيد." }
                        ?: "ما فاتك من أوراد هذا الأسبوع أقلُّ ممّا أتممت.",
                )
            }
            s.steadiestDay?.let {
                Spacer(Modifier.height(8.dp))
                Insight(
                    "وأثبتُ أيّامك: $it",
                    "أكثرُ ما تُتمّ أورادك في هذا اليوم من الأسبوع.",
                )
            }

            /* ═══ ٢ · كيف كان شهري؟ ═══ */
            SectionHead("هذا الشهر", dayCount(s.monthOpened, ar))
            MonthGrid(s.monthLevels, rc)
            Spacer(Modifier.height(12.dp))
            Legend(
                listOf(
                    fullTone(rc) to "أتممتَه",
                    partialTone(rc) to "بعضَه",
                    emptyTone(rc) to "لم تفتحه",
                ),
            )

            /* ═══ ٣ · وكم أتممت؟ ═══ */
            SectionHead("وما عدا الأوراد", "")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Total(s.tasbeeh.localized(ar), "تسبيحة", Modifier.weight(1f))
                Total(s.quranPages.localized(ar), "صفحة من المصحف", Modifier.weight(1f))
            }

            /* ═══ الختام — الدليلُ على غياب السلاسل ═══ */
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(rc.divider))
            Spacer(Modifier.height(22.dp))
            Text(
                "«أَحَبُّ الأعمالِ إلى اللهِ\nأَدْوَمُها وإنْ قَلَّ»",
                style     = RafiqType.dhikr,
                color     = rc.ink,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "متفق عليه · عن عائشة رضي الله عنها",
                style     = RafiqType.caption,
                color     = rc.inkMed,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(36.dp))
        }
    }
}

/* ── شبكة الأسبوع: ثمانُ محطّاتٍ × سبعةُ أيّام ─────────────────── */

@Composable
private fun WeekGrid(days: List<String>, rows: List<AwraqViewModel.StationRow>, rc: RafiqPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(LABEL_W))
            days.forEach {
                Text(
                    it,
                    style     = RafiqType.caption,
                    color     = rc.inkMed,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f),
                )
            }
        }
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    row.short,
                    style    = RafiqType.caption,
                    color    = rc.inkMed,
                    maxLines = 1,
                    modifier = Modifier.width(LABEL_W),
                )
                row.cells.forEach { c ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (c == Cell.DONE) fullTone(rc) else emptyTone(rc))
                            .then(
                                // «اليوم» حالةُ شكلٍ لا نبرة — حلقةٌ حول الخانة،
                                // فلا تزاحم الدرجات على مدىً ضيّق أصلاً.
                                if (c == Cell.TODAY)
                                    Modifier.border(1.5.dp, rc.emeraldFill, RoundedCornerShape(5.dp))
                                else Modifier,
                            ),
                    )
                }
            }
        }
    }
}

private val LABEL_W = 60.dp

/* ── شبكة الشهر: ثلاثون يوماً في عشرة أعمدة ──────────────────── */

@Composable
private fun MonthGrid(levels: List<Int>, rc: RafiqPalette) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        levels.chunked(10).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                week.forEach { lvl ->
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when (lvl) {
                                    2 -> fullTone(rc)
                                    1 -> partialTone(rc)
                                    else -> emptyTone(rc)
                                },
                            ),
                    )
                }
                // الصفُّ الأخير قد ينقص، فتُملأ فراغاته حتى تبقى الخانات مربّعة
                repeat(10 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/* ── قطعُ البناء ──────────────────────────────────────────────── */

@Composable
private fun SectionHead(title: String, trailing: String) {
    val rc = LocalRafiqColors.current
    Spacer(Modifier.height(24.dp))
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom,
    ) {
        Text(title, style = RafiqType.titleM, color = rc.ink)
        if (trailing.isNotEmpty()) {
            Text(trailing, style = RafiqType.bodyS, color = rc.inkMed)
        }
    }
}

@Composable
private fun Legend(items: List<Pair<Color, String>>, ring: String? = null) {
    val rc = LocalRafiqColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        items.forEach { (c, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(11.dp).clip(RoundedCornerShape(4.dp)).background(c))
                Spacer(Modifier.width(6.dp))
                Text(label, style = RafiqType.caption, color = rc.inkMed)
            }
        }
        if (ring != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.5.dp, rc.emeraldFill, RoundedCornerShape(4.dp)),
                )
                Spacer(Modifier.width(6.dp))
                Text(ring, style = RafiqType.caption, color = rc.inkMed)
            }
        }
    }
}

@Composable
private fun Insight(title: String, body: String) {
    val rc = LocalRafiqColors.current
    Column(Modifier.fillMaxWidth().rafiqCard().padding(15.dp)) {
        Text(title, style = RafiqType.titleM, color = rc.emerald)
        Spacer(Modifier.height(5.dp))
        Text(body, style = RafiqType.bodyS, color = rc.inkMed)
    }
}

@Composable
private fun Total(value: String, label: String, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Column(modifier.rafiqCard().padding(15.dp)) {
        Text(value, style = RafiqType.display, color = rc.emerald)
        Spacer(Modifier.height(3.dp))
        Text(label, style = RafiqType.bodyS, color = rc.inkMed)
    }
}
