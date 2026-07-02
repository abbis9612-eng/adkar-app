package app.rafiqaldhikr.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.UserDataRepository
import kotlinx.coroutines.launch

class ExportDataViewModel(
    private val userDataRepo: UserDataRepository
) : ViewModel() {

    fun exportJson(onReady: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { userDataRepo.exportAsJson() }
                .onSuccess(onReady)
        }
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { userDataRepo.clearAllUserData() }
                .onSuccess { onDone() }
        }
    }
}
