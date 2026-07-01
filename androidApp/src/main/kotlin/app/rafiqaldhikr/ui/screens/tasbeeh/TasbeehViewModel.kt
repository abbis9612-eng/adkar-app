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
    fun reset()     { savedState["count"] = 0 }
    fun setTarget(target: Int) {
        savedState["target"] = target
        savedState["count"]  = 0
    }
    fun setDhikr(text: String) {
        savedState["dhikr"] = text
        savedState["count"] = 0
    }

    fun saveSession() {
        val state = uiState.value
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
            // ✅ تحديث التقدم اليومي بعدد التسبيحات
            progressRepo.ensureExists(today)
            val totalToday = tasbeehRepo.getTotalCountByDate(today)
            totalToday.collect { total ->
                progressRepo.updateTasbeeh(today, total)
                return@collect
            }
            updateStreak(today)
        }
    }
}
