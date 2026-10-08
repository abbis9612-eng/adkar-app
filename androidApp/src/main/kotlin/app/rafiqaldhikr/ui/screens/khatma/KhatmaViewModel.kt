package app.rafiqaldhikr.ui.screens.khatma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.KhatmaPlan
import app.rafiq.domain.repository.KhatmaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** الختمةُ تُضبط مرّةً ثمّ تُنسى — والوِردُ يظهر محطّةً في صفّ اليوم. */
class KhatmaViewModel(private val repo: KhatmaRepository) : ViewModel() {

    val active: StateFlow<KhatmaPlan?> =
        repo.active().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val today: Long = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()

    fun start(days: Int, meeqat: String, continuous: Boolean) {
        viewModelScope.launch { repo.start(days = days, meeqat = meeqat, continuous = continuous) }
    }

    fun finish() {
        viewModelScope.launch { repo.finish() }
    }
}
