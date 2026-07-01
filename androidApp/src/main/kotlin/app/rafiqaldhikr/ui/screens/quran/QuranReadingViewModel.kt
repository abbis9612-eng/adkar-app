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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuranReadingViewModel(
    private val savedState: SavedStateHandle,
    private val repository: QuranRepository
) : ViewModel() {

    data class UiState(
        val ayahs:     List<AyahInfo>  = emptyList(),
        val surah:     SurahInfo?      = null,
        val bookmarks: Set<Int>        = emptySet(),
        val isLoading: Boolean         = true,
        val error:     String?         = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            val surahInfo = repository.getAllSurahs().first()
                .find { it.number == surahNumber }
            _uiState.update { it.copy(surah = surahInfo) }

            combine(
                repository.getAyahsBySurah(surahNumber),
                repository.getBookmarks()
            ) { ayahs, bookmarks ->
                _uiState.update {
                    it.copy(
                        ayahs     = ayahs,
                        bookmarks = bookmarks
                            .filter { b -> b.surah == surahNumber }
                            .map { b -> b.ayah }.toSet(),
                        isLoading = false
                    )
                }
            }.collect { }
        }
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
