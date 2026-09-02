package app.rafiqaldhikr.ui.screens.achievements

import app.rafiqaldhikr.util.weekdayIndex
import app.rafiqaldhikr.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.DayCompanionRepository
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * «أوراقي» — ما أتممتَه، لا نسبةً من هدفٍ لم تضعه.
 *
 * ثلاثةُ أسئلة تجيبها الشاشة: أيُّ وردٍ يثبت؟ وكيف كان شهري؟ وكم أتممت؟
 * ولا سلسلةَ هنا ولا نقاط — «أَحَبُّ الأعمالِ إلى اللهِ أَدْوَمُها وإنْ قَلَّ».
 */
class AwraqViewModel(
    private val companionRepo: DayCompanionRepository,
    private val progressRepo:  ProgressRepository,
) : ViewModel() {

    /** حالةُ خانةٍ واحدة في شبكة الأسبوع. */
    enum class Cell { DONE, MISSED, TODAY }

    data class StationRow(
        val id:    String,
        /*  مرجعُ مورد لا نصّ — كانت أسماءُ المحطّات الثماني مكتوبةً
         *  عربيةً في هذا الـViewModel، فشبكةُ «أوراقي» لا تُترجَم. */
        @androidx.annotation.StringRes val short: Int,
        val cells: List<Cell>,      // سبعٌ: من أقدم يومٍ إلى اليوم
        val done:  Int,
    )

    data class UiState(
        val loading:      Boolean          = true,
        /*  فهارسُ الأيّام لا حروفُها.
         *
         *  كان الـViewModel يُخرج الحرفَ العربيَّ جاهزاً — فيصل الشاشةَ
         *  نصّاً لا يمرّ بمترجم. والفهرسُ محايدٌ، والشاشةُ تقرأ حرفَه من
         *  الموارد بلغة صاحبها. */
        val weekDayIndices: List<Int>      = emptyList(),
        val rows:         List<StationRow> = emptyList(),
        val weekTotal:    Int              = 0,
        /** كثافةُ كلِّ يومٍ في الشهر: 0 = لم تفتحه · 1 = بعضه · 2 = أتممتَه. */
        val monthLevels:  List<Int>        = emptyList(),
        val monthOpened:  Int              = 0,
        @androidx.annotation.StringRes val steadiest: Int? = null,
        @androidx.annotation.StringRes val faintest:  Int? = null,
        val steadiestDayIndex: Int?        = null,
        val tasbeeh:      Long             = 0,
        val quranPages:   Long             = 0,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val weekStart  = today.minus(DatePeriod(days = 6))
            val monthStart = today.minus(DatePeriod(days = MONTH_DAYS - 1))

            combine(
                companionRepo.getCompletedRange(monthStart.toString(), today.toString()),
                progressRepo.getRange(monthStart.toString(), today.toString()),
            ) { logged, progress ->
                // أذكارُ الصباح والمساء تُسجَّل في «التقدّم اليومي» لا في سجلّ
                // المحطّات، فتُضمّ هنا وإلّا ظهر صفّاها فارغَين وهما متمّان.
                val byDate = HashMap<String, MutableSet<String>>()
                logged.forEach { (d, s) -> byDate.getOrPut(d) { mutableSetOf() } += s }
                progress.forEach { p ->
                    val set = byDate.getOrPut(p.date) { mutableSetOf() }
                    if (p.morningDone) set += "fajr_morning"
                    if (p.eveningDone) set += "asr_evening"
                }

                val week = (0 until 7).map { weekStart.plus(DatePeriod(days = it)) }
                val rows = STATIONS.map { (id, short) ->
                    val cells = week.map { d ->
                        when {
                            id in (byDate[d.toString()] ?: emptySet()) -> Cell.DONE
                            d == today                                 -> Cell.TODAY
                            else                                       -> Cell.MISSED
                        }
                    }
                    StationRow(id, short, cells, cells.count { it == Cell.DONE })
                }

                val month = (0 until MONTH_DAYS).map { monthStart.plus(DatePeriod(days = it)) }
                val levels = month.map { d ->
                    val n = byDate[d.toString()]?.count { it in STATION_IDS } ?: 0
                    when {
                        n == 0                 -> 0
                        n >= STATIONS.size - 1 -> 2
                        else                   -> 1
                    }
                }

                // أثبتُ أيّامك: يومُ الأسبوع الذي تُتمّ فيه أكثر أورادك خلال الشهر
                val perWeekday = month.groupBy { weekdayIndex(it) }
                    .mapValues { (_, days) ->
                        days.sumOf { d -> byDate[d.toString()]?.count { it in STATION_IDS } ?: 0 }
                    }
                val topDay = perWeekday.filterValues { it > 0 }.maxByOrNull { it.value }?.key

                val ranked = rows.sortedByDescending { it.done }

                UiState(
                    loading      = false,
                    weekDayIndices = week.map { weekdayIndex(it) },
                    rows         = rows,
                    weekTotal    = rows.sumOf { it.done },
                    monthLevels  = levels,
                    monthOpened  = levels.count { it > 0 },
                    steadiest    = ranked.firstOrNull()?.takeIf { it.done >= 3 }?.short,
                    faintest     = ranked.lastOrNull()?.takeIf { it.done <= 2 }?.short,
                    steadiestDayIndex = topDay,
                    tasbeeh      = progress.sumOf { it.tasbeehCount },
                    quranPages   = progress.sumOf { it.quranPages },
                )
            }.collect { _uiState.value = it }
        }
    }

    private companion object {
        const val MONTH_DAYS = 30

        /** مطابقةٌ لمحطّات «رفيق اليوم» — الثامنةُ الجمعة موسميّة فتُستثنى. */
        val STATIONS = listOf(
            "wake"          to R.string.st_wake_s,
            "fajr_morning"  to R.string.st_fajr_s,
            "duha"          to R.string.st_duha_s,
            "dhuhr"         to R.string.st_dhuhr_s,
            "asr_evening"   to R.string.st_asr_s,
            "maghrib"       to R.string.st_maghrib_s,
            "isha"          to R.string.st_isha_s,
            "sleep"         to R.string.st_sleep_s,
        )
        val STATION_IDS = STATIONS.map { it.first }.toSet()

        // weekdayIndex و WEEKDAY_LETTER و WEEKDAY_NAME نُقلت إلى
        // util/Weekday.kt: شاشتان تحتاجانها، ونسخُ الحسابِ يفترقان.
    }
}
