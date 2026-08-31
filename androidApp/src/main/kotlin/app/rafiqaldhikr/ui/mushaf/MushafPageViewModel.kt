package app.rafiqaldhikr.ui.mushaf

import androidx.lifecycle.ViewModel
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow

/** آياتُ صفحةٍ بترقيم المصحف — للنمط المضبوط الذي يعمل بلا تنزيل. */
class MushafPageViewModel(private val repo: QuranRepository) : ViewModel() {
    fun pageFlow(page: Int): Flow<List<AyahInfo>> = repo.getAyahsByPage(page)
}
