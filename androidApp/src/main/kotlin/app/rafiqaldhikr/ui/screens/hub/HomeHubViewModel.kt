package app.rafiqaldhikr.ui.screens.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.Wisdom
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.repository.TasbeehRepository
import app.rafiq.domain.repository.WisdomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * حالة أبواب الرئيسية.
 *
 * الرئيسية مركز انطلاق: كل باب فيها يحمل حاله — مصقولٌ إن فعلتَه اليوم،
 * مصدوءٌ إن لم تفعله. فلا تسأل المستخدم «إلى أين تريد؟» بل تريه ما بقي
 * عليه، وكل جواب باب.
 *
 * الأرقام كلّها من قاعدة البيانات المحلية — لا شيء مخترَع ولا شيء ثابت.
 */
class HomeHubViewModel(
    private val progressRepo: ProgressRepository,
    private val tasbeehRepo:  TasbeehRepository,
    private val wisdomRepo:   WisdomRepository,
) : ViewModel() {

    data class UiState(
        val morningDone: Boolean = false,
        val eveningDone: Boolean = false,
        val quranPages:  Int     = 0,
        val tasbeeh:     Int     = 0,
        val prayers:     Int     = 0,
        /** كلمةُ اليوم — تتصدّر الشاشة، وتحتها مصدرها. تدور يوماً بيوم. */
        val wisdom:      Wisdom?  = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val today: String
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

    init {
        viewModelScope.launch {
            val day = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
            val w = wisdomRepo.forDay(day.toLong())
            _uiState.value = _uiState.value.copy(wisdom = w)
        }
        viewModelScope.launch {
            val d = today
            combine(
                progressRepo.getByDate(d),
                tasbeehRepo.getTotalCountByDate(d),
            ) { p, tasbeeh ->
                _uiState.value.copy(
                    morningDone = p?.morningDone == true,
                    eveningDone = p?.eveningDone == true,
                    quranPages  = (p?.quranPages ?: 0L).toInt(),
                    tasbeeh     = tasbeeh.toInt(),
                    prayers     = (p?.prayersLogged ?: 0L).toInt(),
                )
            }.collect { _uiState.value = it }
        }
    }
}
