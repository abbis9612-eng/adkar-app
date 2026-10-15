package app.rafiqaldhikr.ui.screens.adhkar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * حالُ شاشة أبواب الذِّكر — البابُ الحاضر، وما أُتِمّ، والمواظبة.
 */
class AdhkarCategoriesViewModel(
    private val streakRepo:   StreakRepository,
    private val progressRepo: ProgressRepository,
) : ViewModel() {

    data class UiState(
        /** سلسلةُ الأيّام المتتابعة كما يحسبها [StreakRepository]. */
        val streak: Long = 0,
        /** آخرُ سبعةِ أيّامٍ أقدمُها أوّلاً — `true` ليومٍ سُجِّل فيه شيء. */
        val week: List<Boolean> = List(DOTS) { false },
        /**
         * ما سُجِّل اليومَ فعلاً.
         *
         * والبابان لا غير: `DailyProgress` لا يحمل إلّا الصباحَ والمساء.
         * ووسمُ النومِ أو الصلاةِ أو المتفرّقاتِ «تمّت» لمجرّد مضيِّ
         * وقتِها شهادةٌ بعبادةٍ لم تُسجَّل — وهي قاعدةُ «رفيق اليوم»
         * نفسُها، لا تُخالَف هنا لتمتلئ خمسُ علاماتٍ بدل اثنتين.
         */
        val doneToday: Set<String> = emptySet(),
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val days = (0 until DOTS).map { today.minus(DatePeriod(days = DOTS - 1 - it)).toString() }

        viewModelScope.launch {
            combine(
                streakRepo.getStreak(),
                progressRepo.getRange(days.first(), days.last()),
            ) { streak, range ->
                val byDate = range.associateBy { it.date }
                val todayRow = byDate[days.last()]
                UiState(
                    streak = streak.current,
                    week   = days.map { d ->
                        byDate[d]?.let { it.morningDone || it.eveningDone } == true
                    },
                    doneToday = buildSet {
                        if (todayRow?.morningDone == true) add("morning")
                        if (todayRow?.eveningDone == true) add("evening")
                    },
                )
            }.collect { s -> _uiState.value = s }
        }
    }

    companion object {
        /** عددُ حبّاتِ المواظبة — أسبوعٌ يُرى في سطر. */
        const val DOTS = 7

        /**
         * البابُ الحاضرُ من ساعةِ المكان.
         *
         * وهو **ساعةُ الجهاز لا الشمس**: هذه الشاشةُ لا تملك إحداثيّاتٍ
         * ولا تطلبها، ولا يصحّ أن يصير بابُ الذِّكر رهنَ إذنِ موقعٍ قد
         * يُرفَض. والحدودُ تقريبٌ معلَن: من رآها لا تطابق أذانَه فتح
         * البابَ الذي يريد — فكلُّها مفتوحةٌ دائماً، و«الآن» ترشيحٌ لا قفل.
         */
        fun nowKey(hour: Int): String = when (hour) {
            in 4..10  -> "morning"   // من الفجر إلى الضحى
            in 11..14 -> "prayer"    // الظهرُ والعصرُ ودُبُرُهما
            in 15..20 -> "evening"   // من العصر إلى العشاء
            else      -> "sleep"     // ما بعد العشاء إلى السَّحَر
        }
    }
}
