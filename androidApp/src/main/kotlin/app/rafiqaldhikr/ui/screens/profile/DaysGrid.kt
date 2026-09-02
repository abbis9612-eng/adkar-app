package app.rafiqaldhikr.ui.screens.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.rafiqaldhikr.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DailyProgressInfo
import app.rafiq.domain.repository.ProgressRepository
import app.rafiqaldhikr.ui.theme.LocalRafiqColors
import app.rafiqaldhikr.ui.theme.NumbersStyle
import app.rafiqaldhikr.ui.theme.RafiqType
import app.rafiqaldhikr.ui.utils.LocalArabicNumerals
import app.rafiqaldhikr.ui.utils.localizedDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

/* ═══════════════════════════════════════════════════════════════════
   دفتر الأيام

   شاشة الإحصاءات كانت أرقاماً في بطاقات: صفحات، تسبيحات، صلوات. وهي
   تخبرك بما فعلت ولا تُريك إيّاه.

   هنا كل يومٍ مضى **ورقةٌ صغيرة**، وارتفاع الحبر فيها ما خُطّ منها.
   الشهر كلّه في نظرة واحدة، والفراغ فيه يقول ما لا يقوله رقم.

   الامتلاء يُحسب من خمسة أعمال متساوية الوزن: أذكار الصباح، أذكار
   المساء، شيء من القرآن، شيء من التسبيح، والصلوات الخمس.
═══════════════════════════════════════════════════════════════════ */

private const val DaysShown = 30

class DaysGridViewModel(
    private val progressRepo: ProgressRepository,
) : ViewModel() {

    /** كل عنصر: امتلاء يومٍ 0..1، مرتّبة من الأقدم إلى اليوم. */
    private val _fills = MutableStateFlow<List<Float>>(emptyList())
    val fills: StateFlow<List<Float>> = _fills.asStateFlow()

    init {
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val first = today.minus(DaysShown - 1, DateTimeUnit.DAY)

            progressRepo.getRange(first.toString(), today.toString()).collect { rows ->
                val byDate = rows.associateBy { it.date }
                _fills.value = (0 until DaysShown).map { i ->
                    fillOf(byDate[first.plus(i, DateTimeUnit.DAY).toString()])
                }
            }
        }
    }

    private fun fillOf(p: DailyProgressInfo?): Float {
        if (p == null) return 0f
        var score = 0f
        if (p.morningDone) score += 1f
        if (p.eveningDone) score += 1f
        if (p.quranPages   > 0) score += 1f
        if (p.tasbeehCount > 0) score += 1f
        score += (p.prayersLogged.coerceIn(0, 5).toFloat() / 5f)
        return (score / 5f).coerceIn(0f, 1f)
    }
}

/* ═══════════════════════════════════════════════════════════════════ */

@Composable
fun DaysGrid(
    streakCurrent: Long,
    streakLongest: Long,
    vm: DaysGridViewModel = koinViewModel(),
) {
    val rc = LocalRafiqColors.current
    val ar = LocalArabicNumerals.current
    val fills by vm.fills.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                streakCurrent.toString().localizedDigits(ar),
                style = NumbersStyle,
                fontSize = RafiqType.display.fontSize,
                color = rc.gold,
            )
            Text(
                stringResource(R.string.days_streak, streakLongest.toString().localizedDigits(ar)),
                style = RafiqType.bodyS, color = rc.inkMed,
                modifier = Modifier.padding(bottom = 5.dp),
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            stringResource(R.string.days_caption, DaysShown.toString().localizedDigits(ar)),
            style = RafiqType.caption, color = rc.inkMed,
        )

        Spacer(Modifier.height(9.dp))

        if (fills.isEmpty()) {
            // لا نرسم شبكة فارغة تُوهم بأيام بيضاء قبل وصول البيانات
            Box(Modifier.fillMaxWidth().height(56.dp))
        } else {
            Leaves(fills)
        }

        Spacer(Modifier.height(12.dp))
        Legend()
    }
}

/** صفّان من الأوراق — خمس عشرة ورقة في كل صفّ. */
@Composable
private fun Leaves(fills: List<Float>) {
    val done = fills.count { it >= 0.999f }
    val ctx = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = ctx.getString(R.string.days_a11y, fills.size, done)
            },
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        /*  اليومُ يُعرف بموضعه في القائمة لا بمطابقة مرجع.
         *
         *  كان الشرط `row === fills.chunked(15).last()` — و`chunked` تُنشئ
         *  قائمةً جديدةً في كل نداء، فمقارنةُ المرجع `===` تكذب **دائماً**.
         *  فلم تكن ورقةُ اليوم تُميَّز بحلقتها الزمرّدية قطّ، ولا في مرّة.
         */
        val rows = fills.chunked(15)
        val todayIndex = fills.lastIndex
        rows.forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                row.forEachIndexed { i, f ->
                    Leaf(
                        fill = f,
                        isToday = rowIndex * 15 + i == todayIndex,
                        modifier = Modifier.weight(1f),
                    )
                }
                // آخر صفّ قد يكون ناقصاً — نملأ الفراغ حتى تبقى المقاسات واحدة
                repeat(15 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Leaf(fill: Float, isToday: Boolean, modifier: Modifier = Modifier) {
    val rc = LocalRafiqColors.current
    Canvas(modifier.aspectRatio(0.72f)) {
        val r = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        // الورقة الفارغة
        drawRoundRect(rc.chipBg, size = size, cornerRadius = r)
        // الحبر يرتفع من أسفلها
        if (fill > 0.01f) {
            val h = size.height * fill
            drawRoundRect(
                color = rc.gold,
                topLeft = Offset(0f, size.height - h),
                size = Size(size.width, h),
                cornerRadius = r,
            )
        }
        drawRoundRect(
            color = if (isToday) rc.emerald else rc.divider,
            size = size,
            cornerRadius = r,
            style = Stroke((if (isToday) 1.5f else 1f) * density),
        )
    }
}

@Composable
private fun Legend() {
    val rc = LocalRafiqColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(0.15f, 0.5f, 1f).forEach { f ->
            Canvas(Modifier.size(width = 9.dp, height = 13.dp)) {
                val r = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                drawRoundRect(rc.chipBg, size = size, cornerRadius = r)
                val h = size.height * f
                drawRoundRect(rc.gold, Offset(0f, size.height - h), Size(size.width, h), r)
                drawRoundRect(rc.divider, size = size, cornerRadius = r, style = Stroke(density))
            }
        }
        Spacer(Modifier.width(2.dp))
        Text(stringResource(R.string.days_legend), style = RafiqType.caption, color = rc.inkMed)
    }
}
