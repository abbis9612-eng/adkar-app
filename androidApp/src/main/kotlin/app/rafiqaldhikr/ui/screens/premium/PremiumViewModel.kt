package app.rafiqaldhikr.ui.screens.premium

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PremiumViewModel : ViewModel() {

    data class UiState(
        val isPremium: Boolean = false,
        val isLoading: Boolean = false,
        val error:     String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // M2: RevenueCat integration
    fun purchase() { /* placeholder */ }
    fun restore()  { /* placeholder */ }
}
