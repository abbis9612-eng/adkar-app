package app.rafiqaldhikr.ui.screens.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.model.SurahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * مصحف يُقلب صفحة صفحة: 604 صفحات مبنية على أرقام صفحات مصحف المدينة
 * المخزنة مع كل آية — تُحمّل كل صفحة عند الحاجة وتُحفظ في ذاكرة مؤقتة.
 */
class QuranReadingViewModel(
    private val savedState: SavedStateHandle,
    private val repository: QuranRepository
) : ViewModel() {

    companion object { const val TOTAL_PAGES = 604 }

    data class TafsirState(
        val ayah:       AyahInfo,
        val tafsirText: String
    )

    data class UiState(
        val surah:         SurahInfo?               = null,
        val surahByNumber: Map<Int, SurahInfo>      = emptyMap(),
        val pages:         Map<Int, List<AyahInfo>> = emptyMap(),
        // معرف العلامة = surah*1000 + ayah — يغطي كل المصحف لا سورة واحدة
        val bookmarks:     Set<Long>                = emptySet(),
        val tafsir:        TafsirState?             = null,
        val isLoading:     Boolean                  = true,
        val error:         String?                  = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            val allSurahs = repository.getAllSurahs().first()
            val surahInfo = allSurahs.find { it.number == surahNumber }
            _uiState.update {
                it.copy(
                    surah         = surahInfo,
                    surahByNumber = allSurahs.associateBy { s -> s.number },
                    isLoading     = surahInfo == null
                )
            }

            // العلامات عبر كامل المصحف — تتحدث تفاعلياً
            repository.getBookmarks().collect { list ->
                _uiState.update { st ->
                    st.copy(bookmarks = list.map { b -> (b.surah * 1000 + b.ayah).toLong() }.toSet())
                }
            }
        }
    }

    /** الصفحات التي يُحمَّل جوارها مسبقاً على كل جانب */
    private val prefetchRadius = 2
    /** نصف قطر الذاكرة المؤقتة — ما بَعُد عن الصفحة الحالية يُقلَّم */
    private val cacheRadius = 4
    /** استعلامات جارية — تمنع تكرار الاستعلام لنفس الصفحة */
    private val inFlight = mutableSetOf<Int>()

    /**
     * يحمّل الصفحة وجيرانها قبل أن تصل إليها العين، ويقلّم البعيد.
     * بهذا لا يقف التقليب على استعلام قاعدة البيانات ولا تتضخّم الذاكرة.
     */
    fun prefetchAround(page: Int) {
        loadPage(page)
        for (step in 1..prefetchRadius) {
            loadPage(page + step)
            loadPage(page - step)
        }
        trimCache(page)
    }

    private fun trimCache(center: Int) {
        _uiState.update { st ->
            if (st.pages.size <= cacheRadius * 2 + 1) st
            else st.copy(pages = st.pages.filterKeys { abs(it - center) <= cacheRadius })
        }
    }

    fun loadPage(page: Int) {
        if (page < 1 || page > TOTAL_PAGES) return
        if (_uiState.value.pages.containsKey(page)) return
        if (!inFlight.add(page)) return
        viewModelScope.launch {
            try {
                val ayahs = repository.getAyahsByPage(page).first()
                _uiState.update { it.copy(pages = it.pages + (page to ayahs)) }
            } finally {
                inFlight.remove(page)
            }
        }
    }

    fun showTafsir(ayah: AyahInfo) {
        viewModelScope.launch {
            val text = repository.getTafsir(ayah.surah, ayah.ayahNumber)
                ?: "التفسير غير متوفر لهذه الآية"
            _uiState.update { it.copy(tafsir = TafsirState(ayah, text)) }
        }
    }

    fun dismissTafsir() {
        _uiState.update { it.copy(tafsir = null) }
    }

    fun savePosition(surah: Int, ayah: Int, page: Int, scrollY: Float) {
        viewModelScope.launch {
            repository.saveLastRead(surah, ayah, page, scrollY)
        }
    }

    fun toggleBookmark(surah: Int, ayah: Int, page: Int) {
        viewModelScope.launch {
            val isMarked = repository.isBookmarked(surah, ayah)
            if (isMarked) {
                repository.removeBookmarkByPosition(surah, ayah)
            } else {
                repository.addBookmark(surah, ayah, page)
            }
        }
    }
}
