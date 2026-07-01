package app.rafiqaldhikr.ui.screens.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.PrefsRepository
import app.rafiq.domain.usecase.CalculateQiblaUseCase
import app.rafiqaldhikr.service.CompassManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QiblaViewModel(
    private val calculateQibla: CalculateQiblaUseCase,
    private val compassManager: CompassManager,
    private val prefsRepo:      PrefsRepository
) : ViewModel() {

    data class UiState(
        val qiblaBearing:      Float   = 0f,
        val deviceHeading:     Float   = 0f,
        val rotationToQibla:   Float   = 0f,
        val isCompassAvailable: Boolean = true,
        val isLocationKnown:   Boolean  = false,
        val error:             String?  = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.getPrefs().collect { prefs ->
                if (prefs == null) return@collect
                val lat = prefs.lastKnownLat.takeIf { it != 0.0 } ?: 35.5558
                val lng = prefs.lastKnownLng.takeIf { it != 0.0 } ?: 45.4436
                val bearing = calculateQibla(lat, lng)
                
                _uiState.update { 
                    it.copy(
                        qiblaBearing = bearing,
                        isLocationKnown = true
                    )
                }
            }
        }

        viewModelScope.launch {
            if (!compassManager.isAvailable) {
                _uiState.update { it.copy(isCompassAvailable = false) }
                return@launch
            }
            compassManager.getHeadingFlow().collect { heading ->
                val qibla = _uiState.value.qiblaBearing
                _uiState.update {
                    it.copy(
                        deviceHeading   = heading,
                        rotationToQibla = (qibla - heading + 360f) % 360f
                    )
                }
            }
        }
    }
}
