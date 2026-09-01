package app.rafiqaldhikr.ui.mushaf

import androidx.lifecycle.ViewModel
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** آياتُ صفحةٍ بترقيم المصحف — للنمط المضبوط الذي يعمل بلا تنزيل. */
class MushafPageViewModel(private val repo: QuranRepository) : ViewModel() {

    fun pageFlow(page: Int): Flow<List<AyahInfo>> = repo.getAyahsByPage(page)

    /** آيةٌ بعينها — تُلتقط من صفحتها لأنّها المفهرسةُ في القاعدة. */
    suspend fun ayah(surah: Int, ayah: Int, page: Int): AyahInfo? =
        repo.getAyahsByPage(page).first().firstOrNull { it.surah == surah && it.ayahNumber == ayah }

    suspend fun tafsir(surah: Int, ayah: Int): String? = repo.getTafsir(surah, ayah)

    suspend fun isMarked(surah: Int, ayah: Int): Boolean = repo.isBookmarked(surah, ayah)

    /**
     * يضع العلامةَ أو يرفعها — ويُرجع حالَها بعد الفعل.
     *
     * وهذا أوّلُ طريقٍ في التطبيق لإنشاء علامة: كانت شاشةُ العلامات
     * تعرض ولا شيءَ يكتب فيها، فتبقى فارغةً مهما فعل المستخدم.
     */
    suspend fun toggleMark(surah: Int, ayah: Int, page: Int): Boolean =
        if (repo.isBookmarked(surah, ayah)) {
            repo.removeBookmarkByPosition(surah, ayah)
            false
        } else {
            repo.addBookmark(surah, ayah, page)
            true
        }
}
