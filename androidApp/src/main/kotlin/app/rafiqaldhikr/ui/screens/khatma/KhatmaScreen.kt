package app.rafiqaldhikr.ui.screens.khatma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.wirdOf
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqTopBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalContext
import app.rafiqaldhikr.ui.mushaf.SurahNames
import app.rafiqaldhikr.ui.mushaf.juzOfPage
import app.rafiqaldhikr.ui.navigation.RafiqRoute
import app.rafiqaldhikr.ui.theme.RafiqShape
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.localizedDigits
import org.koin.androidx.compose.koinViewModel

/*
 *  التوقيع: وِردُ اليوم بطاقةٌ على هيئة مصحفٍ مفتوح — بدايتُه يميناً ووقوفُه شمالاً
 *
 *  وهي الفعلُ الأوّلُ نفسُه: لمسُها تفتح المصحفَ عند أوّل صفحةٍ من
 *  الوِرد. فما يُنظَر إليه هو ما يُلمَس، لا زينةٌ فوق زرّ.
 *
 * شاشةُ الختمة — **تُضبط مرّةً ثمّ تُنسى**.
 *
 * ولا تُفتح كلَّ يوم: الوِردُ يظهر محطّةً في صفّ يومك، ويُحتسب ما قرأتَه
 * من المصحف نفسِه بلا زرّ «أتممت» — من قرأ فقد قرأ، وزرُّ إقرارٍ زائدٌ
 * يُنسى فيبدو صاحبُه متأخّراً وهو مواظب. وهذه الشاشةُ للبدء والإنهاء.
 */

private val LENGTHS = listOf(7, 10, 15, 30, 60, 90)

/** الأطوارُ التي يصحّ تعليقُ الوِرد بها — ولا ليلَ: النومُ ميقاتُه. */
private val MEEQATS = listOf(
    "fajr" to R.string.fajr,
    "duha" to R.string.meeqat_duha,
    "dhuhr" to R.string.dhuhr,
    "asr" to R.string.asr,
    "maghrib" to R.string.maghrib,
    "isha" to R.string.isha,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KhatmaScreen(
    navController: NavHostController,
    vm: KhatmaViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val plan by vm.active.collectAsStateWithLifecycle()

    var days by remember { mutableIntStateOf(30) }
    var meeqat by remember { mutableStateOf("dhuhr") }
    var continuous by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(rc.bg).statusBarsPadding(),
    ) {
        RafiqTopBar(
            title = stringResource(R.string.khatma_title),
            onBack = { navController.popBackStack() },
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            val k = plan
            if (k == null) {
                Spacer(Modifier.height(18.dp))
                Text(stringResource(R.string.khatma_none), style = RafiqType.titleL, color = rc.ink)
                Spacer(Modifier.height(5.dp))
                Text(
                    stringResource(R.string.khatma_none_sub),
                    style = RafiqType.bodyS, color = rc.inkMed,
                )
                Spacer(Modifier.height(20.dp))

                Label(stringResource(R.string.khatma_days))
                Chips(
                    LENGTHS.map {
                        it to stringResource(R.string.khatma_days_n, it.toString())
                            .localizedDigits(ar)
                    },
                    days,
                ) { days = it }

                Spacer(Modifier.height(18.dp))
                Label(stringResource(R.string.khatma_meeqat))
                Chips(MEEQATS.map { it.first to stringResource(it.second) }, meeqat) { meeqat = it }

                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { continuous = !continuous }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.khatma_continuous),
                            style = RafiqType.body, color = rc.ink,
                        )
                        Text(
                            stringResource(R.string.khatma_continuous_sub),
                            style = RafiqType.caption, color = rc.inkMed,
                        )
                    }
                    Box(
                        Modifier
                            .size(width = 44.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(if (continuous) rc.emeraldFill else rc.divider),
                        contentAlignment =
                            if (continuous) Alignment.CenterStart else Alignment.CenterEnd,
                    ) {
                        Box(
                            Modifier.padding(3.dp).size(20.dp).clip(CircleShape)
                                .background(rc.card),
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp, 15.dp, 26.dp, 15.dp))
                        .background(rc.emeraldFill)
                        .clickable { vm.start(days, meeqat, continuous) }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.khatma_start),
                        style = RafiqType.titleM, color = rc.onEmeraldFill,
                    )
                }
            } else {
                val w = wirdOf(
                    fromPage = k.fromPage, toPage = k.toPage, days = k.days,
                    startedOn = k.startedOn, readTo = k.readTo, today = vm.today,
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(
                        R.string.khatma_day_of,
                        (k.days - w.daysLeft + 1).toString(), k.days.toString(),
                    ).localizedDigits(ar),
                    style = RafiqType.metaS, color = rc.inkLight,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(
                        R.string.khatma_progress,
                        k.readTo.coerceAtLeast(0).toString(), k.toPage.toString(),
                    ).localizedDigits(ar),
                    style = RafiqType.titleL, color = rc.ink,
                )

                Spacer(Modifier.height(18.dp))

                /*  وِردُ اليوم — مصحفٌ مفتوحٌ لا صندوقٌ وزرٌّ عريض.
                 *
                 *  وكان صندوقاً فيه سطرٌ وزرٌّ أخضر: شكلٌ يصلح لأيّ تطبيق
                 *  بتبديل الكلمات — فيرسب في اختبار النسخ. وهذه بطاقةٌ
                 *  **هي الفعلُ نفسُه**: ورقتان، البدايةُ يميناً والوقوفُ
                 *  شمالاً، وفي الكعب عددُ الصفحات وجزؤها. ولمسُها فتحٌ. */
                OpenLeafWird(
                    from = w.from, to = w.to, pages = w.pages, minutes = w.minutes,
                    onOpen = {
                        navController.navigate(RafiqRoute.Mushaf.atPage(w.from))
                    },
                )

                if (w.behind > 0) {
                    Spacer(Modifier.height(6.dp))
                    //  يُقال صراحةً — لا يُترك ليكتشفه حين ييأس
                    Text(
                        stringResource(R.string.wird_behind, w.behind.toString())
                            .localizedDigits(ar),
                        style = RafiqType.caption, color = rc.gold,
                    )
                }

                Spacer(Modifier.height(26.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, rc.cardBorder, RoundedCornerShape(14.dp))
                        .clickable { vm.finish() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.khatma_stop),
                        style = RafiqType.label, color = rc.inkMed,
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = RafiqType.metaS, color = LocalRafiqColors.current.inkLight)
    Spacer(Modifier.height(8.dp))
}

/** صفُّ شرائح — نفسُ لغة شرائح المحطّات في الرئيسية. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Chips(items: List<Pair<T, String>>, selected: T, onPick: (T) -> Unit) {
    val rc = LocalRafiqColors.current
    FlowRowCompat {
        items.forEach { (value, label) ->
            val on = value == selected
            Box(
                Modifier
                    .padding(end = 7.dp, bottom = 7.dp)
                    .clip(CircleShape)
                    .background(if (on) rc.emeraldFill else Color.Transparent)
                    .border(1.dp, if (on) rc.emeraldFill else rc.divider, CircleShape)
                    .clickable { onPick(value) }
                    .defaultMinSize(minHeight = 42.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = RafiqType.bodyS,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                    color = if (on) rc.onEmeraldFill else rc.inkMed,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), content = content)
}

/* ── وِردُ اليوم: مصحفٌ مفتوح ───────────────────────────────────────── */

/**
 * بطاقةُ الوِرد على هيئة ورقتين مفتوحتين.
 *
 * البدايةُ في اليمين والوقوفُ في اليسار — **لا عكسَ ذلك**: المصحفُ يُقرأ
 * من اليمين، فورقتُه اليمنى هي التي تبدأ. وفي الكعب عددُ الصفحات وجزؤها.
 *
 * وليست زينةً فوق زرّ: **البطاقةُ كلُّها هي الفعل** — لمسُها يفتح المصحفَ
 * عند أوّل صفحةٍ من الوِرد، فيُكمل من حيث يجب أن يبدأ.
 */
@Composable
private fun OpenLeafWird(
    from: Int,
    to: Int,
    pages: Int,
    minutes: Int,
    onOpen: () -> Unit,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val ctx = LocalContext.current
    //  الاسمُ من `surah_metadata.json` — لا من تخطيط المصحف الضخم
    val fromSurah = remember(from) { SurahNames.atPage(ctx, from) }
    val toSurah = remember(to) { SurahNames.atPage(ctx, to) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RafiqShape.card)
            .clickable(onClick = onOpen),
    ) {
        Text(
            stringResource(R.string.wird_short),
            style = RafiqType.metaS, color = rc.inkLight,
        )
        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(134.dp)
                .clip(RafiqShape.item)
                .border(1.dp, rc.gold.copy(alpha = 0.38f), RafiqShape.item),
        ) {
            Row(Modifier.fillMaxSize()) {
                Leaf(
                    label = stringResource(R.string.khatma_open_from),
                    page = from, surah = fromSurah, dim = false,
                    modifier = Modifier.weight(1f),
                )
                //  الكعبُ — خطٌّ ذهبيٌّ رقيقٌ لا فاصلٌ ثقيل
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(rc.gold.copy(alpha = 0.22f)),
                )
                Leaf(
                    label = stringResource(R.string.khatma_open_to),
                    page = to, surah = toSurah, dim = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                    .background(rc.emeraldFill)
                    .padding(horizontal = 13.dp, vertical = 4.dp),
            ) {
                Text(
                    stringResource(
                        R.string.khatma_open_spine,
                        pages.toString(), juzOfPage(from).toString(),
                    ).localizedDigits(ar),
                    style = RafiqType.metaS, color = rc.onEmeraldFill,
                )
            }
        }

        Spacer(Modifier.height(11.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.khatma_open_verb),
                style = RafiqType.label, color = rc.gold,
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(
                    R.string.wird_pages,
                    from.toString(), to.toString(), minutes.toString(),
                ).localizedDigits(ar),
                style = RafiqType.metaS, color = rc.inkLight,
            )
        }
    }
}

/** ورقةٌ واحدة — رقمُها واسمُ سورتها. والمنتهى أخفتُ من المبتدأ. */
@Composable
private fun Leaf(
    label: String,
    page: Int,
    surah: String,
    dim: Boolean,
    modifier: Modifier = Modifier,
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    Column(
        modifier.padding(start = 13.dp, end = 13.dp, top = 11.dp, bottom = 22.dp),
        horizontalAlignment = if (dim) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = RafiqType.metaS, color = rc.inkLight)
        Text(
            page.toString().localizedDigits(ar),
            style = RafiqType.display,
            color = if (dim) rc.inkLight else rc.ink,
        )
        Text(surah, style = RafiqType.dhikr, color = rc.gold, maxLines = 1)
    }
}
