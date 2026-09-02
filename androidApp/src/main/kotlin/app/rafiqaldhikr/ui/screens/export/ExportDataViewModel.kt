package app.rafiqaldhikr.ui.screens.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.repository.ImportResult
import app.rafiq.domain.repository.UserDataRepository
import kotlinx.coroutines.launch

class ExportDataViewModel(
    private val userDataRepo: UserDataRepository
) : ViewModel() {

    fun exportJson(onReady: (String) -> Unit, onError: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { userDataRepo.exportAsJson() }
                .onSuccess(onReady)
                // كان `onSuccess` وحدَه: يفشل التصديرُ فلا يعلم أحد.
                .onFailure { onError() }
        }
    }

    /**
     * يستورد ملفَّ تصديرٍ سابق.
     *
     * كان التصديرُ يعمل ولا استيرادَ معه — أي نسخٌ احتياطيٌّ بلا استعادة.
     */
    fun importJson(text: String, onDone: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val r = runCatching { userDataRepo.importFromJson(text) }
                .getOrElse { ImportResult.Invalid(ImportResult.Reason.NOT_JSON) }
            onDone(r)
        }
    }

    fun deleteAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { userDataRepo.clearAllUserData() }
                .onSuccess { onDone() }
        }
    }
}
