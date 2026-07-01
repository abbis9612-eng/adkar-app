package app.rafiq.domain.usecase

import app.rafiq.domain.model.Dhikr
import app.rafiq.domain.repository.AdhkarRepository
import kotlinx.coroutines.flow.Flow

class GetAdhkarByCategoryUseCase(
    private val repository: AdhkarRepository
) {
    operator fun invoke(category: String): Flow<List<Dhikr>> =
        repository.getByCategory(category)
}
