package app.rafiqaldhikr.ui.screens.adhkar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.Dhikr
import app.rafiq.domain.usecase.GetAdhkarByCategoryUseCase
import app.rafiq.domain.usecase.UpdateStreakUseCase
import app.rafiq.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DhikrReadingViewModel(
    private val savedState:   SavedStateHandle,
    private val getAdhkar:    GetAdhkarByCategoryUseCase,
    private val progressRepo: ProgressRepository,
    private val updateStreak: UpdateStreakUseCase
) : ViewModel() {

    data class UiState(
        val adhkar:         List<Dhikr> = emptyList(),
        val categoryId:     String      = "",
        val currentIndex:   Int         = 0,
        val currentCount:   Int         = 0,
        val isLoading:      Boolean     = true,
        val isAllCompleted: Boolean     = false,
        val error:          String?     = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * مجمِّعُ التصنيف الحاضر.
     *
     * كان كلُّ نداءٍ لـ[loadCategory] يُطلق مجمِّعاً جديداً ولا يُلغي ما قبله —
     * و`LaunchedEffect` يناديها عند كل دخول، وزرُّ «أعد المحاولة» ينادي أيضاً.
     * فتتكدّس مجمّعاتٌ تكتب في الحالة نفسها.
     */
    private var loadJob: kotlinx.coroutines.Job? = null

    fun loadCategory(category: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getAdhkar(category).collect { list ->
                    val index = savedState.get<Int>("index") ?: 0
                    val count = savedState.get<Int>("count") ?: 0
                    _uiState.update {
                        it.copy(
                            adhkar       = list,
                            categoryId   = category,
                            currentIndex = index.coerceAtMost(list.lastIndex.coerceAtLeast(0)),
                            currentCount = count,
                            isLoading    = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /*  ═══ العدُّ يقف عند العدد، ولا ينتقل من نفسه ═══
     *
     *  كان بلوغُ العدد ينقل إلى الذكر التالي **في الضغطة نفسِها**. فمن
     *  سبّح «سبحان الله وبحمده» مئةً، اختفى النصُّ تحت إصبعه عند المئة
     *  وحلّ محلَّه نصٌّ آخر لم يستعدّ له. وهو أسوأُ ما يقع في شاشة ذِكر:
     *  اليدُ أسرعُ من العين، فيضغط على الجديد قبل أن يقرأه.
     *
     *  فالعدُّ يقف الآن عند العدد، والانتقالُ بطلبٍ صريحٍ من صاحبه.
     */
    fun tap() {
        val state = _uiState.value
        if (state.adhkar.isEmpty() || state.isAllCompleted) return

        val currentDhikr = state.adhkar[state.currentIndex]
        if (state.currentCount >= currentDhikr.count) return   // اكتمل — ينتظر «التالي»

        val newCount = state.currentCount + 1
        savedState["count"] = newCount
        _uiState.update { it.copy(currentCount = newCount) }
    }

    /**
     * تراجعٌ عن ضغطةٍ واحدة.
     *
     *  ضغطةٌ خاطئةٌ في ذكرٍ عدده مئة كانت **لا تُستدرَك**: لا سبيلَ إلّا
     *  إعادةُ الباب من أوّله أو المضيُّ على عددٍ يعرف صاحبُه أنّه ليس
     *  الصواب. والعبادةُ لا تُبنى على رقمٍ يعلم أنّه خطأ.
     */
    fun undo() {
        val state = _uiState.value
        if (state.adhkar.isEmpty() || state.currentCount == 0) return
        val newCount = state.currentCount - 1
        savedState["count"] = newCount
        _uiState.update { it.copy(currentCount = newCount) }
    }

    /** الانتقالُ إلى الذكر التالي — بطلبٍ صريحٍ بعد اكتمال العدد. */
    fun next() {
        val state = _uiState.value
        if (state.adhkar.isEmpty() || state.isAllCompleted) return
        val currentDhikr = state.adhkar[state.currentIndex]
        if (state.currentCount < currentDhikr.count) return

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.adhkar.size) {
            _uiState.update { it.copy(isAllCompleted = true) }
            markCategoryCompleted()
        } else {
            savedState["index"] = nextIndex
            savedState["count"] = 0
            _uiState.update { it.copy(currentIndex = nextIndex, currentCount = 0) }
        }
    }

    private fun markCategoryCompleted() {
        val catId = _uiState.value.categoryId
        viewModelScope.launch {
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date.toString()
            
            progressRepo.ensureExists(today)
            if (catId == "morning") {
                progressRepo.updateMorning(today, true)
            } else if (catId == "evening") {
                progressRepo.updateEvening(today, true)
            }
            
            updateStreak(today)
        }
    }

    /**
     * يُستدعى بعد أن تُفتح شاشةُ الاحتفاء.
     *
     * وبدونه كانت الشاشةُ مصيدة: `isAllCompleted` تبقى `true`، فالرجوعُ من
     * الاحتفاء يُعيد إطلاقَ الانتقال إليه، والشريطُ السفليُّ مخفيٌّ على
     * الشاشتين — فلا مخرجَ إلا قتلُ التطبيق. والتصفيرُ هنا يجعل التصنيف
     * يبدأ من أوّله في المرّة القادمة، وهو الصواب: الأذكار تُعاد لا تُتابَع.
     */
    fun consumeCompletion() {
        savedState["index"] = 0
        savedState["count"] = 0
        _uiState.update { it.copy(isAllCompleted = false, currentIndex = 0, currentCount = 0) }
    }
}
