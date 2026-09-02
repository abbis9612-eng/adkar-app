package app.rafiqaldhikr.ui.screens.tasbeeh

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.ProgressRepository
import app.rafiq.domain.repository.TasbeehRepository
import app.rafiq.domain.usecase.UpdateStreakUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TasbeehViewModel(
    private val savedState:    SavedStateHandle,
    private val tasbeehRepo:   TasbeehRepository,
    private val progressRepo:  ProgressRepository,
    private val updateStreak:  UpdateStreakUseCase
) : ViewModel() {

    data class UiState(
        val count:       Int     = 0,
        val target:      Int     = 33,
        val dhikrText:   String  = "سبحان الله",
        val isCompleted: Boolean = false
    )

    private val _count     = savedState.getStateFlow("count",  0)
    private val _target    = savedState.getStateFlow("target", 33)
    private val _dhikrText = savedState.getStateFlow("dhikr",  "سبحان الله")

    val uiState: StateFlow<UiState> = combine(
        _count, _target, _dhikrText
    ) { count, target, dhikr ->
        UiState(count = count, target = target, dhikrText = dhikr, isCompleted = count >= target)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun increment() { savedState["count"] = _count.value + 1 }

    /** تصفيرٌ بعد حفظِ ما عُدّ — لا محوَ له. */
    fun reset() {
        saveSession()
        savedState["count"] = 0
    }

    fun setTarget(target: Int) {
        saveSession()
        savedState["target"] = target
        savedState["count"]  = 0
    }

    fun setDhikr(text: String) {
        saveSession()
        savedState["dhikr"] = text
        savedState["count"] = 0
    }

    /**
     * آخرُ عددٍ حُفظ — حتى لا يُكتب الشيءُ نفسُه مرّتين.
     *
     * الحفظُ صار يقع عند كل مخرج (تصفير، تبديل ذكر أو هدف، مغادرة الشاشة)
     * لا عند زرّ التصفير وحده، فبلا هذا الحارس يُسجَّل الشوطُ الواحد
     * مرّتين إن صُفِّر ثم غُودرت الشاشة.
     */
    private var lastSavedCount = 0

    /**
     * يحفظ الشوطَ الحاضر ويُحدّث تقدّمَ اليوم والسلسلة.
     *
     * كان لا يُنادى إلا من زرّ التصفير — فمن عدّ مئةً ثمّ خرج لم يُحفظ له
     * شيء: لا رقمَ في الرئيسية ولا تقدّمَ في اليوم. وكان فيه كذلك
     * `return@collect` يُظنّ كسراً للحلقة وليس كذلك — فلا يُبلَغ
     * [updateStreak] أبداً، والسلسلةُ لا تتقدّم بالتسبيح قطّ.
     */
    fun saveSession() {
        val state = uiState.value
        if (state.count <= 0 || state.count == lastSavedCount) return
        lastSavedCount = state.count
        viewModelScope.launch {
            val now   = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            tasbeehRepo.saveSession(
                dhikrText       = state.dhikrText,
                count           = state.count,
                target          = state.target,
                completed       = state.isCompleted,
                durationSeconds = 0L,
                date            = today
            )
            progressRepo.ensureExists(today)
            // first() لا collect: نريد أوّلَ قيمةٍ ثمّ ننصرف. وcollect لا
            // ينتهي على Flow قاعدةٍ حيّ، فما بعده لا يُبلَغ.
            val total = tasbeehRepo.getTotalCountByDate(today).first()
            progressRepo.updateTasbeeh(today, total)
            updateStreak(today)
        }
    }

    /** مغادرةُ الشاشة مخرجٌ كغيره — ما عُدّ يُحفظ. */
    override fun onCleared() {
        saveSession()
        super.onCleared()
    }
}
