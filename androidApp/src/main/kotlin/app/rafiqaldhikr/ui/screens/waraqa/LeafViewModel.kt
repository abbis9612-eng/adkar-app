package app.rafiqaldhikr.ui.screens.waraqa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.DayCompanionRepository
import app.rafiq.domain.repository.PrayerRepository
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
import kotlinx.datetime.toLocalDateTime

/**
 * ورقةُ اليوم — ما صلّيتَه، وما أتممتَه، وأسبوعُك.
 *
 * منفصلٌ عن `DayCompanionViewModel` لا ملحقٌ به: ذاك يبني المحطّاتِ من
 * أوقات الصلاة ويحتاج الموقع، وهذا يقرأ ما سجّلتَه ولا يحتاج شيئاً. فمن
 * لم يختر مدينتَه بعدُ يحيط صلاتَه بالقلم ويرى أسبوعَه كأيّ أحد.
 */
class LeafViewModel(
    private val prayerRepo:    PrayerRepository,
    private val progressRepo:  ProgressRepository,
    private val companionRepo: DayCompanionRepository,
) : ViewModel() {

    /** يومٌ من أسبوع المحاسبة — أقدمُها أوّلاً، واليومُ آخرُها. */
    data class WeekDay(val date: LocalDate, val prayed: Int)

    data class UiState(
        val prayed:    Set<String>       = emptySet(),
        val completed: Set<String>       = emptySet(),
        val morning:   Boolean           = false,
        val evening:   Boolean           = false,
        val week:      List<WeekDay>     = emptyList(),
        /** عزمُ اليوم، أو `null` ما لم يُعزَم بعد — فالسؤالُ يظهر. */
        val intentions: Set<String>?     = null,
    )

    private val today: LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    private val todayKey = today.toString()

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        val days = (WEEK - 1 downTo 0).map { today.minus(DatePeriod(days = it)) }
        viewModelScope.launch {
            combine(
                prayerRepo.getRange(days.first().toString(), todayKey),
                companionRepo.getCompletedStations(todayKey),
                progressRepo.getByDate(todayKey),
                companionRepo.getIntentions(todayKey),
            ) { logs, completed, progress, intents ->
                val prayedByDay = logs.filter { it.prayed }.groupBy { it.date }
                UiState(
                    prayed    = prayedByDay[todayKey].orEmpty().map { it.prayerName }.toSet(),
                    completed = completed,
                    morning   = progress?.morningDone == true,
                    evening   = progress?.eveningDone == true,
                    week      = days.map { d ->
                        WeekDay(d, prayedByDay[d.toString()].orEmpty().map { it.prayerName }.toSet().size)
                    },
                    intentions = intents.takeIf { it.isNotEmpty() },
                )
            }.collect { _ui.value = it }
        }
    }

    /**
     * يحيط الصلاةَ بالقلم أو يمحوها — بنمطِ `PrayerTimesViewModel.markPrayed`
     * نفسِه: السجلُّ ثمّ عددُ اليوم في `DailyProgress`، فتبقى الإحصاءاتُ
     * والتقريرُ الأسبوعيُّ على ما يعرفان.
     */
    fun setPrayed(prayer: String, prayed: Boolean) {
        viewModelScope.launch {
            prayerRepo.logPrayer(todayKey, prayer, prayed)
            progressRepo.ensureExists(todayKey)
            progressRepo.updatePrayers(todayKey, prayerRepo.getPrayedCount(todayKey, todayKey))
        }
    }

    fun setTask(task: StationTask, done: Boolean) {
        if (task.kind == StationTask.Kind.PRAYER) return setPrayed(task.key, done)
        viewModelScope.launch {
            if (done) companionRepo.completeStation(todayKey, task.key)
            else companionRepo.uncompleteStation(todayKey, task.key)
        }
    }

    /** «فعلتُها» — كلُّ مهامّ المحطّة، ثمّ المحطّةُ نفسُها لتعرفها الرئيسية. */
    fun completeStation(stationId: String, tasks: List<StationTask>) {
        tasks.forEach { setTask(it, true) }
        viewModelScope.launch { companionRepo.completeStation(todayKey, stationId) }
    }

    /** إلغاءُ «فعلتُها» إن ضُغطت خطأً — المحطّةُ ومهامُّها غيرُ الصلوات. */
    fun undoStation(stationId: String, tasks: List<StationTask>) {
        viewModelScope.launch {
            companionRepo.uncompleteStation(todayKey, stationId)
            tasks.filter { it.kind != StationTask.Kind.PRAYER }
                .forEach { companionRepo.uncompleteStation(todayKey, it.key) }
        }
    }

    /**
     * «عزمتُ» — يُحفظ ما اختاره. ومن لم يختر شيئاً حُفظ له [NONE]: أجاب
     * السؤالَ فلا يُعاد عليه اليوم، ولا يُكتب له عزمٌ لم يعزمه.
     */
    fun setIntentions(items: Set<String>) {
        viewModelScope.launch { companionRepo.setIntentions(todayKey, items.ifEmpty { setOf(NONE) }) }
    }

    companion object {
        const val NONE = "none"
        /** النوافلُ التي يُعزم عليها، ومهمّةُ كلٍّ في المحطّات — بها يُعلَّم العزمُ تامّاً. */
        val INTENT_TASK = linkedMapOf(
            "duha" to "duha.pray",
            "witr" to "isha.witr",
            "kahf" to "friday_kahf.read",
            "wird" to "wird.read",
        )
        const val WEEK = 7
        val PRAYERS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    }
}
