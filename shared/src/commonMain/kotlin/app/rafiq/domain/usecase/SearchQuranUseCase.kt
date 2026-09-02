package app.rafiq.domain.usecase

import app.rafiq.domain.model.ArabicSearch
import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * بحثُ القرآن.
 *
 * كان يمرّر ما كتبه المستخدم خاماً إلى `LIKE` على عمودٍ مشكولٍ بالرسم
 * العثماني — فيرجع بصفر نتائج **دائماً**، لكلّ كلمةٍ بلا استثناء. الآن
 * يُطبَّع الاستعلامُ بنفس ما وُلّد به العمود، فيلتقيان.
 */
class SearchQuranUseCase(
    private val repository: QuranRepository
) {
    operator fun invoke(query: String): Flow<List<AyahInfo>> {
        val normalized = ArabicSearch.normalize(query)
        // الحدُّ على النصّ المطبَّع لا على ما كُتب: التطبيعُ يُقصّر الكلمة،
        // واستعلامٌ بحرفين يُطابق آلافَ الآيات فيعطي ضجيجاً لا نتائج.
        if (normalized.length < ArabicSearch.MIN_QUERY) return flowOf(emptyList())
        return repository.searchAyahs(normalized)
    }
}
