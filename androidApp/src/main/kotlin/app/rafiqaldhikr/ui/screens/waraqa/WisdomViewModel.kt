package app.rafiqaldhikr.ui.screens.waraqa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DuaItem
import app.rafiq.domain.repository.DuaRepository
import kotlinx.coroutines.flow.first
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
class WisdomViewModel(
    private val repo: WisdomRepository,
    private val duaRepo: DuaRepository,
) : ViewModel() {

    private val _wisdom = MutableStateFlow<Wisdom?>(null)
    val wisdom: StateFlow<Wisdom?> = _wisdom.asStateFlow()

    /** كلُّ الأدعية — تُنخَل منها رسالةُ اليوم في الشاشة بحسب الطور. */
    private val _duas = MutableStateFlow<List<DuaItem>>(emptyList())
    val duas: StateFlow<List<DuaItem>> = _duas.asStateFlow()

    /** رقمُ اليوم — بذرةُ الاختيار، ومفتاحُ حالة الفتح. */
    val today: Long = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()

    init {
        viewModelScope.launch { _wisdom.value = repo.forDay(today) }
        viewModelScope.launch {
            /*  تُجمع الفئاتُ كلُّها مرّةً واحدة: `DuaRepository` لا يملك
             *  «أعطني الكلّ» — وإضافتُه استعلامٌ جديدٌ لا يحتاجه سواها،
             *  والفئاتُ تسعٌ فالجمعُ منها أرخصُ من توسيع الواجهة. */
            duaRepo.getCategories().collect { cats ->
                val all = mutableListOf<DuaItem>()
                cats.forEach { c -> all += duaRepo.getByCategory(c).first() }
                //  ترتيبٌ ثابتٌ بالمعرّف: ترتيبُ الفئات قد يتبدّل، والبذرةُ
                //  تعتمد على الفهرس — فلو تبدّل الترتيبُ تبدّلت رسالةُ اليوم.
                _duas.value = all.sortedBy { it.id }
            }
        }
    }
}
