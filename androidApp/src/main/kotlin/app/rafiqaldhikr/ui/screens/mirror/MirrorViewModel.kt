package app.rafiqaldhikr.ui.screens.mirror

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.MirrorDay
import app.rafiq.domain.model.MirrorSummary
import app.rafiq.domain.model.mirrorOf
import app.rafiq.domain.model.summarize
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * مرآةُ آخرِ ٣٦٥ يوماً — **لا السنةِ الميلاديّة**.
 *
 * ولو كانت السنةَ الميلاديّة لرأى من فتحها في يناير مرآةً فارغة، ومن
 * فتحها في ديسمبر مرآةً ممتلئةً تُفرغ بعد أيّام. والنافذةُ المتحرّكةُ
 * تُري عاماً كاملاً في كلّ يوم.
 */
class MirrorViewModel(private val repo: ProgressRepository) : ViewModel() {

    data class UiState(
        val days: List<MirrorDay> = emptyList(),
        val summary: MirrorSummary? = null,
        val loading: Boolean = true,
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            val start = today.plus(DatePeriod(days = -364))
            //  تواريخُ النافذة كلُّها — فالأيّامُ الخاليةُ تبقى في موضعها
            val all = buildList {
                var d: LocalDate = start
                while (d <= today) { add(d.toString()); d = d.plus(DatePeriod(days = 1)) }
            }
            repo.getRange(start.toString(), today.toString()).collect { rows ->
                val mirror = mirrorOf(rows, all)
                _ui.value = UiState(mirror, summarize(rows, mirror), loading = false)
            }
        }
    }
}
