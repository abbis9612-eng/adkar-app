package app.rafiq.domain.usecase

import app.rafiq.domain.model.AyahInfo
import app.rafiq.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SearchQuranUseCase(
    private val repository: QuranRepository
) {
    operator fun invoke(query: String): Flow<List<AyahInfo>> {
        if (query.length < 2) return flowOf(emptyList())
        return repository.searchAyahs(query)
    }
}
