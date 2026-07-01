package app.rafiqaldhikr.ui.screens.khatira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.Khatira
import app.rafiq.domain.usecase.GetKhatiraUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class KhatiraViewModel(
    private val getKhatira: GetKhatiraUseCase
) : ViewModel() {

    data class UiState(
        val khatira:   Khatira? = null,
        val isLoading: Boolean  = true,
        val error:     String?  = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val dayOfYear = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .dayOfYear
        viewModelScope.launch {
            getKhatira(dayOfYear).collect { khatira ->
                _uiState.update { it.copy(khatira = khatira, isLoading = false) }
            }
        }
    }
}
