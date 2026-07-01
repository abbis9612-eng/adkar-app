package app.rafiqaldhikr.ui.screens.adhkar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.Dhikr
import app.rafiq.domain.usecase.GetAdhkarByCategoryUseCase
import app.rafiq.domain.usecase.UpdateStreakUseCase
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DhikrReadingViewModel(
    private val savedState:   SavedStateHandle,
    private val getAdhkar:    GetAdhkarByCategoryUseCase,
    private val progressRepo: ProgressRepository,
    private val updateStreak: UpdateStreakUseCase
) : ViewModel() {

    data class UiState(
        val adhkar:         List<Dhikr> = emptyList(),
        val categoryId:     String      = "",
        val currentIndex:   Int         = 0,
        val currentCount:   Int         = 0,
        val isLoading:      Boolean     = true,
        val isAllCompleted: Boolean     = false,
        val error:          String?     = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadCategory(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getAdhkar(category).collect { list ->
                    val index = savedState.get<Int>("index") ?: 0
                    val count = savedState.get<Int>("count") ?: 0
                    _uiState.update {
                        it.copy(
                            adhkar       = list,
                            categoryId   = category,
                            currentIndex = index.coerceAtMost(list.lastIndex.coerceAtLeast(0)),
                            currentCount = count,
                            isLoading    = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun tap() {
        val state = _uiState.value
        if (state.adhkar.isEmpty() || state.isAllCompleted) return

        val currentDhikr = state.adhkar[state.currentIndex]
        val newCount     = state.currentCount + 1

        if (newCount >= currentDhikr.count) {
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.adhkar.size) {
                _uiState.update { it.copy(isAllCompleted = true) }
                markCategoryCompleted()
            } else {
                savedState["index"] = nextIndex
                savedState["count"] = 0
                _uiState.update { it.copy(currentIndex = nextIndex, currentCount = 0) }
            }
        } else {
            savedState["count"] = newCount
            _uiState.update { it.copy(currentCount = newCount) }
        }
    }

    private fun markCategoryCompleted() {
        val catId = _uiState.value.categoryId
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()
            
            progressRepo.ensureExists(today)
            if (catId == "morning") {
                progressRepo.updateMorning(today, true)
            } else if (catId == "evening") {
                progressRepo.updateEvening(today, true)
            }
            
            updateStreak(today)
        }
    }
}
