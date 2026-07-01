package app.rafiqaldhikr.ui.screens.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.db.CustomDhikr
import app.rafiq.domain.repository.CustomDhikrRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomDhikrViewModel(
    private val repository: CustomDhikrRepository
) : ViewModel() {

    val customDhikrs: StateFlow<List<CustomDhikr>> = repository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDhikr(text: String, target: Long) {
        viewModelScope.launch {
            repository.insert(text, target)
        }
    }

    fun deleteDhikr(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
