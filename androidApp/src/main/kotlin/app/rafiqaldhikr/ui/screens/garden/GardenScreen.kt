package app.rafiqaldhikr.ui.screens.garden

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.model.dayFill
import app.rafiqaldhikr.R
import app.rafiqaldhikr.ui.components.RafiqTopBar
import app.rafiqaldhikr.ui.components.rafiqCard
import app.rafiqaldhikr.ui.screens.profile.ProfileViewModel
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

/* ═══════════════════════════════════════════════════════════════════
   الحديقة الروحية

   ثلاثةُ أشياء كانت تكذب هنا، وكلُّها أُصلحت:

   ١) `coerceAtMost(4)` بعد جمع **خمسة** أعمال — فالسقفُ ٤، أي أنّ أربعةً
      من خمسة «زهرةٌ مكتملة». من ذكر وقرأ وسبّح ولم يصلِّ فرضاً واحداً
      تُهنّئه الحديقةُ بيومٍ تامّ. والحسابُ الآن `dayFill` المشترك،
      والزهرةُ لا تُفتح إلّا على ١٫٠ — وفيها الصلواتُ الخمس.

   ٢) الأسطورةُ تصف تركيبَ الأعمال («شجرة = أذكار + قرآن + صلاة») والحسابُ
      لا ينظر إلى التركيب بل إلى المقدار. فصارت تصف ما يحدث فعلاً.

   ٣) الأسبوعُ كان `state.weekProgress` كما جاء من القاعدة: ثلاثةُ أيّامٍ
      مسجَّلةٍ ← ثلاثُ نبتات، فيبدو الأسبوعُ الفارغُ أسبوعاً قصيراً لا
      أسبوعاً فارغاً. فيُكثَّف إلى سبعةٍ دائماً، والغائبُ بذرة — كما
      يفعل `DaysGrid` بالضبط.
═══════════════════════════════════════════════════════════════════ */

@Composable
fun GardenScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = org.koin.androidx.compose.koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current

    val today   = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val byDate  = state.weekProgress.associateBy { it.date }
    // سبعةٌ دائماً — والغائبُ بذرةٌ لا فجوة.
    val week: List<Pair<String, DailyProgressInfo?>> = (6 downTo 0).map { back ->
        val d = today.minus(back, DateTimeUnit.DAY).toString()
        d to byDate[d]
    }

    val todayStage = stageOf(dayFill(byDate[today.toString()]))

    Box(Modifier.fillMaxSize().background(rc.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            RafiqTopBar(
                title  = stringResource(R.string.garden_title),
                onBack = { navController.popBackStack() },
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.fillMaxWidth().rafiqCard()) {
                    Column(
                        modifier            = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(EMOJI[todayStage], fontSize = 72.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(LABEL[todayStage]), style = RafiqType.titleL, color = rc.emerald)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.garden_today_hint),
                            color = rc.inkMed, style = RafiqType.bodyS,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    stringResource(R.string.garden_week),
                    fontWeight = FontWeight.SemiBold,
                    color = rc.ink, style = RafiqType.titleM,
                )
                Spacer(Modifier.height(12.dp))

                /*  سبعةُ أيّامٍ في صفّ واحد — لا شبكةً كسولةً بأربعة أعمدة.
                 *  الأسبوعُ سبعةٌ، فيُرى سبعةً دفعةً واحدة كما في `DaysGrid`. */
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    week.forEachIndexed { idx, (date, p) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(EMOJI[stageOf(dayFill(p))], fontSize = 30.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                date.takeLast(2).localizedDigits(ar),
                                color = if (idx == week.lastIndex) rc.gold else rc.inkMed,
                                fontWeight = if (idx == week.lastIndex) FontWeight.Bold
                                             else FontWeight.Normal,
                                style = RafiqType.micro,
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Box(Modifier.fillMaxWidth().rafiqCard()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(R.string.garden_legend),
                            fontWeight = FontWeight.Bold, color = rc.emerald,
                            style = RafiqType.bodyS,
                        )
                        Spacer(Modifier.height(8.dp))
                        /*  الأسطورةُ تصف الحسابَ لا شيئاً آخر: خمسةُ أعمالٍ
                         *  (صباحٌ · مساءٌ · قرآنٌ · تسبيحٌ · صلوات)، والصلواتُ
                         *  كسرٌ من خمس. فمرحلةُ النبتة عددٌ لا تركيب. */
                        LEGEND.forEachIndexed { i, res ->
                            Text(
                                "${EMOJI[i]}  ${stringResource(res)}",
                                color = rc.inkMed,
                                modifier = Modifier.padding(vertical = 2.dp),
                                style = RafiqType.caption,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * مرحلةُ النبتة من نسبة اكتمال اليوم: ٠ بذرة … ٤ زهرة.
 *
 * والزهرةُ لا تُفتح إلّا على اليوم التامّ — `>= 1f` لا `>= 0.8f`.
 */
private fun stageOf(fill: Float): Int = when {
    fill >= 1f   -> 4
    fill >= 0.7f -> 3
    fill >= 0.4f -> 2
    fill > 0f    -> 1
    else         -> 0
}

private val EMOJI  = listOf("🌰", "🌱", "🌿", "🌳", "🌺")
private val LABEL  = listOf(
    R.string.garden_stage_seed, R.string.garden_stage_sprout,
    R.string.garden_stage_growing, R.string.garden_stage_tree,
    R.string.garden_stage_flower,
)
private val LEGEND = listOf(
    R.string.garden_legend_0, R.string.garden_legend_1, R.string.garden_legend_2,
    R.string.garden_legend_3, R.string.garden_legend_4,
)
