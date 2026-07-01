package app.rafiqaldhikr.ui.screens.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.SurahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuranListViewModel(
    private val repository: QuranRepository
) : ViewModel() {

    data class UiState(
        val surahs:    List<SurahInfo> = emptyList(),
        val filtered:  List<SurahInfo> = emptyList(),
        val query:     String          = "",
        val isLoading: Boolean         = true,
        val error:     String?         = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllSurahs().collect { list ->
                _uiState.update {
                    it.copy(surahs = list, filtered = list, isLoading = false)
                }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) state.surahs
            else state.surahs.filter { s ->
                s.nameAr.contains(query) ||
                s.nameEn.contains(query, ignoreCase = true) ||
                s.nameTranslit.contains(query, ignoreCase = true) ||
                s.number.toString() == query.trim()
            }
            state.copy(query = query, filtered = filtered)
        }
    }
}
