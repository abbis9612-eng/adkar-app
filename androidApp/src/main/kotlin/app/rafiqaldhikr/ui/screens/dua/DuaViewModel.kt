package app.rafiqaldhikr.ui.screens.dua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.DuaItem
import app.rafiq.domain.repository.DuaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DuaViewModel(
    private val repository: DuaRepository
) : ViewModel() {

    data class UiState(
        val categories:     List<String>      = emptyList(),
        val categoryCounts: Map<String, Long> = emptyMap(),
        val duas:           List<DuaItem>     = emptyList(),
        val favorites:      List<DuaItem>     = emptyList(),
        val isLoading:      Boolean           = true,
        val error:          String?           = null,
        /** تحميلُ أدعية التصنيف — مستقلٌّ عن تحميل التصنيفات. */
        val duasLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getCategories(),
                repository.getCategoryCounts(),
                repository.getFavorites()
            ) { cats, counts, favs ->
                _uiState.update {
                    it.copy(categories = cats, categoryCounts = counts, favorites = favs, isLoading = false)
                }
            }.collect { }
        }
    }

    /**
     * مجمِّعُ التصنيف الحاضر.
     *
     * كان كلُّ نداءٍ يُطلق مجمِّعاً جديداً ولا يُلغي ما قبله، و`LaunchedEffect`
     * يناديها عند كل دخول — فتتكدّس مجمّعاتٌ تكتب في الحالة نفسها، ويظلّ
     * أوّلُها يكتب أدعيةَ التصنيف القديم فوق الجديد.
     */
    private var categoryJob: kotlinx.coroutines.Job? = null

    fun loadCategory(category: String) {
        categoryJob?.cancel()
        /*  `duasLoading` تخصّ هذا التصنيف وحدَه.
         *
         *  كانت الشاشة تعتمد `isLoading` العامّة — وهي تُطفأ من مجمِّع
         *  **التصنيفات** في `init`، لا من هنا. فتومض «لا أدعية» قبل أن
         *  تصل القائمة.  */
        _uiState.update { it.copy(duas = emptyList(), duasLoading = true) }
        categoryJob = viewModelScope.launch {
            repository.getByCategory(category).collect { list ->
                _uiState.update { it.copy(duas = list, duasLoading = false) }
            }
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch { repository.toggleFavorite(id, !current) }
    }
}
