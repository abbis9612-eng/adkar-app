package app.rafiqaldhikr.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DuaItem
import app.rafiq.domain.repository.DuaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DuaViewModel(
    private val repository: DuaRepository
) : ViewModel() {

    data class UiState(
        val categories: List<String>  = emptyList(),
        val duas:       List<DuaItem> = emptyList(),
        val favorites:  List<DuaItem> = emptyList(),
        val isLoading:  Boolean       = true,
        val error:      String?       = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getCategories(),
                repository.getFavorites()
            ) { cats, favs ->
                _uiState.update {
                    it.copy(categories = cats, favorites = favs, isLoading = false)
                }
            }.collect { }
        }
    }

    fun loadCategory(category: String) {
        viewModelScope.launch {
            repository.getByCategory(category).collect { list ->
                _uiState.update { it.copy(duas = list) }
            }
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(id, !current) }
    }
}
