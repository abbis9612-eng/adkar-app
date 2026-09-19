package app.rafiqaldhikr.ui.screens.waraqa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.Wisdom
import app.rafiq.domain.repository.WisdomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * كلمةُ اليوم — تدور يوماً بيوم، وتُقرأ من القاعدة لا من الشبكة.
 *
 * نموذجٌ صغيرٌ مستقلٌّ بدل جرِّ `HomeHubViewModel` كلِّه إلى «أوراقي»:
 * ذاك يفتح مجمِّعاتٍ للتقدّم والتسبيح وآخر موضعٍ في المصحف، ولا تحتاج
 * هذه الشاشةُ منها شيئاً.
 */
class WisdomViewModel(private val repo: WisdomRepository) : ViewModel() {

    private val _wisdom = MutableStateFlow<Wisdom?>(null)
    val wisdom: StateFlow<Wisdom?> = _wisdom.asStateFlow()

    init {
        viewModelScope.launch {
            val day = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
            _wisdom.value = repo.forDay(day.toLong())
        }
    }
}
